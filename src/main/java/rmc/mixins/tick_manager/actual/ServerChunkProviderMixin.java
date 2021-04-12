package rmc.mixins.tick_manager.actual;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ServerChunkProvider;
import rmc.mixins.tick_manager.extend.ServerChunkProviderEx;

/**
 * Developed by RMC Team, 2021
 */
@Mixin(value = ServerChunkProvider.class)
public abstract class ServerChunkProviderMixin
implements ServerChunkProviderEx {

    @Shadow private ChunkHolder func_217213_a(long chunkPosIn) { return null; }

    @Override
    public ChunkHolder rmc$getChunkHolder(long cPos) {
        return this.func_217213_a(cPos);
    }

}