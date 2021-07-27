package rmc.mixins.tick_manager.actual;

import net.minecraft.entity.Entity;
import net.minecraft.util.ClassInheritanceMultiMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import rmc.mixins.tick_manager.extend.ClassInheritanceMultiMapEx;

import java.util.List;

/**
 * Developed by RMC Team, 2021
 * @author KR33PY
 */
@Mixin(value = ClassInheritanceMultiMap.class)
public abstract class ClassInheritanceMultiMapMixin implements ClassInheritanceMultiMapEx {

    @Shadow @Final
    private List<Entity> values;

    @Override
    public Entity rmc$getEntity(int index) {
        return this.values.get(index);
    }

}
