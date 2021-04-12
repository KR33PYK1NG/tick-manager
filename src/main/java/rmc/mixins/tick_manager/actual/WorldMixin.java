package rmc.mixins.tick_manager.actual;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import rmc.mixins.tick_manager.TickManager;
import rmc.mixins.tick_manager.Tickable;

/**
 * Developed by RMC Team, 2021
 */
@Mixin(value = World.class,
       priority = 1100)
public abstract class WorldMixin {

    @Redirect(method = "Lnet/minecraft/world/World;tickBlockEntities()V",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/tileentity/ITickableTileEntity;tick()V"))
    private void checkTileActivation(ITickableTileEntity tickTile) {
        if (this.rmc$canTileTick((TileEntity) tickTile)) {
            tickTile.tick();
        }
    }

    private boolean rmc$canTileTick(TileEntity tile) {
        Object hack = this;
        return TickManager.canTick((ServerWorld) hack, tile.getPos(), Tickable.TILE);
    }

}