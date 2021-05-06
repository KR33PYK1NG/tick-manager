package rmc.mixins.tick_manager.actual;

import java.util.List;
import java.util.Set;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.mohistmc.api.mc.ChunkMcAPI;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import rmc.mixins.tick_manager.TickManager;

/**
 * Developed by RMC Team, 2021
 */
@Mixin(value = World.class)
public abstract class WorldMixin {

    @Shadow protected boolean processingLoadedTiles;
    @Shadow @Final public List<TileEntity> loadedTileEntityList;
    @Shadow @Final public List<TileEntity> tickableTileEntities;
    @Shadow @Final protected Set<TileEntity> tileEntitiesToBeRemoved;
    @Shadow @Final protected List<TileEntity> addedTileEntityList;

    @Overwrite
    public void tickBlockEntities() {
        this.loadedTileEntityList.clear();
        this.tickableTileEntities.clear();
        this.processingLoadedTiles = true;
        this.rmc$processRemovedTiles();
        this.rmc$processTickableTiles();
        this.processingLoadedTiles = false;
        this.rmc$processAddedTiles();
    }

    private void rmc$processRemovedTiles() {
        if (!this.tileEntitiesToBeRemoved.isEmpty()) {
            this.tileEntitiesToBeRemoved.forEach((tile) -> tile.onChunkUnloaded());
            this.tileEntitiesToBeRemoved.clear();
        }
    }

    private void rmc$processTickableTiles() {
        if (!TickManager.PENDING_TILES.isEmpty()) {
            TickManager.PENDING_TILES.forEach((tile) -> tile.tick());
        }
    }

    private void rmc$processAddedTiles() {
        if (!this.addedTileEntityList.isEmpty()) {
            Object hack = this;
            this.addedTileEntityList.forEach((tile) -> {
                BlockPos pos = tile.getPos();
                ChunkMcAPI.getBorderChunkNow((ServerWorld) hack, pos.getX() >> 4, pos.getZ() >> 4).ifPresent((chunkMc) -> {
                    chunkMc.addTileEntity(pos, tile);
                });
            });
            this.addedTileEntityList.clear();
        }
    }

}