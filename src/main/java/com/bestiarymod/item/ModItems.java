package com.bestiarymod.item;

import java.util.List;
import com.bestiarymod.Extremo;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemLore;

public class ModItems {
    public static final Identifier TP_WAND_ID = Identifier.fromNamespaceAndPath(Extremo.MOD_ID, "tp_wand");
    public static final Identifier TP_WAND_1_ID = Identifier.fromNamespaceAndPath(Extremo.MOD_ID, "tp_wand_1");
    public static final Identifier TP_WAND_2_ID = Identifier.fromNamespaceAndPath(Extremo.MOD_ID, "tp_wand_2");
    public static final Identifier TP_WAND_3_ID = Identifier.fromNamespaceAndPath(Extremo.MOD_ID, "tp_wand_3");
    public static final Identifier TP_WAND_4_ID = Identifier.fromNamespaceAndPath(Extremo.MOD_ID, "tp_wand_4");
    public static final Identifier TP_WAND_5_ID = Identifier.fromNamespaceAndPath(Extremo.MOD_ID, "tp_wand_5");
    public static final Identifier ENDER_PEARL_UPGRADE_ID = Identifier.fromNamespaceAndPath(Extremo.MOD_ID, "ender_pearl_upgrade");

    private static final int GRAY = 0xAAAAAA;

    private static Item coin(String display, int color, Identifier id) {
        return Registry.register(
            BuiltInRegistries.ITEM,
            id,
            new CoinItem(new Item.Properties()
                .stacksTo(64)
                .setId(ResourceKey.create(Registries.ITEM, id))
                .component(DataComponents.ITEM_MODEL, id)
            , Component.literal(display).withStyle(style -> style.withColor(color)))
        );
    }

    private static Item buildWand(Identifier id, int tier) {
        int range = 4 + tier;
        int cooldown = 400 - tier * 20;

        List<Component> lore;
        if (tier == 5) {
            lore = List.of(
                Component.literal("Teletransporta " + range + " bloques hacia donde miras").withStyle(style -> style.withColor(GRAY)),
                Component.literal("Cooldown: " + (cooldown / 20) + " segundos").withStyle(style -> style.withColor(0x55FF55)),
                Component.literal("").withStyle(style -> style.withColor(GRAY)),
                Component.literal("\u00a1Poder m\u00e1ximo alcanzado!").withStyle(style -> style.withColor(0xFFAA00))
            );
        } else {
            lore = List.of(
                Component.literal("Teletransporta " + range + " bloques hacia donde miras").withStyle(style -> style.withColor(GRAY)),
                Component.literal("Cooldown: " + (cooldown / 20) + " segundos").withStyle(style -> style.withColor(tier == 0 ? 0xFF5555 : 0x55FF55)),
                Component.literal("").withStyle(style -> style.withColor(GRAY)),
                Component.literal("Su poder yace latente...").withStyle(style -> style.withColor(GRAY))
            );
        }

        return Registry.register(
            BuiltInRegistries.ITEM,
            id,
            new TpWandItem(new Item.Properties()
                .stacksTo(1)
                .setId(ResourceKey.create(Registries.ITEM, id))
                .component(DataComponents.ITEM_MODEL, TP_WAND_ID)
                .component(DataComponents.LORE, new ItemLore(lore))
            , range, cooldown, tier)
        );
    }

    public static final Item TP_WAND = buildWand(TP_WAND_ID, 0);
    public static final Item TP_WAND_1 = buildWand(TP_WAND_1_ID, 1);
    public static final Item TP_WAND_2 = buildWand(TP_WAND_2_ID, 2);
    public static final Item TP_WAND_3 = buildWand(TP_WAND_3_ID, 3);
    public static final Item TP_WAND_4 = buildWand(TP_WAND_4_ID, 4);
    public static final Item TP_WAND_5 = buildWand(TP_WAND_5_ID, 5);

    public static final Identifier CARTERA_ID = Identifier.fromNamespaceAndPath(Extremo.MOD_ID, "cartera");
    public static final Item CARTERA = Registry.register(
        BuiltInRegistries.ITEM,
        CARTERA_ID,
        new CarteraItem(new Item.Properties()
            .stacksTo(1)
            .setId(ResourceKey.create(Registries.ITEM, CARTERA_ID))
            .component(DataComponents.ITEM_MODEL, CARTERA_ID)
        )
    );

    public static final Identifier EXTREME_HEART_ID = Identifier.fromNamespaceAndPath(Extremo.MOD_ID, "extreme_heart");
    public static final Item EXTREME_HEART = Registry.register(
        BuiltInRegistries.ITEM,
        EXTREME_HEART_ID,
        new ExtremeHeartItem(new Item.Properties()
            .stacksTo(16)
            .setId(ResourceKey.create(Registries.ITEM, EXTREME_HEART_ID))
            .component(DataComponents.ITEM_MODEL, EXTREME_HEART_ID)
        )
    );

