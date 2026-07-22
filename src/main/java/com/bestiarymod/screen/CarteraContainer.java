package com.bestiarymod.screen;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import java.util.ArrayList;
import java.util.List;

public class CarteraContainer extends SimpleContainer {
    private final ItemStack carteraStack;

    public CarteraContainer(ItemStack carteraStack) {
        super(9);
        this.carteraStack = carteraStack;
        loadFromComponent();
    }

    private void loadFromComponent() {
        ItemContainerContents contents = carteraStack.get(DataComponents.CONTAINER);
        if (contents != null) {
            var items = NonNullList.withSize(9, ItemStack.EMPTY);
            contents.copyInto(items);
            for (int i = 0; i < 9; i++) {
                setItem(i, items.get(i));
            }
        }
    }

    @Override
    public void stopOpen(ContainerUser user) {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            items.add(getItem(i));
        }
        carteraStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
        super.stopOpen(user);
    }
}
