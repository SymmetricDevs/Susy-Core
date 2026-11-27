package supersymmetry.common.item;

import static gregtech.common.items.MetaItems.SPRAY_EMPTY;
import static supersymmetry.common.item.behavior.GunFireMode.*;
import static supersymmetry.common.item.behavior.SuSyGunBehavior.*;
import static supersymmetry.common.metatileentities.multi.electric.MetaTileEntityCargoDronePad.*;
import static supersymmetry.common.metatileentities.storage.MetaTileEntityLockedCrate.BREACH_DURABILITY;

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
import gregtech.api.items.metaitem.*;
import gregtech.api.items.metaitem.ElectricStats;
import gregtech.api.items.metaitem.MetaItem.MetaValueItem;
import gregtech.api.items.metaitem.MetaOreDictItem.OreDictValueItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.items.metaitem.stats.IItemDurabilityManager;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.material.registry.MaterialRegistry;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.items.MetaItems;
import gregtech.common.items.behaviors.TooltipBehavior;
import supersymmetry.SuSyValues;
import supersymmetry.api.guns.attachment.AttachmentSlot;
import supersymmetry.api.guns.attachment.AttachmentStats;
import supersymmetry.api.unification.ore.SusyOrePrefix;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.item.armor.SuSyMetaArmor;
import supersymmetry.common.item.behavior.*;
import supersymmetry.common.item.behavior.ElectrodeDurabilityManager;

public class SuSyMetaItems {

    private static StandardMetaItem metaItem;
    public static SuSyArmorItem armorItem;

    public static StandardMetaItem getMetaItem() {
        return metaItem;
    }

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
    public static MetaValueItem CODE_BREACHER_DEV;
    public static MetaValueItem SHAPE_MOLD_TARGET;
    public static MetaValueItem ENTITY_TAGGER;
    public static MetaValueItem FACTION_RADIO;
    public static MetaValueItem BASIC_CARGO_DRONE;
    public static MetaValueItem ADVANCED_CARGO_DRONE;
    public static MetaValueItem LOCATION_CARD;
    public static MetaValueItem ELITE_CARGO_DRONE;
    public static MetaValueItem CLAY_GRAPHITE_CRUCIBLE;
    public static MetaValueItem INTEL_CHIP;
    public static MetaValueItem INTEL_CHIP_FULL;
    public static MetaValueItem ENTITY_PROMOTER;
    public static MetaValueItem HYPER_DEFOLIANT;
    public static MetaValueItem SKYWATCH_CHIP;

    public static MetaValueItem DATA_CARD;
    public static MetaValueItem DATA_CARD_ACTIVE;
    public static MetaValueItem DATA_CARD_MASTER_BLUEPRINT;
    public static MetaValueItem ROCKET_CONFIGURER;

    public static MetaValueItem AIR_DISPERSER;
    public static MetaValueItem OXYGEN_SENSOR;
    public static MetaValueItem AIRLOCK;

    public static MetaValueItem FLINTLOCK_PISTOL;
    public static MetaValueItem MUSKET;
    public static MetaValueItem BLUNDERBUSS;

    public static MetaValueItem REVOLVER; // Colt SAA (black powder version)
    public static MetaValueItem BREECHLOADING_RIFLE; // Gras rifle, Mauser Model 1871, Martini-Henry, Berdan rifle, etc
    public static MetaValueItem DOUBLE_BARREL_SHOTGUN;

    public static MetaValueItem BASIC_PISTOL; // Colt M1911
    public static MetaValueItem BOLT_ACTION_RIFLE; // M1903 Springfield, Lee-Enfield, Karabiner 98k, Mosin-Nagant, etc
    public static MetaValueItem PUMP_SHOTGUN; // Winchester Model 1897/1912

    public static MetaValueItem IMPROVED_PISTOL; // Browning Hi-Power (except in .45 ACP)
    public static MetaValueItem SEMI_AUTO_RIFLE; // M1 Garand, SVT-40, Gewehr 43
    public static MetaValueItem BASIC_SMG; // Thompson SMG, PPSh-41, Sten, MP40, etc

