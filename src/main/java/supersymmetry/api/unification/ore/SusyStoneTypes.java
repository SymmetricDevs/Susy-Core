package supersymmetry.api.unification.ore;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.ore.StoneType;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.common.ConfigHolder;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.blocks.SusyStoneVariantBlock;
import supersymmetry.common.materials.SusyMaterials;

public class SusyStoneTypes {

    public static StoneType GABBRO;
    public static StoneType GNEISS;
    public static StoneType GRAPHITE;
    public static StoneType LIMESTONE;
    public static StoneType MICA;
    public static StoneType PHYLLITE;
    public static StoneType QUARTZITE;
    public static StoneType SHALE;
    public static StoneType SLATE;
    public static StoneType SOAPSTONE;
    public static StoneType KIMBERLITE;

    public static StoneType ANORTHOSITE;

    public SusyStoneTypes() {}

    public static void init() {
        GABBRO = new StoneType(12, "gabbro", SoundType.STONE, SusyOrePrefix.oreGabbro, SusyMaterials.Gabbro,
                () -> gtStoneState(SusyStoneVariantBlock.StoneType.GABBRO),
                state -> gtStonePredicate(state, SusyStoneVariantBlock.StoneType.GABBRO), false);
        GNEISS = new StoneType(13, "gneiss", SoundType.STONE, SusyOrePrefix.oreGneiss, SusyMaterials.Gneiss,
                () -> gtStoneState(SusyStoneVariantBlock.StoneType.GNEISS),
                state -> gtStonePredicate(state, SusyStoneVariantBlock.StoneType.GNEISS), false);
        LIMESTONE = new StoneType(14, "limestone", SoundType.STONE, SusyOrePrefix.oreLimestone, SusyMaterials.Limestone,
                () -> gtStoneState(SusyStoneVariantBlock.StoneType.LIMESTONE),
                state -> gtStonePredicate(state, SusyStoneVariantBlock.StoneType.LIMESTONE), false);
        PHYLLITE = new StoneType(15, "phyllite", SoundType.STONE, SusyOrePrefix.orePhyllite, SusyMaterials.Phyllite,
                () -> gtStoneState(SusyStoneVariantBlock.StoneType.PHYLLITE),
                state -> gtStonePredicate(state, SusyStoneVariantBlock.StoneType.PHYLLITE), false);
        QUARTZITE = new StoneType(16, "quartzite", SoundType.STONE, SusyOrePrefix.oreQuartzite, Materials.Quartzite,
                () -> gtStoneState(SusyStoneVariantBlock.StoneType.QUARTZITE),
                state -> gtStonePredicate(state, SusyStoneVariantBlock.StoneType.QUARTZITE), false);
        SHALE = new StoneType(17, "shale", SoundType.STONE, SusyOrePrefix.oreShale, SusyMaterials.Shale,
                () -> gtStoneState(SusyStoneVariantBlock.StoneType.SHALE),
                state -> gtStonePredicate(state, SusyStoneVariantBlock.StoneType.SHALE), false);
        SLATE = new StoneType(18, "slate", SoundType.STONE, SusyOrePrefix.oreSlate, SusyMaterials.Slate,
                () -> gtStoneState(SusyStoneVariantBlock.StoneType.SLATE),
                state -> gtStonePredicate(state, SusyStoneVariantBlock.StoneType.SLATE), false);
        SOAPSTONE = new StoneType(19, "soapstone", SoundType.STONE, SusyOrePrefix.oreSoapstone, Materials.Soapstone,
                () -> gtStoneState(SusyStoneVariantBlock.StoneType.SOAPSTONE),
                state -> gtStonePredicate(state, SusyStoneVariantBlock.StoneType.SOAPSTONE), false);
        KIMBERLITE = new StoneType(20, "kimberlite", SoundType.STONE, SusyOrePrefix.oreKimberlite,
                SusyMaterials.Kimberlite,
                () -> gtStoneState(SusyStoneVariantBlock.StoneType.KIMBERLITE),
                state -> gtStonePredicate(state, SusyStoneVariantBlock.StoneType.KIMBERLITE), false);
        ANORTHOSITE = new StoneType(21, "anorthosite", SoundType.STONE, SusyOrePrefix.oreAnorthosite,
                SusyMaterials.Anorthosite,
                () -> gtStoneState(SusyStoneVariantBlock.StoneType.ANORTHOSITE),
                state -> gtStonePredicate(state, SusyStoneVariantBlock.StoneType.ANORTHOSITE), false);

        if (ConfigHolder.worldgen.allUniqueStoneTypes) {
            addSecondary(SusyOrePrefix.oreGabbro, SusyMaterials.Gabbro);
            addSecondary(SusyOrePrefix.oreGneiss, SusyMaterials.Gneiss);
            addSecondary(SusyOrePrefix.oreLimestone, SusyMaterials.Limestone);
            addSecondary(SusyOrePrefix.orePhyllite, SusyMaterials.Phyllite);
            addSecondary(SusyOrePrefix.oreQuartzite, Materials.Quartzite);
            addSecondary(SusyOrePrefix.oreShale, SusyMaterials.Shale);
            addSecondary(SusyOrePrefix.oreSlate, SusyMaterials.Slate);
            addSecondary(SusyOrePrefix.oreSoapstone, Materials.Soapstone);
            addSecondary(SusyOrePrefix.oreKimberlite, SusyMaterials.Kimberlite);
            addSecondary(SusyOrePrefix.oreAnorthosite, SusyMaterials.Anorthosite);
        }
    }

    private static void addSecondary(OrePrefix prefix, Material material) {
        prefix.addSecondaryMaterial(new MaterialStack(material, OrePrefix.dust.getMaterialAmount(material)));
    }

    private static IBlockState gtStoneState(SusyStoneVariantBlock.StoneType stoneType) {
        return SuSyBlocks.SUSY_STONE_BLOCKS.get(SusyStoneVariantBlock.StoneVariant.SMOOTH).getState(stoneType);
    }

    private static boolean gtStonePredicate(IBlockState state, SusyStoneVariantBlock.StoneType stoneType) {
        SusyStoneVariantBlock block = SuSyBlocks.SUSY_STONE_BLOCKS.get(SusyStoneVariantBlock.StoneVariant.SMOOTH);
        return state.getBlock() == block && block.getState(state) == stoneType;
    }
}
