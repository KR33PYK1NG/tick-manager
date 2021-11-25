package rmc.mixins.tick_manager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.electronwill.nightconfig.core.file.FileConfig;
import com.mohistmc.api.mc.ChunkMcAPI;

import codechicken.chunkloader.api.IChunkLoaderHandler;
import codechicken.chunkloader.world.Organiser;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.WorldEntitySpawner;
import rmc.mixins.tick_manager.extend.ChunkEx;
import rmc.mixins.tick_manager.extend.ChunkLoaderHandlerEx;
import rmc.mixins.tick_manager.extend.TileEntityEx;

/**
 * Developed by RMC Team, 2021
 */
public abstract class TickManager {

    public static final int ENTITY_SPAWN_BOUNDS;
    private static final int CHUNK_RADIUS;
    private static final int CHUNK_TICK_RADIUS;
    private static final int CHUNK_SPAWN_RADIUS;

    static {
        File cfgfile = new File("mixincfg/tick-manager.toml");
        cfgfile.getParentFile().mkdir();
        FileConfig config = FileConfig.of(cfgfile);
        config.load();
        config.add("entity-spawn-bounds", 16);
        config.add("chunk-radius", 1);
        config.add("chunk-tick-radius", 6);
        config.add("chunk-spawn-radius", 6);
        ENTITY_SPAWN_BOUNDS = config.getInt("entity-spawn-bounds");
        CHUNK_RADIUS = config.getInt("chunk-radius");
        CHUNK_TICK_RADIUS = config.getInt("chunk-tick-radius");
        CHUNK_SPAWN_RADIUS = config.getInt("chunk-spawn-radius");
        config.save();
        config.close();
    }

    private static final ExecutorService ACTIVATION_EXECUTOR = Executors.newFixedThreadPool(20);
    public static final List<ITickableTileEntity> PENDING_TILES = new ArrayList<>();
    public static final Map<Chunk, PendingChunkInfo> PENDING_CHUNKS = new HashMap<>();
    public static final Map<Chunk, WorldEntitySpawner.EntityDensityManager> CHUNK_DENSITY = new HashMap<>();