    public static MetaValueItem ADVANCED_PISTOL; // FN Five-seven (ignore the anachronism)
    public static MetaValueItem AUTOMATIC_BATTLE_RIFLE; // FN FAL, M14, H&K G3
    public static MetaValueItem SEMI_AUTO_SHOTGUN; // SPAS-12, Benelli M4
    public static MetaValueItem ADVANCED_SMG; // MP5, Uzi
    public static MetaValueItem BASIC_ASSAULT_RIFLE; // AK-47, StG 44
    public static MetaValueItem BASIC_SNIPER_RIFLE; // M21, M40 or L42A1 except in .338 Lapua Magnum

    public static MetaValueItem HEAVY_PISTOL; // Desert Eagle
    public static MetaValueItem BASIC_DMR; // SVD-ish
    public static MetaValueItem AUTO_SHOTGUN; // AA-12
    public static MetaValueItem IMPROVED_ASSAULT_RIFLE; // M16, Steyr AUG, FAMAS, AK-74
    public static MetaValueItem IMPROVED_SNIPER_RIFLE; // AWM
    public static MetaValueItem BASIC_ANTI_MATERIEL_RIFLE; // M82
    public static MetaValueItem BASIC_LMG; // M249

    public static MetaValueItem IMPROVED_DMR; // SCAR-H, Mk14 EBR
    public static MetaValueItem PDW; // FN P90, MP7
    public static MetaValueItem ADVANCED_ASSAULT_RIFLE; // SIG MCX SPEAR
    public static MetaValueItem ADVANCED_SNIPER_RIFLE; // Barrett MRAD, M2010
    public static MetaValueItem IMPROVED_ANTI_MATERIEL_RIFLE; // AS50
    public static MetaValueItem IMPROVED_LMG; // M250
    public static MetaValueItem BASIC_GPMG; // M240

    public static MetaValueItem PAPER_CARTRIDGE;
    public static MetaValueItem GUNPOWDER_CARTRIDGE;
    public static MetaValueItem STRIPPER_CLIP;
    public static MetaValueItem SHOTGUN_SHELL;
    public static MetaValueItem BASIC_CARTRIDGE;
    public static MetaValueItem MAGAZINE_5;
    public static MetaValueItem MAGAZINE_10;
    public static MetaValueItem MAGAZINE_15;
    public static MetaValueItem MAGAZINE_20;
    public static MetaValueItem MAGAZINE_30;
    public static MetaValueItem MAGAZINE_50;
    public static MetaValueItem BELT_200;

    // 300-306: gun attachments
    public static MetaValueItem RED_DOT;
    public static MetaValueItem SCOPE;
    public static MetaValueItem VERTICAL_GRIP;
    public static MetaValueItem SUPPRESSOR;
    public static MetaValueItem LASER_SIGHT;
    public static MetaValueItem HEAVY_STOCK;
    public static MetaValueItem BIPOD;

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

        // IDs start at 1 for historical reasons. Do not renumber existing items.
        CATALYST_BED_SUPPORT_GRID = metaItem.addItem(1, "catalyst_bed_support_grid");
        CONVEYOR_STEAM = metaItem.addItem(2, "conveyor.steam").addComponents(
                new TooltipBehavior(lines -> Collections.addAll(lines, I18n.format("metaitem.conveyor.module.tooltip"),
                        I18n.format("gregtech.universal.tooltip.item_transfer_rate", 4))));
        PUMP_STEAM = metaItem.addItem(3, "pump.steam").addComponents(
                new TooltipBehavior(lines -> Collections.addAll(lines, I18n.format("metaitem.electric.pump.tooltip"),
                        I18n.format("gregtech.universal.tooltip.fluid_transfer_rate", 32))));
        AIR_VENT = metaItem.addItem(4, "air_vent").addComponents(
                new TooltipBehavior(lines -> lines.add(I18n.format("metaitem.air_vent.tooltip.1", 100))));

        TRACK_SEGMENT = metaItem.addItem(5, "track_segment").addComponents(
                new TooltipBehavior(lines -> lines.add(I18n.format("metaitem.track_segment.length_info"))));
        RESTRICTIVE_FILTER = metaItem.addItem(6, "restrictive_filter");
        EARTH_ORBITAL_SCRAP = metaItem.addItem(7, "orbital.scrap.earth").setMaxStackSize(8);

        CODE_BREACHER = metaItem.addItem(8, "code_breacher").setMaxStackSize(1)
                .addComponents((IItemDurabilityManager) itemStack -> {
                    int uses = itemStack.hasTagCompound() ? itemStack.getTagCompound().getInteger("Uses") : 0;
                    return (double) (BREACH_DURABILITY - uses) / BREACH_DURABILITY;
                });

