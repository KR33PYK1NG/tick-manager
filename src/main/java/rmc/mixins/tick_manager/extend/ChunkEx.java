package rmc.mixins.tick_manager.extend;

import rmc.mixins.tick_manager.TickPolicy;
import rmc.mixins.tick_manager.Tickable;

/**
 * Developed by RMC Team, 2021
 */
public interface ChunkEx {

    public boolean rmc$canTick   (Tickable tickable);
    public void    rmc$tickPolicy(int until, Tickable tickable, TickPolicy policy);
    public void    rmc$tickUntil (int until);

    public int  rmc$getClosestPlayerY();
    public void rmc$setClosestPlayerY(int y);

}
