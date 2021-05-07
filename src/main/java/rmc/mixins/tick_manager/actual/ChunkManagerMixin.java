package rmc.mixins.tick_manager.actual;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.world.server.ChunkManager;
import rmc.mixins.tick_manager.extend.ChunkManagerEx;

/**
 * Developed by RMC Team, 2021
 */
@Mixin(value = ChunkManager.class)
public abstract class ChunkManagerMixin
implements ChunkManagerEx {

    @Shadow protected void tickEntityTracker() {}

    @Override
    public void rmc$tickEntityTracker() {
        this.tickEntityTracker();
    }

}