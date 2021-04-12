package rmc.mixins.tick_manager.actual;

import org.spigotmc.ActivationRange;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

/**
 * Developed by RMC Team, 2021
 */
@Mixin(value = ActivationRange.class)
public abstract class ActivationRangeMixin {

    @Overwrite
    public static void activateEntities(World world) {}

    @Overwrite
    public static boolean checkIfActive(Entity entity) {
        return true;
    }

}