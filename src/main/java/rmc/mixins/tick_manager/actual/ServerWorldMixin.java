package rmc.mixins.tick_manager.actual;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
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