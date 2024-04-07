package supersymmetry.loaders;

import gregtech.api.unification.OreDictUnifier;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.StoneVariantBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.blocks.SusyStoneVariantBlock;
import supersymmetry.common.item.SuSyMetaItems;


public class SusyOreDictionaryLoader {
    public static void init(){
        loadStoneOredict();
    }

    public static void loadStoneOredict(){

        for (SusyStoneVariantBlock.StoneType type : SusyStoneVariantBlock.StoneType.values()) {
            ItemStack smooth = SuSyBlocks.SUSY_STONE_BLOCKS.get(SusyStoneVariantBlock.StoneVariant.SMOOTH).getItemVariant(type);
            ItemStack cobble = SuSyBlocks.SUSY_STONE_BLOCKS.get(SusyStoneVariantBlock.StoneVariant.COBBLE).getItemVariant(type);
            OreDictUnifier.registerOre(smooth, type.getOrePrefix(), type.getMaterial());
            OreDictionary.registerOre("stone", smooth);
            OreDictionary.registerOre("cobblestone", cobble);
        }

        for (StoneVariantBlock.StoneType type : StoneVariantBlock.StoneType.values()) {
            ItemStack smooth = MetaBlocks.STONE_BLOCKS.get(StoneVariantBlock.StoneVariant.SMOOTH).getItemVariant(type);
            ItemStack cobble = MetaBlocks.STONE_BLOCKS.get(StoneVariantBlock.StoneVariant.COBBLE).getItemVariant(type);
            OreDictionary.registerOre("stone", smooth);
            OreDictionary.registerOre("cobblestone", cobble);
        }

        // For IR railbeds
        ItemStack concreteLightSmooth = MetaBlocks.STONE_BLOCKS.get(StoneVariantBlock.StoneVariant.SMOOTH).getItemVariant(StoneVariantBlock.StoneType.CONCRETE_LIGHT);
        OreDictionary.registerOre("railBed", concreteLightSmooth);

        // For IR tracks
        ItemStack trackSegmentStack = SuSyMetaItems.TRACK_SEGMENT.getStackForm();
        OreDictionary.registerOre("trackMaglev", trackSegmentStack);
    }
}
