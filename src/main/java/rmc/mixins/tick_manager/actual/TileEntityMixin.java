package rmc.mixins.tick_manager.actual;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.tileentity.TileEntity;
import rmc.mixins.tick_manager.extend.TileEntityEx;

/**
 * Developed by RMC Team, 2021
 */
@Mixin(value = TileEntity.class)
public abstract class TileEntityMixin
implements TileEntityEx {

    private boolean rmc$noTick;

    @Override
    public boolean rmc$isNonTickable() {
        return this.rmc$noTick;
    }

    @Override
    public void rmc$setNonTickable(boolean noTick) {
        this.rmc$noTick = noTick;
    }

}