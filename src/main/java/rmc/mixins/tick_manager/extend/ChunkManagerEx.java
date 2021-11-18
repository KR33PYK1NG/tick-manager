package rmc.mixins.tick_manager.extend;

import net.minecraft.world.server.ChunkHolder;

/**
 * Developed by RMC Team, 2021
 */
public interface ChunkManagerEx {

    public void rmc$tickEntityTracker();

    public ChunkHolder rmc$getChunkHolder(long l);

}
