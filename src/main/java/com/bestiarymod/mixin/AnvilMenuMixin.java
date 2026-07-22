package com.bestiarymod.mixin;

import com.bestiarymod.item.EnderPearlUpgradeItem;
import com.bestiarymod.item.TpWandItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilMenu.class)
public class AnvilMenuMixin {

    @Shadow private DataSlot cost;

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    private void onCustomRecipe(CallbackInfo ci) {
        AnvilMenu self = (AnvilMenu) (Object) this;
        ItemStack left = self.getSlot(0).getItem();
        ItemStack right = self.getSlot(1).getItem();

        if (left.isEmpty() || right.isEmpty()) return;
        if (!(left.getItem() instanceof TpWandItem wand)) return;
        if (!(right.getItem() instanceof EnderPearlUpgradeItem)) return;

        int currentTier = getTier(wand);
        if (currentTier >= 5) return;

        Identifier nextId;
        if (currentTier == 0) {
            nextId = Identifier.fromNamespaceAndPath("extremo", "tp_wand_1");
        } else {
            nextId = Identifier.fromNamespaceAndPath("extremo", "tp_wand_" + (currentTier + 1));
        }
        Item nextWand = BuiltInRegistries.ITEM.get(nextId).map(holder -> holder.value()).orElse(null);
        if (nextWand == null) return;

        ItemStack result = new ItemStack(nextWand, 1);
        self.getSlot(2).set(result);
        this.cost.set(30);
        ci.cancel();
    }

    private static int getTier(TpWandItem wand) {
        Identifier id = BuiltInRegistries.ITEM.getKey(wand);
        String path = id.getPath();
        if (path.equals("tp_wand")) return 0;
        if (path.startsWith("tp_wand_")) {
            return Integer.parseInt(path.substring(8));
        }
        return -1;
    }
}
