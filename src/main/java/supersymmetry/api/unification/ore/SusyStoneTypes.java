package supersymmetry.api.unification.ore;

import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.StoneType;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
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

    public SusyStoneTypes(){
    }
    public static void init(){
        GABBRO = new StoneType(12, "gabbro", SoundType.STONE, SusyOrePrefix.oreGabbro, SusyMaterials.Gabbro,
                () -> gtStoneState(SusyStoneVariantBlock.StoneType.GABBRO),
                state -> gtStonePredicate(state, SusyStoneVariantBlock.StoneType.GABBRO), false);
        GNEISS = new StoneType(13, "gneiss", SoundType.STONE, SusyOrePrefix.oreGneiss, SusyMaterials.Gneiss,
                () -> gtStoneState(SusyStoneVariantBlock.StoneType.GNEISS),
                state -> gtStonePredicate(state, SusyStoneVariantBlock.StoneType.GNEISS), false);
        GRAPHITE = new StoneType(14, "graphite", SoundType.STONE, SusyOrePrefix.oreGraphite, Materials.Graphite,
                () -> gtStoneState(SusyStoneVariantBlock.StoneType.GRAPHITE),
                state -> gtStonePredicate(state, SusyStoneVariantBlock.StoneType.GRAPHITE), false);
        LIMESTONE = new StoneType(15, "limestone", SoundType.STONE, SusyOrePrefix.oreLimestone, SusyMaterials.Limestone,
                () -> gtStoneState(SusyStoneVariantBlock.StoneType.LIMESTONE),
                state -> gtStonePredicate(state, SusyStoneVariantBlock.StoneType.LIMESTONE), false);
        MICA = new StoneType(16, "mica", SoundType.STONE, SusyOrePrefix.oreMica, Materials.Mica,
                () -> gtStoneState(SusyStoneVariantBlock.StoneType.MICA),
                state -> gtStonePredicate(state, SusyStoneVariantBlock.StoneType.MICA), false);
        PHYLLITE = new StoneType(17, "phyllite", SoundType.STONE, SusyOrePrefix.orePhyllite, SusyMaterials.Phyllite,
                () -> gtStoneState(SusyStoneVariantBlock.StoneType.PHYLLITE),
                state -> gtStonePredicate(state, SusyStoneVariantBlock.StoneType.PHYLLITE), false);
        QUARTZITE = new StoneType(18, "quartzite", SoundType.STONE, SusyOrePrefix.oreQuartzite, Materials.Quartzite,
                () -> gtStoneState(SusyStoneVariantBlock.StoneType.QUARTZITE),
                state -> gtStonePredicate(state, SusyStoneVariantBlock.StoneType.QUARTZITE), false);
        SHALE = new StoneType(19, "shale", SoundType.STONE, SusyOrePrefix.oreShale, SusyMaterials.Shale,
                () -> gtStoneState(SusyStoneVariantBlock.StoneType.SHALE),
                state -> gtStonePredicate(state, SusyStoneVariantBlock.StoneType.SHALE), false);
        SLATE = new StoneType(20, "slate", SoundType.STONE, SusyOrePrefix.oreSlate, SusyMaterials.Slate,
                () -> gtStoneState(SusyStoneVariantBlock.StoneType.SLATE),
                state -> gtStonePredicate(state, SusyStoneVariantBlock.StoneType.SLATE), false);
        SOAPSTONE = new StoneType(21, "soapstone", SoundType.STONE, SusyOrePrefix.oreSoapstone, Materials.Soapstone,
                () -> gtStoneState(SusyStoneVariantBlock.StoneType.SOAPSTONE),
                state -> gtStonePredicate(state, SusyStoneVariantBlock.StoneType.SOAPSTONE), false);
        KIMBERLITE = new StoneType(22, "kimberlite", SoundType.STONE, SusyOrePrefix.oreKimberlite, SusyMaterials.Kimberlite,
                () -> gtStoneState(SusyStoneVariantBlock.StoneType.KIMBERLITE),
                state -> gtStonePredicate(state, SusyStoneVariantBlock.StoneType.KIMBERLITE), false);

    }
    private static IBlockState gtStoneState(SusyStoneVariantBlock.StoneType stoneType) {
        return SuSyBlocks.SUSY_STONE_BLOCKS.get(SusyStoneVariantBlock.StoneVariant.SMOOTH).getState(stoneType);
    }

    private static boolean gtStonePredicate(IBlockState state, SusyStoneVariantBlock.StoneType stoneType) {
        SusyStoneVariantBlock block = SuSyBlocks.SUSY_STONE_BLOCKS.get(SusyStoneVariantBlock.StoneVariant.SMOOTH);
        return state.getBlock() == block && block.getState(state) == stoneType;
    }
}
