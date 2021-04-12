package rmc.mixins.tick_manager.extend;

/**
 * Developed by RMC Team, 2021
 */
public interface TileEntityEx {

    public boolean rmc$isNonTickable ();
    public void    rmc$setNonTickable(boolean noTick);

}