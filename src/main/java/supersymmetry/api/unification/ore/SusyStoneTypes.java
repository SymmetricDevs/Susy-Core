package supersymmetry.api.unification.ore;

import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.ore.StoneType;
import net.minecraft.block.SoundType;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.api.block.SusyBlockStoneSmooth;
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

    public SusyStoneTypes(){
    }
    public static void init(){
        GABBRO = new StoneType(12, "gabbro", SoundType.STONE, SusyOrePrefix.oreGabbro, SusyMaterials.Gabbro,
                () -> SuSyBlocks.SUSY_STONE_SMOOTH.getState(SusyBlockStoneSmooth.BlockType.GABBRO),
                state -> state.getBlock() instanceof SusyBlockStoneSmooth && ((SusyBlockStoneSmooth) state.getBlock()).getVariant(state) == SusyBlockStoneSmooth.BlockType.GABBRO, true);
        GNEISS = new StoneType(13, "gneiss", SoundType.STONE, SusyOrePrefix.oreGneiss, SusyMaterials.Gneiss,
                () -> SuSyBlocks.SUSY_STONE_SMOOTH.getState(SusyBlockStoneSmooth.BlockType.GNEISS),
                state -> state.getBlock() instanceof SusyBlockStoneSmooth && ((SusyBlockStoneSmooth) state.getBlock()).getVariant(state) == SusyBlockStoneSmooth.BlockType.GNEISS, false);
        GRAPHITE = new StoneType(14, "graphite", SoundType.STONE, SusyOrePrefix.oreGraphite, Materials.Graphite,
                () -> SuSyBlocks.SUSY_STONE_SMOOTH.getState(SusyBlockStoneSmooth.BlockType.GRAPHITE),
                state -> state.getBlock() instanceof SusyBlockStoneSmooth && ((SusyBlockStoneSmooth) state.getBlock()).getVariant(state) == SusyBlockStoneSmooth.BlockType.GRAPHITE, false);
        LIMESTONE = new StoneType(15, "limestone", SoundType.STONE, SusyOrePrefix.oreLimestone, SusyMaterials.Limestone,
                () -> SuSyBlocks.SUSY_STONE_SMOOTH.getState(SusyBlockStoneSmooth.BlockType.LIMESTONE),
                state -> state.getBlock() instanceof SusyBlockStoneSmooth && ((SusyBlockStoneSmooth) state.getBlock()).getVariant(state) == SusyBlockStoneSmooth.BlockType.LIMESTONE, false);
        MICA = new StoneType(16, "mica", SoundType.STONE, SusyOrePrefix.oreMica, Materials.Mica,
                () -> SuSyBlocks.SUSY_STONE_SMOOTH.getState(SusyBlockStoneSmooth.BlockType.MICA),
                state -> state.getBlock() instanceof SusyBlockStoneSmooth && ((SusyBlockStoneSmooth) state.getBlock()).getVariant(state) == SusyBlockStoneSmooth.BlockType.MICA, false);
        PHYLLITE = new StoneType(17, "phyllite", SoundType.STONE, SusyOrePrefix.orePhyllite, SusyMaterials.Phyllite,
                () -> SuSyBlocks.SUSY_STONE_SMOOTH.getState(SusyBlockStoneSmooth.BlockType.PHYLLITE),
                state -> state.getBlock() instanceof SusyBlockStoneSmooth && ((SusyBlockStoneSmooth) state.getBlock()).getVariant(state) == SusyBlockStoneSmooth.BlockType.PHYLLITE, false);
        QUARTZITE = new StoneType(18, "quartzite", SoundType.STONE, SusyOrePrefix.oreQuartzite, Materials.Quartzite,
                () -> SuSyBlocks.SUSY_STONE_SMOOTH.getState(SusyBlockStoneSmooth.BlockType.QUARTZITE),
                state -> state.getBlock() instanceof SusyBlockStoneSmooth && ((SusyBlockStoneSmooth) state.getBlock()).getVariant(state) == SusyBlockStoneSmooth.BlockType.QUARTZITE, false);
        SHALE = new StoneType(19, "shale", SoundType.STONE, SusyOrePrefix.oreShale, SusyMaterials.Shale,
                () -> SuSyBlocks.SUSY_STONE_SMOOTH.getState(SusyBlockStoneSmooth.BlockType.SHALE),
                state -> state.getBlock() instanceof SusyBlockStoneSmooth && ((SusyBlockStoneSmooth) state.getBlock()).getVariant(state) == SusyBlockStoneSmooth.BlockType.SHALE, false);
        SLATE = new StoneType(20, "slate", SoundType.STONE, SusyOrePrefix.oreSlate, SusyMaterials.Slate,
                () -> SuSyBlocks.SUSY_STONE_SMOOTH.getState(SusyBlockStoneSmooth.BlockType.SLATE),
                state -> state.getBlock() instanceof SusyBlockStoneSmooth && ((SusyBlockStoneSmooth) state.getBlock()).getVariant(state) == SusyBlockStoneSmooth.BlockType.SLATE, false);
        SOAPSTONE = new StoneType(21, "soapstone", SoundType.STONE, SusyOrePrefix.oreSoapstone, Materials.Soapstone,
                () -> SuSyBlocks.SUSY_STONE_SMOOTH.getState(SusyBlockStoneSmooth.BlockType.SOAPSTONE),
                state -> state.getBlock() instanceof SusyBlockStoneSmooth && ((SusyBlockStoneSmooth) state.getBlock()).getVariant(state) == SusyBlockStoneSmooth.BlockType.SOAPSTONE, false);
    }

}
