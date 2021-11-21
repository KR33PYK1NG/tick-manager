package rmc.mixins.tick_manager.extend;

/**
 * Разработано командой RMC, 2021
 */
public interface TicketEx {

    public void rmc$setTimestamp(long timeout);

    public boolean rmc$isExpired(long currentTime);

}
