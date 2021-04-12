package rmc.mixins.tick_manager.extend;

import net.minecraft.world.server.ChunkHolder;

/**
 * Developed by RMC Team, 2021
 */
public interface ServerChunkProviderEx {

    public ChunkHolder rmc$getChunkHolder(long cPos);

}