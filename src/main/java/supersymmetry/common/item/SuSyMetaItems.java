package supersymmetry.common.item;

import gregtech.api.GTValues;
import gregtech.api.items.armor.ArmorMetaItem;
import gregtech.api.items.metaitem.MetaItem.MetaValueItem;
import gregtech.api.items.metaitem.MetaOreDictItem;
import gregtech.api.items.metaitem.MetaOreDictItem.OreDictValueItem;
import gregtech.api.items.metaitem.StandardMetaItem;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.items.behaviors.TooltipBehavior;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.EntityEquipmentSlot;
import supersymmetry.SuSyValues;
import supersymmetry.common.item.armor.AdvancedBreathingApparatus;
import supersymmetry.common.item.armor.BreathingApparatus;
import supersymmetry.common.item.armor.SimpleGasMask;

import static net.minecraft.inventory.EntityEquipmentSlot.*;
import supersymmetry.common.item.armor.SuSyMetaArmor;

public class SuSyMetaItems {

    private static StandardMetaItem metaItem;
    private static SuSyArmorItem armorItem;
    public static MetaOreDictItem oreDictItem;
    public static MetaValueItem CATALYST_BED_SUPPORT_GRID;
    public static MetaValueItem CONVEYOR_STEAM;
    public static MetaValueItem PUMP_STEAM;
    public static MetaValueItem AIR_VENT;
    public static MetaValueItem TRACK_SEGMENT;
    public static SuSyArmorItem.SuSyArmorMetaValueItem SIMPLE_GAS_MASK;
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
        SuSyMetaArmor armor = new SuSyMetaArmor();
        armor.setRegistryName("susy_armor");
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
        SIMPLE_GAS_MASK = armorItem.addItem(0, "simple_gas_mask")
                .setArmorLogic(new SimpleGasMask());
        GAS_MASK = armorItem.addItem(1, "gas_mask").setArmorLogic(new BreathingApparatus(HEAD));
        GAS_TANK = armorItem.addItem(2, "gas_tank").setArmorLogic(new BreathingApparatus(CHEST));
        ASBESTOS_MASK = armorItem.addItem(3, "asbestos_mask").setArmorLogic(new AdvancedBreathingApparatus(HEAD, 1, "asbestos", 0, 0.3));
        ASBESTOS_CHESTPLATE = armorItem.addItem(4, "asbestos_chestplate").setArmorLogic(new AdvancedBreathingApparatus(CHEST, 1, "asbestos", 0, 0.3));
        ASBESTOS_LEGGINGS = armorItem.addItem(5, "asbestos_leggings").setArmorLogic(new AdvancedBreathingApparatus(LEGS, 1, "asbestos", 0, 0.3));
        ASBESTOS_BOOTS = armorItem.addItem(6, "asbestos_boots").setArmorLogic(new AdvancedBreathingApparatus(FEET, 1, "asbestos", 0, 0.3));
        REBREATHER_TANK = armorItem.addItem(7, "rebreather_tank").setArmorLogic(new AdvancedBreathingApparatus(CHEST, 1, "rebreather", 0, 0.3));
        REFLECTIVE_MASK = armorItem.addItem(8, "reflective_mask").setArmorLogic(new AdvancedBreathingApparatus(HEAD, 5, "reflective", 0, 0.4));
        REFLECTIVE_CHESTPLATE = armorItem.addItem(9, "reflective_chestplate").setArmorLogic(new AdvancedBreathingApparatus(CHEST, 5, "reflective", 0, 0.4));
        REFLECTIVE_LEGGINGS = armorItem.addItem(10, "reflective_leggings").setArmorLogic(new AdvancedBreathingApparatus(LEGS, 5, "reflective", 0, 0.4));
        REFLECTIVE_BOOTS = armorItem.addItem(11, "reflective_boots").setArmorLogic(new AdvancedBreathingApparatus(FEET, 5, "reflective", 0, 0.4));
        FILTERED_TANK = armorItem.addItem(12, "filtered_tank").setArmorLogic(new AdvancedBreathingApparatus(CHEST, 5, "filtered", 0, 0.4));
        NOMEX_MASK = armorItem.addItem(13, "nomex_mask").setArmorLogic(new AdvancedBreathingApparatus(HEAD, 0, "nomex", 1, 0.6));
        NOMEX_CHESTPLATE = armorItem.addItem(14, "nomex_chestplate").setArmorLogic(new AdvancedBreathingApparatus(CHEST, 0, "nomex", 1, 0.6));
        NOMEX_LEGGINGS = armorItem.addItem(15, "nomex_leggings").setArmorLogic(new AdvancedBreathingApparatus(LEGS, 0, "nomex", 1, 0.6));
        NOMEX_BOOTS = armorItem.addItem(16, "nomex_boots").setArmorLogic(new AdvancedBreathingApparatus(FEET, 0, "nomex", 1, 0.6));
    }

    private static void addTieredOredictItem(OreDictValueItem[] items, int id, int RGB, OrePrefix prefix) {

        for (int i = 0; i < items.length; i++) {
            items[i] = oreDictItem.addOreDictItem(id + i, SuSyValues.TierMaterials[i + 1].toString(), RGB, MaterialIconSet.DULL, prefix, I18n.format("gregtech.universal.catalysts.tooltip.tier", GTValues.V[i], GTValues.VN[i]));
        }

    }
}
