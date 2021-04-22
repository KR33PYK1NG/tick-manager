package rmc.mixins.tick_manager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.electronwill.nightconfig.core.file.FileConfig;

import codechicken.chunkloader.api.IChunkLoaderHandler;
import codechicken.chunkloader.world.Organiser;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ServerWorld;
import rmc.mixins.tick_manager.extend.ChunkEx;
import rmc.mixins.tick_manager.extend.ChunkLoaderHandlerEx;
import rmc.mixins.tick_manager.extend.ServerChunkProviderEx;
import rmc.mixins.tick_manager.extend.TileEntityEx;

/**
 * Developed by RMC Team, 2021
 */
public abstract class TickManager {

    private static final int CHUNK_RADIUS;

    static {
        File cfgfile = new File("mixincfg/tick-manager.toml");
        cfgfile.getParentFile().mkdir();
        FileConfig config = FileConfig.of(cfgfile);
        config.load();
        config.add("chunk-radius", 1);
        CHUNK_RADIUS = config.getInt("chunk-radius");
        config.save();
        config.close();
    }

    private static final ExecutorService ACTIVATION_EXECUTOR = Executors.newFixedThreadPool(20);
    public static final List<ITickableTileEntity> PENDING_TILES = new ArrayList<>();

    public static void activateAll(ServerWorld world) {
        PENDING_TILES.clear();
        int until = MinecraftServer.currentTick + 1;
        List<Chunk> chunks = new ArrayList<>();
        Collection<Organiser> organisers = ((ChunkLoaderHandlerEx) IChunkLoaderHandler.getCapability(world)).rmc$getPlayerOrganisers().values();
        if (!organisers.isEmpty()) {
            organisers.forEach((organiser) -> {
                organiser.forcedChunksByLoader.values().forEach((poses) -> {
                    poses.forEach((pos) -> {
                        ChunkEx chunk = getChunkEx(world, pos.asLong());
                        if (chunk != null) {
                            chunk.rmc$tickPolicy(until, Tickable.TILE, TickPolicy.PERCENT_100);
                            chunk.rmc$tickPolicy(until, Tickable.ENTITY, TickPolicy.PERCENT_100);
                            chunk.rmc$tickUntil(until);
                            if (!chunks.contains((Chunk) chunk)) chunks.add((Chunk) chunk);
                        }
                    });
                });
            });
        }
        List<ServerPlayerEntity> players = world.getPlayers();
        if (!players.isEmpty()) {
            players.forEach((player) -> {
                for (int shiftX = -CHUNK_RADIUS; shiftX <= CHUNK_RADIUS; shiftX++) {
                    for (int shiftZ = -CHUNK_RADIUS; shiftZ <= CHUNK_RADIUS; shiftZ++) {
                        ChunkEx chunk = getChunkEx(world, player.chunkCoordX + shiftX, player.chunkCoordZ + shiftZ);
                        if (chunk != null) {
                            int absShiftX = Math.abs(shiftX);
                            int absShiftZ = Math.abs(shiftZ);
                            if (absShiftX < CHUNK_RADIUS && absShiftZ < CHUNK_RADIUS) {
                                chunk.rmc$tickPolicy(until, Tickable.TILE, TickPolicy.PERCENT_100);
                                chunk.rmc$tickPolicy(until, Tickable.ENTITY, TickPolicy.PERCENT_100);
                                chunk.rmc$tickUntil(until);
                                if (!chunks.contains((Chunk) chunk)) chunks.add((Chunk) chunk);
                            }
                            else {
                                chunk.rmc$tickPolicy(until, Tickable.TILE, TickPolicy.PERCENT_50);
                                chunk.rmc$tickPolicy(until, Tickable.ENTITY, TickPolicy.PERCENT_50);
                                chunk.rmc$tickUntil(until);
                                if (!chunks.contains((Chunk) chunk)) chunks.add((Chunk) chunk);
                            }
                        }
                    }
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
    }

    public static boolean canTick(ServerWorld world, BlockPos pos, Tickable tickable) {
        ChunkEx chunk;
        return (chunk = getChunkEx(world, pos.getX() >> 4, pos.getZ() >> 4)) != null
            && chunk.rmc$canTick(tickable);
    }

    public static ChunkEx getChunkEx(ServerWorld world, int cX, int cZ) {
        return getChunkEx(world, new ChunkPos(cX, cZ).asLong());
    }

    private static ChunkEx getChunkEx(ServerWorld world, long cPos) {
        ChunkHolder holder = ((ServerChunkProviderEx) world.getChunkProvider()).rmc$getChunkHolder(cPos);
        if (holder == null) return null;
        return (ChunkEx) holder.getFullChunk();
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