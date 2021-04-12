package rmc.mixins.tick_manager;

import net.minecraft.server.MinecraftServer;

/**
 * Developed by RMC Team, 2021
 */
public class TickPolicy {

    public static final TickPolicy PERCENT_100 = new TickPolicy(100, 1, false);
    public static final TickPolicy PERCENT_50 = new TickPolicy(50, 2, false);

    public boolean canTick(int shift) {
        int res = (MinecraftServer.currentTick + shift) % this.every;
        return this.skip ? res != 0 : res == 0;
    }

    public boolean canOverrideWith(TickPolicy policy) {
        return policy.priority > this.priority;
    }

    private final int priority;
    private final int every;
    private final boolean skip;

    private TickPolicy(int priority, int every, boolean skip) {
        this.priority = priority;
        this.every = every;
        this.skip = skip;
    }

}