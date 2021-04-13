package rmc.mixins.tick_manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

    public static final List<ITickableTileEntity> PENDING_TILES = new ArrayList<>();

    public static void activateAll(ServerWorld world) {
        PENDING_TILES.clear();
        int until = MinecraftServer.currentTick + 1;
        Collection<Organiser> organisers = ((ChunkLoaderHandlerEx) IChunkLoaderHandler.getCapability(world)).rmc$getPlayerOrganisers().values();
        if (!organisers.isEmpty()) {
            organisers.forEach((organiser) -> {
                organiser.forcedChunksByLoader.values().forEach((poses) -> {
                    poses.forEach((pos) -> {
                        ChunkEx chunk = getChunkEx(world, pos.asLong());
                        if (chunk != null) {
                            handleChunk(chunk, until, TickPolicy.PERCENT_100);
                        }
                    });
                });
            });
        }
        List<ServerPlayerEntity> players = world.getPlayers();
        if (!players.isEmpty()) {
            int radius = 1;
            players.forEach((player) -> {
                for (int shiftX = -radius; shiftX <= radius; shiftX++) {
                    for (int shiftZ = -radius; shiftZ <= radius; shiftZ++) {
                        ChunkEx chunk = getChunkEx(world, player.chunkCoordX + shiftX, player.chunkCoordZ + shiftZ);
                        if (chunk != null) {
                            int absShiftX = Math.abs(shiftX);
                            int absShiftZ = Math.abs(shiftZ);
                            if (absShiftX < radius && absShiftZ < radius) {
                                handleChunk(chunk, until, TickPolicy.PERCENT_100);
                            }
                            else {
                                handleChunk(chunk, until, TickPolicy.PERCENT_50);
                            }
                        }
                    }
                }
            });
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

    private static void handleChunk(ChunkEx chunk, int until, TickPolicy policy) {
        chunk.rmc$tickPolicy(until, Tickable.TILE, policy);
        chunk.rmc$tickPolicy(until, Tickable.ENTITY, policy);
        chunk.rmc$tickUntil(until);
        ((Chunk) chunk).getTileEntityMap().values().forEach((tile) -> {
            if (tile instanceof ITickableTileEntity
             && !((TileEntityEx) tile).rmc$isNonTickable()
             && chunk.rmc$canTick(Tickable.TILE)) {
                PENDING_TILES.add((ITickableTileEntity) tile);
            }
        });
    }

}