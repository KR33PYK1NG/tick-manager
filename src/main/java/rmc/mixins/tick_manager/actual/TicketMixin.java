package rmc.mixins.tick_manager.actual;

import net.minecraft.world.server.Ticket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import rmc.mixins.tick_manager.extend.TicketEx;

/**
 * Разработано командой RMC, 2021
 */
@Mixin(Ticket.class)
public abstract class TicketMixin implements TicketEx {

    @Shadow
    protected void setTimestamp(long p_229861_1_) {}

    @Shadow
    protected boolean isExpired(long currentTime) { return false; }

    @Override
    public void rmc$setTimestamp(long timeout) {
        this.setTimestamp(timeout);
    }

    @Override
    public boolean rmc$isExpired(long currentTime) {
        return this.isExpired(currentTime);
    }

}