    public static void activateAll(ServerWorld world) {
        PENDING_TILES.clear();
        PENDING_CHUNKS.clear();
        CHUNK_DENSITY.clear();
        int until = MinecraftServer.currentTick + 1;
        List<Chunk> chunks = new ArrayList<>();
        Map<Chunk, PendingChunkInfo> chunksForTick = new HashMap<>();
        Collection<Organiser> organisers = ((ChunkLoaderHandlerEx) IChunkLoaderHandler.getCapability(world)).rmc$getPlayerOrganisers().values();
        if (!organisers.isEmpty()) {
            organisers.forEach((organiser) -> {
                organiser.forcedChunksByLoader.values().forEach((poses) -> {
                    poses.forEach((pos) -> {
                        ChunkMcAPI.getChunkHolder(world, pos.asLong()).ifPresent((holder) -> {
                            ChunkMcAPI.getTickingChunkNow(holder).ifPresent((ticking) -> {
                                PendingChunkInfo pci = new PendingChunkInfo();
                                pci.spawnMobs = false;
                                ChunkMcAPI.getEntityTickingChunkNow(holder).ifPresent((entityTicking) -> {
                                    pci.isAlsoEntityTicking = true;
                                    if (!chunks.contains(entityTicking)) chunks.add(entityTicking);
                                    ChunkEx ex = (ChunkEx) entityTicking;
                                    ex.rmc$tickPolicy(until, Tickable.TILE, TickPolicy.PERCENT_75);
                                    ex.rmc$tickPolicy(until, Tickable.ENTITY, TickPolicy.PERCENT_75);
                                    ex.rmc$tickUntil(until);
                                });
                                if (!chunksForTick.containsKey(ticking)) chunksForTick.put(ticking, pci);
                            });
                        });
                    });
                });
            });
        }
        List<ServerPlayerEntity> players = world.getPlayers();
        if (!players.isEmpty()) {
            players.forEach((player) -> {
                List<Chunk> densityChunks = new ArrayList<>();
                for (int shiftX = -CHUNK_TICK_RADIUS; shiftX <= CHUNK_TICK_RADIUS; shiftX++) {
                    for (int shiftZ = -CHUNK_TICK_RADIUS; shiftZ <= CHUNK_TICK_RADIUS; shiftZ++) {
                        int absShiftX = Math.abs(shiftX);
                        int absShiftZ = Math.abs(shiftZ);
                        ChunkMcAPI.getChunkHolder(world, player.chunkCoordX + shiftX, player.chunkCoordZ + shiftZ).ifPresent((holder) -> {
                            ChunkMcAPI.getTickingChunkNow(holder).ifPresent((ticking) -> {
                                PendingChunkInfo pci = new PendingChunkInfo();
                                pci.spawnMobs = absShiftX <= CHUNK_SPAWN_RADIUS && absShiftZ <= CHUNK_SPAWN_RADIUS;
                                ChunkMcAPI.getEntityTickingChunkNow(holder).ifPresent((entityTicking) -> {
                                    pci.isAlsoEntityTicking = true;
                                    if (pci.spawnMobs) densityChunks.add(entityTicking);
                                    if (absShiftX <= CHUNK_RADIUS && absShiftZ <= CHUNK_RADIUS) {
                                        if (!chunks.contains(entityTicking)) chunks.add(entityTicking);
                                        ChunkEx ex = (ChunkEx) entityTicking;
                                        if (absShiftX < CHUNK_RADIUS && absShiftZ < CHUNK_RADIUS) {
                                            ex.rmc$tickPolicy(until, Tickable.TILE, TickPolicy.PERCENT_100);
                                            ex.rmc$tickPolicy(until, Tickable.ENTITY, TickPolicy.PERCENT_100);
                                        }
                                        else {
                                            ex.rmc$tickPolicy(until, Tickable.TILE, TickPolicy.PERCENT_50);
                                            ex.rmc$tickPolicy(until, Tickable.ENTITY, TickPolicy.PERCENT_50);
                                        }
                                        ex.rmc$tickUntil(until);
                                    }
                                });
                                if (!chunksForTick.containsKey(ticking)) chunksForTick.put(ticking, pci);
                            });
                        });
                    }
                }
                if (!densityChunks.isEmpty()) {
                    WorldEntitySpawner.EntityDensityManager density = WorldEntitySpawner.func_234964_a_(densityChunks.size(), new ZoneIterable(densityChunks), (cPos, action) -> {
                        ChunkMcAPI.getBorderChunkNow(world, cPos).ifPresent(action);
                    });
                    densityChunks.forEach(chunk -> {
                        if (!CHUNK_DENSITY.containsKey(chunk)) {
                            ((ChunkEx) chunk).rmc$setClosestPlayerY((int) player.getPosY());
                            CHUNK_DENSITY.put(chunk, density);
                        }
                    });
                }
            });
        }
        if (!chunks.isEmpty()) {
            List<CompletableFuture<List<ITickableTileEntity>>> toComplete = new ArrayList<>();
            chunks.forEach((chunk) -> {
                toComplete.add(CompletableFuture.supplyAsync(() -> {
                    return handleTiles(chunk);
                }, ACTIVATION_EXECUTOR));
            });
            toComplete.forEach((future) -> PENDING_TILES.addAll(future.join()));
        }
        if (!chunksForTick.isEmpty()) {
            PENDING_CHUNKS.putAll(chunksForTick);
        }
    }

    public static boolean canTick(ServerWorld world, BlockPos pos, Tickable tickable) {
        Optional<Chunk> optChunkMc = ChunkMcAPI.getEntityTickingChunkNow(world, pos.getX() >> 4, pos.getZ() >> 4);
        return optChunkMc.isPresent()
            && ((ChunkEx) optChunkMc.get()).rmc$canTick(tickable);
    }

    private static List<ITickableTileEntity> handleTiles(Chunk chunk) {
        List<ITickableTileEntity> out = new ArrayList<>();
        chunk.getTileEntityMap().values().forEach((tile) -> {
            if (tile instanceof ITickableTileEntity
             && !((TileEntityEx) tile).rmc$isNonTickable()
             && ((ChunkEx) chunk).rmc$canTick(Tickable.TILE)) {
                out.add((ITickableTileEntity) tile);
            }
        });
        return out;
    }

}
