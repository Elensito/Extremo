package com.bestiarymod.mixin;

import com.bestiarymod.access.ConsumableDataAccessor;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import java.util.HashSet;
import java.util.Set;

@Mixin(Player.class)
public abstract class PlayerConsumableMixin implements ConsumableDataAccessor {
    @Unique
    private final Set<String> extremoConsumedItems = new HashSet<>();

    @Override
    public Set<String> getConsumedItems() {
        return extremoConsumedItems;
    }

    @Override
    public void setConsumedItems(Set<String> items) {
        extremoConsumedItems.clear();
        extremoConsumedItems.addAll(items);
    }
}