        ENTITY_TAGGER = metaItem.addItem(9, "entity_tagger").setMaxStackSize(1);

        FACTION_RADIO = metaItem.addItem(10, "faction_radio").setMaxStackSize(1)
                .addComponents(new FactionRadioBehaviour());

        DATA_CARD = metaItem.addItem(11, "data_card").setMaxStackSize(1)
                .addComponents(new TooltipBehavior(lines -> lines.add(I18n.format("metaitem.data_card.tooltip.1"))));

        DATA_CARD_ACTIVE = metaItem.addItem(12, "data_card.active").setMaxStackSize(1)
                .addComponents(new DataCardBehavior(lines -> lines.add(I18n.format("metaitem.data_card.tooltip.1")),
                        Arrays.asList("name")));

        DATA_CARD_MASTER_BLUEPRINT = metaItem.addItem(13, "data_card.master_blueprint").setMaxStackSize(1)
                .addComponents(new BlueprintBehavior(_ -> {}, Arrays.asList("name")));

        TUNGSTEN_ELECTRODE = metaItem.addItem(14, "tungsten_electrode").setMaxStackSize(1)
                .addComponents(ElectrodeDurabilityManager.INSTANCE);

        ROCKET_CONFIGURER = metaItem.addItem(15, "rocket_configurer").setMaxStackSize(1)
                .addComponents(new RocketConfigBehavior());

        CODE_BREACHER_DEV = metaItem.addItem(16, "code_breacher_dev").setMaxStackSize(1);

        SHAPE_MOLD_TARGET = metaItem.addItem(17, "shape.mold.target");

        BASIC_CARGO_DRONE = metaItem.addItem(18, "cargo_drone.basic").setMaxStackSize(1)
                .addComponents(ElectricStats.createRechargeableBattery(basicDroneCharge, GTValues.MV));

        ADVANCED_CARGO_DRONE = metaItem.addItem(19, "cargo_drone.advanced").setMaxStackSize(1)
                .addComponents(ElectricStats.createRechargeableBattery(advancedDroneCharge, GTValues.HV));

        LOCATION_CARD = metaItem.addItem(20, "location_card").setMaxStackSize(1)
                .addComponents(new LocationCardBehavior());

        ELITE_CARGO_DRONE = metaItem.addItem(21, "cargo_drone.elite").setMaxStackSize(1)
                .addComponents(new HydrogenPoweredDroneBehavior(eliteDroneFuel));

        AIR_DISPERSER = metaItem.addItem(22, "air_disperser");

        OXYGEN_SENSOR = metaItem.addItem(23, "oxygen_sensor").setMaxStackSize(1)
                .addComponents(new OxygenSensorBehavior())
                .addComponents(ElectricStats.createElectricItem(16000, GTValues.LV));

        CLAY_GRAPHITE_CRUCIBLE = metaItem.addItem(24, "clay_graphite_crucible");

        AIRLOCK = metaItem.addItem(25, "airlock")
                .addComponents(new AirlockBehavior(SuSyBlocks.AIRLOCK_DOOR));

        ENTITY_PROMOTER = metaItem.addItem(26, "entity_promoter").setMaxStackSize(1);

        INTEL_CHIP = metaItem.addItem(27, "intel_chip").setMaxStackSize(1)
                .addComponents(IntelChipBehaviour.INSTANCE);

        INTEL_CHIP_FULL = metaItem.addItem(28, "intel_chip_full")
                .addComponents(IntelChipFullBehaviour.INSTANCE);
        HYPER_DEFOLIANT = metaItem.addItem(29, "hyper_defoliant")
                .addComponents(new HyperDefoliantBehavior());
        SKYWATCH_CHIP = metaItem.addItem(30, "skywatch_chip").setMaxStackSize(1)
                .addComponents(SkywatchChipBehaviour.INSTANCE);

