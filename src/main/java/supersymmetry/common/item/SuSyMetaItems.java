package supersymmetry.common.item;

import static gregtech.common.items.MetaItems.SPRAY_EMPTY;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;

import com.google.common.base.CaseFormat;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.items.armor.ArmorMetaItem;
import gregtech.api.items.materialitem.MetaPrefixItem;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.MetaItem.MetaValueItem;
import gregtech.api.items.metaitem.MetaOreDictItem;
import gregtech.api.items.metaitem.MetaOreDictItem.OreDictValueItem;
import gregtech.api.items.metaitem.StandardMetaItem;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.material.registry.MaterialRegistry;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.items.MetaItems;
import gregtech.common.items.behaviors.TooltipBehavior;
import supersymmetry.SuSyValues;
import supersymmetry.api.unification.ore.SusyOrePrefix;
import supersymmetry.common.item.armor.SuSyMetaArmor;
import supersymmetry.common.item.behavior.DataCardBehavior;
import supersymmetry.common.item.behavior.MillBallDurabilityManager;
import supersymmetry.common.item.behavior.PipeNetPainterBehavior;
import supersymmetry.common.item.behavior.RocketConfigBehavior;

public class SuSyMetaItems {

    // DO NOT CHANGE
    private static int itemIndex = 1;

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
    public static MetaValueItem TUNGSTEN_ELECTRODE;
    public static MetaValueItem CODE_BREACHER;

    public static MetaValueItem DATA_CARD;
    public static MetaValueItem DATA_CARD_ACTIVE;
    public static MetaValueItem DATA_CARD_MASTER_BLUEPRINT;
    public static MetaValueItem ROCKET_CONFIGURER;
    public static MetaValueItem PADDING_CLOTH;

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

    public static ArmorMetaItem<?>.ArmorMetaValueItem ASTRONAUT_HELMET;
    public static ArmorMetaItem<?>.ArmorMetaValueItem ASTRONAUT_CHESTPLATE;
    public static ArmorMetaItem<?>.ArmorMetaValueItem ASTRONAUT_LEGGINGS;
    public static ArmorMetaItem<?>.ArmorMetaValueItem ASTRONAUT_BOOTS;

