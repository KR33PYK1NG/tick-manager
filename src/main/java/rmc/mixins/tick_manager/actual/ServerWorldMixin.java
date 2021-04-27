package rmc.mixins.tick_manager.actual;

import java.lang.reflect.Field;
import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.world.server.ServerWorld;
import rmc.mixins.tick_manager.TickManager;
import rmc.mixins.tick_manager.Tickable;

/**
 * Developed by RMC Team, 2021
 */
@Mixin(value = ServerWorld.class)
public abstract class ServerWorldMixin {

    private static final Field rmc$CHUNK_CACHE;

    static {
        Field chunkCache = null;
        try {
            for (Field field : LivingEntity.class.getDeclaredFields()) {
                if (field.getName().equals("chunkCache")) {
                    chunkCache = field;
                    break;
                }
            }
            if (chunkCache != null)
                chunkCache.setAccessible(true);
            else
                System.out.println("TickManager wasn't able to find Performant! Chunk Cache cleanup is disabled.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        rmc$CHUNK_CACHE = chunkCache;
    }

    @Inject(method = "Lnet/minecraft/world/server/ServerWorld;tick(Ljava/util/function/BooleanSupplier;)V",
            at = @At(value = "HEAD"))
    private void activateTickManager(CallbackInfo mixin) {
        Object hack = this;
        TickManager.activateAll((ServerWorld) hack);
    }

    @Redirect(method = "Lnet/minecraft/world/server/ServerWorld;tick(Ljava/util/function/BooleanSupplier;)V",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/server/ServerWorld;guardEntityTick(Ljava/util/function/Consumer;Lnet/minecraft/entity/Entity;)V"))
    private void checkEntityActivation(ServerWorld world, Consumer<Entity> consumer, Entity entity) {
        if (this.rmc$canEntityTick(entity) || this.rmc$canEntityOverride(entity)) {
            world.guardEntityTick(consumer, entity);
        }
        else {
            if (rmc$CHUNK_CACHE != null && entity instanceof LivingEntity) {
                try {
                    rmc$CHUNK_CACHE.set(entity, null);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private boolean rmc$canEntityTick(Entity entity) {
        Object hack = this;
        return TickManager.canTick((ServerWorld) hack, entity.getPosition(), Tickable.ENTITY);
    }

    private boolean rmc$canEntityOverride(Entity entity) {
        return (!entity.isOnGround() && !entity.isInWater())
            || (entity.getType().getClassification() == EntityClassification.MISC && !(entity instanceof ItemEntity) && !(entity instanceof VillagerEntity));
    }

}