        // 100-200: guns
        FLINTLOCK_PISTOL = metaItem.addItem(100, "flintlock_pistol").setMaxStackSize(1)
                .addComponents(gun(100, 20, 6f, 1, CALIBER_PAPER_CARTRIDGE)
                        .magazine(1, 400).setReloadMode(GunReloadMode.INTEGRAL_MAGAZINE).modes(BOLT_ACTION)
                        .setRecoil(0.2f).build());
        MUSKET = metaItem.addItem(101, "musket").setMaxStackSize(1)
                .addComponents(gun(200, 12, 9f, 1, CALIBER_PAPER_CARTRIDGE)
                        .magazine(1, 600).setReloadMode(GunReloadMode.INTEGRAL_MAGAZINE).modes(BOLT_ACTION)
                        .setRecoil(0.5f).build());
        BLUNDERBUSS = metaItem.addItem(102, "blunderbuss").setMaxStackSize(1)
                .addComponents(gun(50, 40, 12f, 1, CALIBER_PAPER_CARTRIDGE)
                        .magazine(1, 600).setReloadMode(GunReloadMode.INTEGRAL_MAGAZINE).modes(BOLT_ACTION)
                        .setRecoil(0.7f).build());
        REVOLVER = metaItem.addItem(103, "revolver").setMaxStackSize(1)
                .addComponents(gun(180, 15, 5f, 6, CALIBER_11x24_BP)
                        .magazine(6, 300).setReloadMode(GunReloadMode.INTEGRAL_MAGAZINE).modes(SEMI_AUTO)
                        .setRecoil(0.15f).attachments(SIDEARM_SLOTS).build());
        BREECHLOADING_RIFLE = metaItem.addItem(104, "breechloading_rifle").setMaxStackSize(1)
                .addComponents(gun(370, 5, 10f, 1, CALIBER_13x45_BP)
                        .magazine(1, 200).setReloadMode(GunReloadMode.INTEGRAL_MAGAZINE).modes(BOLT_ACTION)
                        .setRecoil(0.3f).build());
        DOUBLE_BARREL_SHOTGUN = metaItem.addItem(105, "double_barrel_shotgun").setMaxStackSize(1)
                .addComponents(gun(300, 30, 15f, 1, CALIBER_18_SHOTGUN_BP)
                        .magazine(2, 250).setReloadMode(GunReloadMode.INTEGRAL_MAGAZINE).modes(BOLT_ACTION)
                        .setRecoil(0.4f).build());
        BASIC_PISTOL = metaItem.addItem(106, "basic_pistol").setMaxStackSize(1)
                .addComponents(gun(250, 9, 6f, 5, CALIBER_11x24)
                        .attachments(SIDEARM_SLOTS).modes(SEMI_AUTO).magazine(10, 5).setRecoil(0.1f).build());
        BOLT_ACTION_RIFLE = metaItem.addItem(107, "bolt_action_rifle").setMaxStackSize(1)
                .addComponents(gun(850, 1, 12f, 37, CALIBER_7_5x55)
                        .magazine(5, 104).setReloadMode(GunReloadMode.STRIPPER_CLIP).setRecoil(0.4f).modes(BOLT_ACTION)
                        .aimTime(11).build());
        PUMP_SHOTGUN = metaItem.addItem(108, "pump_shotgun").setMaxStackSize(1)
                .addComponents(gun(350, 25, 20f, 50, CALIBER_18_SHOTGUN)
                        .magazine(5, 200).setReloadMode(GunReloadMode.INTEGRAL_MAGAZINE).setRecoil(0.35f)
                        .modes(BOLT_ACTION).build());
        IMPROVED_PISTOL = metaItem.addItem(109, "improved_pistol").setMaxStackSize(1)
                .addComponents(gun(270, 6, 6.5f, 4, CALIBER_11x24)
                        .modes(SEMI_AUTO).attachments(SIDEARM_SLOTS).magazine(15, 100).setRecoil(0.09f).build());
        SEMI_AUTO_RIFLE = metaItem.addItem(110, "semi_auto_rifle").setMaxStackSize(1)
                .addComponents(gun(850, 1, 12f, 7, CALIBER_7_5x55)
                        .modes(SEMI_AUTO).setRecoil(0.32f).magazine(10, 59).aimTime(9).build());
        BASIC_SMG = metaItem.addItem(111, "basic_smg").setMaxStackSize(1)
                .addComponents(gun(350, 6, 5f, 3, CALIBER_11x24)
                        .modes(FULL_AUTO, SEMI_AUTO).setRecoil(0.1f).magazine(30, 100).build());
        ADVANCED_PISTOL = metaItem.addItem(112, "advanced_pistol").setMaxStackSize(1)
                .addComponents(gun(300, 4, 7f, 4, CALIBER_5_7x30)
                        .magazine(30, 60).setRecoil(0.05f).attachments(SIDEARM_SLOTS).modes(SEMI_AUTO).build());
        AUTOMATIC_BATTLE_RIFLE = metaItem.addItem(113, "automatic_battle_rifle").setMaxStackSize(1)
                .addComponents(gun(850, 1.5, 12f, 2, CALIBER_7_5x55)
                        .modes(FULL_AUTO, SEMI_AUTO).magazine(30, 100).setRecoil(0.35f).build());
        SEMI_AUTO_SHOTGUN = metaItem.addItem(114, "semi_auto_shotgun").setMaxStackSize(1)
                .addComponents(gun(350, 25, 22f, 6, CALIBER_18_SHOTGUN)
                        .modes(SEMI_AUTO).setRecoil(0.3f).magazine(15, 100).build());
        ADVANCED_SMG = metaItem.addItem(115, "advanced_smg").setMaxStackSize(1)
                .addComponents(gun(400, 4, 7f, 2, CALIBER_11x24)
                        .modes(FULL_AUTO, SEMI_AUTO, BURST).magazine(50, 65).burst(3, 2).setRecoil(0.06f).build());
        BASIC_ASSAULT_RIFLE = metaItem.addItem(116, "basic_assault_rifle").setMaxStackSize(1)
                .addComponents(gun(700, 3, 9f, 2, CALIBER_5_5x45)
                        .modes(FULL_AUTO, SEMI_AUTO).setRecoil(0.15f).magazine(50, 65).build());
        BASIC_SNIPER_RIFLE = metaItem.addItem(117, "basic_sniper_rifle").setMaxStackSize(1)
                .addComponents(gun(900, 0.3, 30f, 45, CALIBER_8_6x70)
                        .modes(BOLT_ACTION).setRecoil(0.5f).magazine(5, 100).build());
        HEAVY_PISTOL = metaItem.addItem(118, "heavy_pistol").setMaxStackSize(1)
                .addComponents(gun(500, 3, 12f, 5, CALIBER_13x30)
                        .modes(SEMI_AUTO).setRecoil(0.4f).magazine(15, 100).attachments(SIDEARM_SLOTS).build());
        BASIC_DMR = metaItem.addItem(119, "basic_dmr").setMaxStackSize(1)
                .addComponents(gun(850, 0.7, 22f, 15, CALIBER_7_5x55)
                        .modes(SEMI_AUTO).setRecoil(0.4f).magazine(20, 100).build());
        AUTO_SHOTGUN = metaItem.addItem(120, "auto_shotgun").setMaxStackSize(1)
                .addComponents(gun(370, 25, 24f, 5, CALIBER_18_SHOTGUN)
                        .modes(FULL_AUTO).setRecoil(0.3f).magazine(30, 100).build());
        IMPROVED_ASSAULT_RIFLE = metaItem.addItem(121, "improved_assault_rifle").setMaxStackSize(1)
                .addComponents(gun(900, 2, 10f, 2, CALIBER_5_5x45)
                        .modes(FULL_AUTO, SEMI_AUTO, BURST).magazine(50, 60).burst(3, 3).setRecoil(0.12f).build());
        IMPROVED_SNIPER_RIFLE = metaItem.addItem(122, "improved_sniper_rifle").setMaxStackSize(1)
                .addComponents(gun(900, 0.2, 35f, 40, CALIBER_8_6x70)
                        .setRecoil(0.45f).magazine(10, 100).modes(BOLT_ACTION).build());
        BASIC_ANTI_MATERIEL_RIFLE = metaItem.addItem(123, "basic_anti_materiel_rifle").setMaxStackSize(1)
                .addComponents(gun(850, 1, 50f, 20, CALIBER_13x100)
                        .setRecoil(0.65f).magazine(5, 100).modes(SEMI_AUTO).build());
        BASIC_LMG = metaItem.addItem(124, "basic_lmg").setMaxStackSize(1)
                .addComponents(gun(900, 2, 9f, 1, CALIBER_5_5x45)
                        .magazine(200, 100).setRecoil(0.12f).setReloadMode(GunReloadMode.BELT).modes(FULL_AUTO)
                        .build());
        IMPROVED_DMR = metaItem.addItem(125, "improved_dmr").setMaxStackSize(1)
                .addComponents(gun(850, 0.5, 25f, 12, CALIBER_7_5x55)
                        .magazine(30, 100).modes(SEMI_AUTO, BURST).burst(2, 12).setRecoil(0.32f).build());
        PDW = metaItem.addItem(126, "pdw").setMaxStackSize(1)
                .addComponents(gun(750, 1.5, 8f, 1, CALIBER_5_7x30)
                        .magazine(50, 50).modes(FULL_AUTO, SEMI_AUTO, BURST).burst(3, 2).setRecoil(0.05f).build());
        ADVANCED_ASSAULT_RIFLE = metaItem.addItem(127, "advanced_assault_rifle").setMaxStackSize(1)
                .addComponents(gun(950, 0.9, 13f, 2, CALIBER_6_8x50)
                        .magazine(50, 50).modes(FULL_AUTO, SEMI_AUTO, BURST).burst(3, 2).setRecoil(0.13f).build());
        ADVANCED_SNIPER_RIFLE = metaItem.addItem(128, "advanced_sniper_rifle").setMaxStackSize(1)
                .addComponents(gun(850, 0.05, 40f, 35, CALIBER_8_6x70)
                        .setRecoil(0.4f).magazine(20, 100).modes(BOLT_ACTION).build());
        IMPROVED_ANTI_MATERIEL_RIFLE = metaItem.addItem(129, "improved_anti_materiel_rifle").setMaxStackSize(1)
                .addComponents(gun(900, 0.35, 60f, 15, CALIBER_13x100)
                        .setRecoil(0.6f).magazine(10, 100).modes(SEMI_AUTO).build());
        IMPROVED_LMG = metaItem.addItem(130, "improved_lmg").setMaxStackSize(1)
                .addComponents(gun(900, 1.5, 11f, 1, CALIBER_6_8x50)
                        .setRecoil(0.1f).magazine(200, 100).setReloadMode(GunReloadMode.BELT).modes(FULL_AUTO).build());
        BASIC_GPMG = metaItem.addItem(131, "basic_gpmg").setMaxStackSize(1)
                .addComponents(gun(850, 1.5, 15f, 1, CALIBER_7_5x55)
                        .setRecoil(0.25f).magazine(200, 100).setReloadMode(GunReloadMode.BELT).modes(FULL_AUTO)
                        .build());

