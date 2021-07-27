package rmc.mixins.tick_manager.actual;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.spawner.WorldEntitySpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import rmc.mixins.tick_manager.TickManager;
import rmc.mixins.tick_manager.extend.ChunkEx;

import java.util.Random;

/**
 * Developed by RMC Team, 2021
 * @author KR33PY
 */
@Mixin(value = WorldEntitySpawner.class)
public abstract class WorldEntitySpawnerMixin {

    /* @Overwrite
    private static BlockPos getRandomHeight(World world, Chunk chunk) {
        int pY = ((ChunkEx) chunk).rmc$getClosestPlayerY();
        if (pY - TickManager.ENTITY_SPAWN_BOUNDS < 0) pY = TickManager.ENTITY_SPAWN_BOUNDS;
        else if (pY + TickManager.ENTITY_SPAWN_BOUNDS > 255) pY = 255 - TickManager.ENTITY_SPAWN_BOUNDS;
        // TODO 1.17 ^^^
        int topY = pY + TickManager.ENTITY_SPAWN_BOUNDS;
        int botY = pY - TickManager.ENTITY_SPAWN_BOUNDS;
        return new BlockPos(chunk.getPos().getXStart() + world.rand.nextInt(16),
                world.rand.nextInt((topY - botY) + 1) + botY,
                chunk.getPos().getZStart() + world.rand.nextInt(16));
    } */

}