    public static final Identifier ENCHANTED_IRON_INGOT_ID = Identifier.fromNamespaceAndPath(Extremo.MOD_ID, "enchanted_iron_ingot");
    public static final Item ENCHANTED_IRON_INGOT = Registry.register(
        BuiltInRegistries.ITEM,
        ENCHANTED_IRON_INGOT_ID,
        new EnchantedIronIngotItem(new Item.Properties()
            .stacksTo(1)
            .setId(ResourceKey.create(Registries.ITEM, ENCHANTED_IRON_INGOT_ID))
            .component(DataComponents.ITEM_MODEL, ENCHANTED_IRON_INGOT_ID)
            .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
        )
    );

    public static final Identifier LIFE_HEART_ID = Identifier.fromNamespaceAndPath(Extremo.MOD_ID, "life_heart");
    public static final Item LIFE_HEART = Registry.register(
        BuiltInRegistries.ITEM,
        LIFE_HEART_ID,
        new LifeHeartItem(new Item.Properties()
            .stacksTo(1)
            .setId(ResourceKey.create(Registries.ITEM, LIFE_HEART_ID))
            .component(DataComponents.ITEM_MODEL, LIFE_HEART_ID)
        )
    );

    public static final Identifier TP_COOKIE_ID = Identifier.fromNamespaceAndPath(Extremo.MOD_ID, "tp_cookie");
    public static final Item TP_COOKIE = Registry.register(
        BuiltInRegistries.ITEM,
        TP_COOKIE_ID,
        new TpCookieItem(new Item.Properties()
            .stacksTo(16)
            .setId(ResourceKey.create(Registries.ITEM, TP_COOKIE_ID))
            .component(DataComponents.ITEM_MODEL, TP_COOKIE_ID)
        )
    );

    public static final Item ENDER_PEARL_UPGRADE = Registry.register(
        BuiltInRegistries.ITEM,
        ENDER_PEARL_UPGRADE_ID,
        new EnderPearlUpgradeItem(new Item.Properties()
            .stacksTo(16)
            .setId(ResourceKey.create(Registries.ITEM, ENDER_PEARL_UPGRADE_ID))
            .component(DataComponents.ITEM_MODEL, ENDER_PEARL_UPGRADE_ID)
            .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
            .component(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("Una perla imbuida con energ\u00eda del vac\u00edo.").withStyle(style -> style.withColor(GRAY)),
                Component.literal("").withStyle(style -> style.withColor(GRAY)),
                Component.literal("Mejora el Cetro Dimensional en una mesa de herrer\u00eda.").withStyle(style -> style.withColor(GRAY))
            )))
        )
    );

    private static final Identifier COIN_AMETHYST_ID = Identifier.fromNamespaceAndPath(Extremo.MOD_ID, "coin_amethyst");
    private static final Identifier COIN_COPPER_ID = Identifier.fromNamespaceAndPath(Extremo.MOD_ID, "coin_copper");
    private static final Identifier COIN_DIAMOND_ID = Identifier.fromNamespaceAndPath(Extremo.MOD_ID, "coin_diamond");
    private static final Identifier COIN_EMERALD_ID = Identifier.fromNamespaceAndPath(Extremo.MOD_ID, "coin_emerald");
    private static final Identifier COIN_GOLD_ID = Identifier.fromNamespaceAndPath(Extremo.MOD_ID, "coin_gold");
    private static final Identifier COIN_IRON_ID = Identifier.fromNamespaceAndPath(Extremo.MOD_ID, "coin_iron");
    private static final Identifier COIN_NETHERITE_ID = Identifier.fromNamespaceAndPath(Extremo.MOD_ID, "coin_netherite");
    private static final Identifier COIN_REDSTONE_ID = Identifier.fromNamespaceAndPath(Extremo.MOD_ID, "coin_redstone");

    public static final Item COIN_AMETHYST = coin("Moneda de Amatista", 0xFF69B4, COIN_AMETHYST_ID);
    public static final Item COIN_COPPER = coin("Moneda de Cobre", 0xC47A4D, COIN_COPPER_ID);
    public static final Item COIN_DIAMOND = coin("Moneda de Diamante", 0x55FFFF, COIN_DIAMOND_ID);
    public static final Item COIN_EMERALD = coin("Moneda de Esmeralda Verde", 0x55FF55, COIN_EMERALD_ID);
    public static final Item COIN_GOLD = coin("Moneda de Oro", 0xFFAA00, COIN_GOLD_ID);
    public static final Item COIN_IRON = coin("Moneda de Hierro", 0xCCCCCC, COIN_IRON_ID);
    public static final Item COIN_NETHERITE = coin("Moneda de Netherita Gris Oscuro", 0x444444, COIN_NETHERITE_ID);
    public static final Item COIN_REDSTONE = coin("Moneda de Redstone", 0xFF5555, COIN_REDSTONE_ID);

    public static void register() {
        Extremo.LOGGER.info("Registered custom items");
    }
}