        // 200-300: ammo
        MAGAZINE_5 = metaItem.addItem(200, "magazine_5").setMaxStackSize(1)
                .addComponents(new GunMagazineBehavior(5, 0.5));
        MAGAZINE_10 = metaItem.addItem(201, "magazine_10").setMaxStackSize(1)
                .addComponents(new GunMagazineBehavior(10, 1.0));
        MAGAZINE_15 = metaItem.addItem(202, "magazine_15").setMaxStackSize(1)
                .addComponents(new GunMagazineBehavior(15, 1.5));
        MAGAZINE_20 = metaItem.addItem(203, "magazine_20").setMaxStackSize(1)
                .addComponents(new GunMagazineBehavior(20, 2.0));
        MAGAZINE_30 = metaItem.addItem(204, "magazine_30").setMaxStackSize(1)
                .addComponents(new GunMagazineBehavior(30, 3.0));
        MAGAZINE_50 = metaItem.addItem(205, "magazine_50").setMaxStackSize(1)
                .addComponents(new GunMagazineBehavior(50, 5.0));
        BELT_200 = metaItem.addItem(206, "belt_200").setMaxStackSize(1)
                .addComponents(new GunMagazineBehavior(200, 1.0));
        // 207-214: reserved for bigger belts
        PAPER_CARTRIDGE = metaItem.addItem(215, "paper_cartridge");
        GUNPOWDER_CARTRIDGE = metaItem.addItem(216, "gunpowder_cartridge").addComponents(new BulletTooltipBehavior());
        STRIPPER_CLIP = metaItem.addItem(217, "stripper_clip").addComponents(new BulletTooltipBehavior())
                .setMaxStackSize(4);
        SHOTGUN_SHELL = metaItem.addItem(218, "shotgun_shell").addComponents(new BulletTooltipBehavior());
        BASIC_CARTRIDGE = metaItem.addItem(219, "basic_cartridge").addComponents(new BulletTooltipBehavior());

