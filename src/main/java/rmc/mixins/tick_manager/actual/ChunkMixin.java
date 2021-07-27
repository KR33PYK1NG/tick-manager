package rmc.mixins.tick_manager.actual;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.chunk.Chunk;
import rmc.mixins.tick_manager.TickPolicy;
import rmc.mixins.tick_manager.Tickable;
import rmc.mixins.tick_manager.extend.ChunkEx;

/**
 * Developed by RMC Team, 2021
 */
@Mixin(value = Chunk.class)
public abstract class ChunkMixin
implements ChunkEx {

    private static final Random rmc$SHIFT_RANDOM = new Random();
    private final int rmc$shift = rmc$SHIFT_RANDOM.nextInt(100000);
    private final TickPolicy[] rmc$policies = new TickPolicy[Tickable.values().length];
    private int rmc$until = Integer.MIN_VALUE;
    private int rmc$closestPlayerY = -1;

    @Override
    public boolean rmc$canTick(Tickable tickable) {
        TickPolicy policy;
        return MinecraftServer.currentTick < this.rmc$until
            && (policy = this.rmc$policies[tickable.ordinal()]) != null
            && policy.canTick(this.rmc$shift);
    }

    @Override
    public void rmc$tickPolicy(int until, Tickable tickable, TickPolicy policy) {
        TickPolicy old;
        if (until != this.rmc$until
         || (old = this.rmc$policies[tickable.ordinal()]) == null
         || old.canOverrideWith(policy)) {
            this.rmc$policies[tickable.ordinal()] = policy;
        }
    }

    @Override
    public void rmc$tickUntil(int until) {
        this.rmc$until = until;
    }

    @Override
    public int rmc$getClosestPlayerY() {
        return this.rmc$closestPlayerY;
    }

    @Override
    public void rmc$setClosestPlayerY(int y) {
        this.rmc$closestPlayerY = y;
    }

}
