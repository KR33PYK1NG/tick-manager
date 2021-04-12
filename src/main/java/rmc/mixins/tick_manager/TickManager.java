package rmc.mixins.tick_manager;

import java.util.Collection;
import java.util.List;

import codechicken.chunkloader.api.IChunkLoaderHandler;
import codechicken.chunkloader.world.Organiser;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ServerWorld;
import rmc.mixins.tick_manager.extend.ChunkEx;
import rmc.mixins.tick_manager.extend.ChunkLoaderHandlerEx;
import rmc.mixins.tick_manager.extend.ServerChunkProviderEx;

/**
 * Developed by RMC Team, 2021
 */
public abstract class TickManager {

    public static void activateAll(ServerWorld world) {
        int until = MinecraftServer.currentTick + 1;
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
                                chunk.rmc$tickPolicy(until, Tickable.TILE, TickPolicy.PERCENT_100);
                                chunk.rmc$tickPolicy(until, Tickable.ENTITY, TickPolicy.PERCENT_100);
                                chunk.rmc$tickUntil(until);
                            }
                            else {
                                chunk.rmc$tickPolicy(until, Tickable.TILE, TickPolicy.PERCENT_50);
                                chunk.rmc$tickPolicy(until, Tickable.ENTITY, TickPolicy.PERCENT_50);
                                chunk.rmc$tickUntil(until);
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

    private static ChunkEx getChunkEx(ServerWorld world, int cX, int cZ) {
        return getChunkEx(world, new ChunkPos(cX, cZ).asLong());
    }

    private static ChunkEx getChunkEx(ServerWorld world, long cPos) {
        ChunkHolder holder = ((ServerChunkProviderEx) world.getChunkProvider()).rmc$getChunkHolder(cPos);
        if (holder == null) return null;
        return (ChunkEx) holder.getFullChunk();
    }

}