    public static void initMetaItems() {
        metaItem = new StandardMetaItem();
        metaItem.setRegistryName("meta_item");
        oreDictItem = new MetaOreDictItem((short) 0);
        oreDictItem.setRegistryName("susy_oredict_item");
        armorItem = new SuSyMetaArmor();
        armorItem.setRegistryName("susy_armor");
        CatalystItems.init();

        for (MaterialRegistry registry : GregTechAPI.materialManager.getRegistries()) {
            String regName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, SusyOrePrefix.millBall.name());
            MetaPrefixItem metaOrePrefix = new MetaPrefixItem(registry, SusyOrePrefix.millBall) {

                @Override
                public void registerSubItems() {
                    for (Material material : registry) {
                        short i = (short) registry.getIDForObject(material);
                        if (canGenerate(SusyOrePrefix.millBall, material)) {
                            var metaItem = addItem(i,
                                    new UnificationEntry(SusyOrePrefix.millBall, material).toString());
                            metaItem.addComponents(MillBallDurabilityManager.INSTANCE);
                        }
                    }
                }
            };
            metaOrePrefix.setRegistryName(registry.getModid(), String.format("meta_%s", regName));
        }
    }

    public static void initSubItems() {
        initMetaItem();
        CatalystItems.initCatalysts();
    }

    private static void initMetaItem() {
        addExtraBehaviours();

        // initialize metaitems here
        if (itemIndex != 1) { // but only once
            return;
        }
        CATALYST_BED_SUPPORT_GRID = initOneItem("catalyst_bed_support_grid");
        CONVEYOR_STEAM = initOneItem("conveyor.steam")
                .addComponents(new TooltipBehavior(lines -> Collections.addAll(lines,
                        I18n.format("metaitem.conveyor.module.tooltip"),
                        I18n.format("gregtech.universal.tooltip.item_transfer_rate", 4))));
        PUMP_STEAM = initOneItem("pump.steam").addComponents(new TooltipBehavior(lines -> Collections.addAll(lines,
                I18n.format("metaitem.electric.pump.tooltip"),
                I18n.format("gregtech.universal.tooltip.fluid_transfer_rate", 32))));
        AIR_VENT = initOneItem("air_vent").addComponents(
                new TooltipBehavior(lines -> lines.add(I18n.format("metaitem.air_vent.tooltip.1", 100))));

        TRACK_SEGMENT = initOneItem("track_segment").addComponents(
                new TooltipBehavior(lines -> lines.add(I18n.format("metaitem.track_segment.length_info"))));
        RESTRICTIVE_FILTER = initOneItem("restrictive_filter");
        EARTH_ORBITAL_SCRAP = initOneItem("orbital.scrap.earth").setMaxStackSize(8);

        CODE_BREACHER = initOneItem("code_breacher").setMaxStackSize(1);

        DATA_CARD = initOneItem("data_card").setMaxStackSize(1)
                .addComponents(new TooltipBehavior(lines -> lines.add(I18n.format("metaitem.data_card.tooltip.1"))));

        DATA_CARD_ACTIVE = initOneItem("data_card.active").setMaxStackSize(1).addComponents(new DataCardBehavior(
                lines -> lines.add(I18n.format("metaitem.data_card.tooltip.1")), Arrays.asList("type")));

        DATA_CARD_MASTER_BLUEPRINT = initOneItem("data_card.master_blueprint").setMaxStackSize(1)
                .addComponents(new DataCardBehavior(
                        lines -> lines.add(I18n.format("metaitem.data_card.master_blueprint.tooltip.1")),
                        Arrays.asList("rocketType")));

        TUNGSTEN_ELECTRODE = initOneItem("tungsten_electrode");

        ROCKET_CONFIGURER = initOneItem("rocket_configurer").setMaxStackSize(1)
                .addComponents(new RocketConfigBehavior());

        PADDING_CLOTH = initOneItem("padding_cloth");
    }

    // Ensures ID stability when merging
    private static MetaItem<?>.MetaValueItem initOneItem(String unlocalizedName) {
        MetaItem<?>.MetaValueItem ret = metaItem.addItem(itemIndex, unlocalizedName);
        itemIndex++;
        return ret;
    }

    private static void addExtraBehaviours() {
        MetaItems.SPRAY_SOLVENT.addComponents(new PipeNetPainterBehavior(1024, SPRAY_EMPTY.getStackForm(), -1));
        for (int i = 0; i < EnumDyeColor.values().length; i++) {
            MetaItems.SPRAY_CAN_DYES[i].addComponents(new PipeNetPainterBehavior(512, SPRAY_EMPTY.getStackForm(), i));
        }
    }

    private static void addTieredOredictItem(OreDictValueItem[] items, int id, int RGB, OrePrefix prefix) {
        for (int i = 0; i < items.length; i++) {
            items[i] = oreDictItem.addOreDictItem(id + i, SuSyValues.TierMaterials[i + 1].toString(), RGB,
                    MaterialIconSet.DULL, prefix,
                    I18n.format("susy.universal.catalysts.tooltip.tier", GTValues.V[i], GTValues.VN[i]));
        }
    }

    public static int isMetaItem(ItemStack i) {
        return (i.getItem() instanceof MetaItem<?>) && (i.getItem().equals(metaItem)) ?
                Objects.requireNonNull(((MetaItem<?>) i.getItem()).getItem(i)).metaValue : -1;
    }

    public static ItemStack getItem(String valueName) {
        MetaItem<?>.MetaValueItem item = metaItem.getItem(valueName);
        return item != null ? item.getStackForm() : ItemStack.EMPTY;
    }
}
