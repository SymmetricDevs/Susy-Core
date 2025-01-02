package supersymmetry.common.item;

import gregtech.api.GTValues;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.armor.ArmorMetaItem;
import gregtech.api.items.metaitem.MetaItem.MetaValueItem;
import gregtech.api.items.metaitem.MetaOreDictItem;
import gregtech.api.items.metaitem.MetaOreDictItem.OreDictValueItem;
import gregtech.api.items.metaitem.StandardMetaItem;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.items.behaviors.TooltipBehavior;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import supersymmetry.SuSyValues;

import supersymmetry.common.item.armor.SuSyMetaArmor;

import java.util.Objects;

public class SuSyMetaItems {


    private static StandardMetaItem metaItem;
    public static SuSyArmorItem armorItem;
    public static MetaOreDictItem oreDictItem;
    public static MetaValueItem CATALYST_BED_SUPPORT_GRID;
    public static MetaValueItem CONVEYOR_STEAM;
    public static MetaValueItem PUMP_STEAM;
    public static MetaValueItem AIR_VENT;
    public static MetaValueItem RESTRICTIVE_FILTER;
    public static MetaValueItem TRACK_SEGMENT;
    public static MetaValueItem EARTH_ORBITAL_SCRAP;
    public static MetaValueItem DATA_CARD;
    public static MetaValueItem DATA_CARD_ACTIVE;
    public static ArmorMetaItem<?>.ArmorMetaValueItem SIMPLE_GAS_MASK;
    public static ArmorMetaItem<?>.ArmorMetaValueItem GAS_MASK;
    public static ArmorMetaItem<?>.ArmorMetaValueItem GAS_TANK;
    public static ArmorMetaItem<?>.ArmorMetaValueItem ASBESTOS_MASK;
    public static ArmorMetaItem<?>.ArmorMetaValueItem ASBESTOS_CHESTPLATE;
    public static ArmorMetaItem<?>.ArmorMetaValueItem ASBESTOS_LEGGINGS;
    public static ArmorMetaItem<?>.ArmorMetaValueItem ASBESTOS_BOOTS;
    public static ArmorMetaItem<?>.ArmorMetaValueItem REBREATHER_TANK;
    public static ArmorMetaItem<?>.ArmorMetaValueItem REFLECTIVE_MASK;
    public static ArmorMetaItem<?>.ArmorMetaValueItem REFLECTIVE_CHESTPLATE;
    public static ArmorMetaItem<?>.ArmorMetaValueItem REFLECTIVE_LEGGINGS;
    public static ArmorMetaItem<?>.ArmorMetaValueItem REFLECTIVE_BOOTS;
    public static ArmorMetaItem<?>.ArmorMetaValueItem FILTERED_TANK;
    public static ArmorMetaItem<?>.ArmorMetaValueItem NOMEX_MASK;
    public static ArmorMetaItem<?>.ArmorMetaValueItem NOMEX_CHESTPLATE;
    public static ArmorMetaItem<?>.ArmorMetaValueItem NOMEX_LEGGINGS;
    public static ArmorMetaItem<?>.ArmorMetaValueItem NOMEX_BOOTS;

    public static ArmorMetaItem<?>.ArmorMetaValueItem JET_WINGPACK;

    public static void initMetaItems() {
        metaItem = new StandardMetaItem();
        metaItem.setRegistryName("meta_item");
        oreDictItem = new MetaOreDictItem((short) 0);
        oreDictItem.setRegistryName("susy_oredict_item");
        armorItem = new SuSyMetaArmor();
        armorItem.setRegistryName("susy_armor");
        CatalystItems.init();

    }

    public static void initSubItems() {
        initMetaItem();
        CatalystItems.initCatalysts();
    }

    private static void initMetaItem() {
        // initialize metaitems here
        CATALYST_BED_SUPPORT_GRID = metaItem.addItem(1, "catalyst_bed_support_grid");
        CONVEYOR_STEAM = metaItem.addItem(2, "conveyor.steam").addComponents(new TooltipBehavior((lines) -> {
            lines.add(I18n.format("metaitem.conveyor.module.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.item_transfer_rate", new Object[]{4}));
        }));
        PUMP_STEAM = metaItem.addItem(3, "pump.steam").addComponents(new TooltipBehavior((lines) -> {
            lines.add(I18n.format("metaitem.electric.pump.tooltip"));
            lines.add(I18n.format("gregtech.universal.tooltip.fluid_transfer_rate", new Object[]{32}));
        }));
        AIR_VENT = metaItem.addItem(4, "air_vent").addComponents(new TooltipBehavior((lines) -> {
            lines.add(I18n.format("metaitem.air_vent.tooltip.1", 100));
        }));

        TRACK_SEGMENT = metaItem.addItem(5, "track_segment").addComponents(new TooltipBehavior((lines) -> {
            lines.add(I18n.format("metaitem.track_segment.length_info"));
        }));

        EARTH_ORBITAL_SCRAP = metaItem.addItem(6, "orbital.scrap.earth").setMaxStackSize(8);

        DATA_CARD = metaItem.addItem(7,"data_card").setMaxStackSize(1).addComponents(new TooltipBehavior((lines) -> {
            lines.add(I18n.format("metaitem.data_card.tooltip.1"));
        }));

        DATA_CARD_ACTIVE = metaItem.addItem(8,"data_card.active").setMaxStackSize(1).addComponents(new TooltipBehavior((lines) -> {
            lines.add(I18n.format("metaitem.data_card.active.tooltip.1"));
        }));
        RESTRICTIVE_FILTER = metaItem.addItem(6, "restrictive_filter");
      EARTH_ORBITAL_SCRAP = metaItem.addItem(7, "orbital.scrap.earth").setMaxStackSize(8);
    }

    private static void addTieredOredictItem(OreDictValueItem[] items, int id, int RGB, OrePrefix prefix) {

        for (int i = 0; i < items.length; i++) {
            items[i] = oreDictItem.addOreDictItem(id + i, SuSyValues.TierMaterials[i + 1].toString(), RGB, MaterialIconSet.DULL, prefix, I18n.format("gregtech.universal.catalysts.tooltip.tier", GTValues.V[i], GTValues.VN[i]));
        }

    }

    public static int isMetaItem(ItemStack i) {
        return (i.getItem() instanceof MetaItem<?>) && (i.getItem().equals(metaItem)) ? Objects.requireNonNull(((MetaItem<?>) i.getItem()).getItem(i)).metaValue : -1;
    }
}