        // 300-306: gun attachments
        // TODO: suppressors do not change the firing sound yet
        RED_DOT = metaItem.addItem(300, "red_dot").setMaxStackSize(1)
                .addComponents(new SuSyAttachmentBehaviour(AttachmentSlot.TOP,
                        new AttachmentStats.Modifiers(1f, 0.97f, 1f, 0.8f, 1.1f)));
        SCOPE = metaItem.addItem(301, "scope").setMaxStackSize(1)
                .addComponents(new SuSyAttachmentBehaviour(AttachmentSlot.TOP,
                        new AttachmentStats.Modifiers(1f, 0.9f, 1f, 1.5f, 3f)));
        VERTICAL_GRIP = metaItem.addItem(302, "vertical_grip").setMaxStackSize(1)
                .addComponents(new SuSyAttachmentBehaviour(AttachmentSlot.UNDERSIDE,
                        new AttachmentStats.Modifiers(1f, 0.8f, 0.75f, 1f, 1f)));
        SUPPRESSOR = metaItem.addItem(303, "suppressor").setMaxStackSize(1)
                .addComponents(new SuSyAttachmentBehaviour(AttachmentSlot.MUZZLE,
                        new AttachmentStats.Modifiers(0.9f, 0.95f, 0.9f, 1f, 1f)));
        LASER_SIGHT = metaItem.addItem(304, "laser_sight").setMaxStackSize(1)
                .addComponents(new SuSyAttachmentBehaviour(AttachmentSlot.LATERAL,
                        new AttachmentStats.Modifiers(1f, 0.85f, 1f, 1f, 1f)));
        HEAVY_STOCK = metaItem.addItem(305, "heavy_stock").setMaxStackSize(1)
                .addComponents(new SuSyAttachmentBehaviour(AttachmentSlot.STOCK,
                        new AttachmentStats.Modifiers(1f, 0.85f, 0.6f, 0.9f, 1f)));
        BIPOD = metaItem.addItem(306, "bipod").setMaxStackSize(1)
                .addComponents(new SuSyAttachmentBehaviour(AttachmentSlot.UNDERSIDE,
                        AttachmentStats.Modifiers.NONE,
                        // a bipod is only braced while crouching, so these only count then
                        new AttachmentStats.Modifiers(1f, 0.6f, 0.4f, 0.8f, 1f)));
    }

    /** Slots a sidearm takes: everything but a stock, which is what the screen crosses out on pistols. */
    private static final AttachmentSlot[] SIDEARM_SLOTS = { AttachmentSlot.TOP, AttachmentSlot.UNDERSIDE,
            AttachmentSlot.MUZZLE, AttachmentSlot.LATERAL };

    private static void addExtraBehaviours() {
        MetaItems.SPRAY_SOLVENT.addComponents(new PipeNetPainterBehavior(1024, SPRAY_EMPTY.getStackForm(), -1));
        for (int i = 0; i < EnumDyeColor.values().length; i++) {
            MetaItems.SPRAY_CAN_DYES[i].addComponents(new PipeNetPainterBehavior(512, SPRAY_EMPTY.getStackForm(), i));
        }
        MetaItems.DUCT_TAPE.addComponents(new TapeRepairBehavior());
    }

    private static void addTieredOredictItem(OreDictValueItem[] items, int id, int RGB, OrePrefix prefix) {
        for (int i = 0; i < items.length; i++) {
            items[i] = oreDictItem.addOreDictItem(id + i, SuSyValues.TierMaterials[i + 1].toString(), RGB,
                    MaterialIconSet.DULL, prefix,
                    I18n.format("susy.universal.catalysts.tooltip.tier", GTValues.V[i], GTValues.VN[i]));
        }
    }

    public static SuSyGunBehavior getGunBehaviour(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof MetaItem)) {
            return null;
        }
        MetaItem<?>.MetaValueItem valueItem = ((MetaItem<?>) stack.getItem()).getItem(stack);
        if (valueItem == null) {
            return null;
        }
        for (IItemBehaviour behaviour : valueItem.getBehaviours()) {
            if (behaviour instanceof SuSyGunBehavior) {
                return (SuSyGunBehavior) behaviour;
            }
        }
        return null;
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
