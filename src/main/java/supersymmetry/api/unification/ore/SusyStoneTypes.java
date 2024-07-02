package supersymmetry.api.unification.ore;

import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.StoneType;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.blocks.SuSyStoneVariantBlock;
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

    public SusyStoneTypes(){
    }
    public static void init(){
        GABBRO = new StoneType(12, "gabbro", SoundType.STONE, SusyOrePrefix.oreGabbro, SusyMaterials.Gabbro,
                () -> gtStoneState(SuSyStoneVariantBlock.StoneType.GABBRO),
                state -> gtStonePredicate(state, SuSyStoneVariantBlock.StoneType.GABBRO), false);
        GNEISS = new StoneType(13, "gneiss", SoundType.STONE, SusyOrePrefix.oreGneiss, SusyMaterials.Gneiss,
                () -> gtStoneState(SuSyStoneVariantBlock.StoneType.GNEISS),
                state -> gtStonePredicate(state, SuSyStoneVariantBlock.StoneType.GNEISS), false);
        LIMESTONE = new StoneType(14, "limestone", SoundType.STONE, SusyOrePrefix.oreLimestone, SusyMaterials.Limestone,
                () -> gtStoneState(SuSyStoneVariantBlock.StoneType.LIMESTONE),
                state -> gtStonePredicate(state, SuSyStoneVariantBlock.StoneType.LIMESTONE), false);
        PHYLLITE = new StoneType(15, "phyllite", SoundType.STONE, SusyOrePrefix.orePhyllite, SusyMaterials.Phyllite,
                () -> gtStoneState(SuSyStoneVariantBlock.StoneType.PHYLLITE),
                state -> gtStonePredicate(state, SuSyStoneVariantBlock.StoneType.PHYLLITE), false);
        QUARTZITE = new StoneType(16, "quartzite", SoundType.STONE, SusyOrePrefix.oreQuartzite, Materials.Quartzite,
                () -> gtStoneState(SuSyStoneVariantBlock.StoneType.QUARTZITE),
                state -> gtStonePredicate(state, SuSyStoneVariantBlock.StoneType.QUARTZITE), false);
        SHALE = new StoneType(17, "shale", SoundType.STONE, SusyOrePrefix.oreShale, SusyMaterials.Shale,
                () -> gtStoneState(SuSyStoneVariantBlock.StoneType.SHALE),
                state -> gtStonePredicate(state, SuSyStoneVariantBlock.StoneType.SHALE), false);
        SLATE = new StoneType(18, "slate", SoundType.STONE, SusyOrePrefix.oreSlate, SusyMaterials.Slate,
                () -> gtStoneState(SuSyStoneVariantBlock.StoneType.SLATE),
                state -> gtStonePredicate(state, SuSyStoneVariantBlock.StoneType.SLATE), false);
        SOAPSTONE = new StoneType(19, "soapstone", SoundType.STONE, SusyOrePrefix.oreSoapstone, Materials.Soapstone,
                () -> gtStoneState(SuSyStoneVariantBlock.StoneType.SOAPSTONE),
                state -> gtStonePredicate(state, SuSyStoneVariantBlock.StoneType.SOAPSTONE), false);
        KIMBERLITE = new StoneType(20, "kimberlite", SoundType.STONE, SusyOrePrefix.oreKimberlite, SusyMaterials.Kimberlite,
                () -> gtStoneState(SuSyStoneVariantBlock.StoneType.KIMBERLITE),
                state -> gtStonePredicate(state, SuSyStoneVariantBlock.StoneType.KIMBERLITE), false);
    }

    private static IBlockState gtStoneState(SuSyStoneVariantBlock.StoneType stoneType) {
        return SuSyBlocks.SUSY_STONE_BLOCKS.get(SuSyStoneVariantBlock.StoneVariant.SMOOTH).getState(stoneType);
    }

    private static boolean gtStonePredicate(IBlockState state, SuSyStoneVariantBlock.StoneType stoneType) {
        SuSyStoneVariantBlock block = SuSyBlocks.SUSY_STONE_BLOCKS.get(SuSyStoneVariantBlock.StoneVariant.SMOOTH);
        return state.getBlock() == block && block.getState(state) == stoneType;
    }
}
