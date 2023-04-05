package supersymmetry.loaders;

import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import supersymmetry.common.blocks.SusyBlockStoneSmooth;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.materials.SusyMaterials;


public class SusyOreDictionaryLoader {
    public static void init(){
        OreDictUnifier.registerOre(SuSyBlocks.SUSY_STONE_SMOOTH.getItemVariant(SusyBlockStoneSmooth.BlockType.GABBRO, 1), OrePrefix.stone, SusyMaterials.Gabbro);
        OreDictUnifier.registerOre(SuSyBlocks.SUSY_STONE_SMOOTH.getItemVariant(SusyBlockStoneSmooth.BlockType.GNEISS, 1), OrePrefix.stone, SusyMaterials.Gneiss);
        OreDictUnifier.registerOre(SuSyBlocks.SUSY_STONE_SMOOTH.getItemVariant(SusyBlockStoneSmooth.BlockType.GRAPHITE, 1), OrePrefix.stone, Materials.Graphite);
        OreDictUnifier.registerOre(SuSyBlocks.SUSY_STONE_SMOOTH.getItemVariant(SusyBlockStoneSmooth.BlockType.LIMESTONE, 1), OrePrefix.stone, SusyMaterials.Limestone);
        OreDictUnifier.registerOre(SuSyBlocks.SUSY_STONE_SMOOTH.getItemVariant(SusyBlockStoneSmooth.BlockType.MICA, 1), OrePrefix.stone, Materials.Mica);
        OreDictUnifier.registerOre(SuSyBlocks.SUSY_STONE_SMOOTH.getItemVariant(SusyBlockStoneSmooth.BlockType.PHYLLITE, 1), OrePrefix.stone, SusyMaterials.Phyllite);
        OreDictUnifier.registerOre(SuSyBlocks.SUSY_STONE_SMOOTH.getItemVariant(SusyBlockStoneSmooth.BlockType.QUARTZITE, 1), OrePrefix.stone, Materials.Quartzite);
        OreDictUnifier.registerOre(SuSyBlocks.SUSY_STONE_SMOOTH.getItemVariant(SusyBlockStoneSmooth.BlockType.SHALE, 1), OrePrefix.stone, SusyMaterials.Shale);
        OreDictUnifier.registerOre(SuSyBlocks.SUSY_STONE_SMOOTH.getItemVariant(SusyBlockStoneSmooth.BlockType.SLATE, 1), OrePrefix.stone, SusyMaterials.Slate);
        OreDictUnifier.registerOre(SuSyBlocks.SUSY_STONE_SMOOTH.getItemVariant(SusyBlockStoneSmooth.BlockType.SOAPSTONE, 1), OrePrefix.stone, Materials.Soapstone);

    }
}
