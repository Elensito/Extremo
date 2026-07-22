package com.bestiarymod.mixin;

import com.bestiarymod.Extremo;
import com.bestiarymod.spawn.SpawnConfigManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityType.class)
public class EntityTypeMixin {

    @Inject(method = "create", at = @At("RETURN"))
    private void onEntityCreate(Level level, CallbackInfoReturnable<Entity> cir) {
        Entity entity = cir.getReturnValue();
        if (entity != null) {
            Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
            if (id != null && id.getNamespace().equals("extremo")) {
                StackTraceElement[] stack = Thread.currentThread().getStackTrace();
                String caller = stack.length > 3 ? stack[3].getClassName() + "." + stack[3].getMethodName() : "unknown";
                Extremo.LOGGER.info("[EntityCreate] Created {} at {}, caller={}", id, entity.blockPosition(), caller);
            }
        }
    }
}
