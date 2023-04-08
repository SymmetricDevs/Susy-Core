package supersymmetry.loaders;

import gregtech.api.unification.OreDictUnifier;
import net.minecraft.item.ItemStack;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.blocks.SusyStoneVariantBlock;


public class SusyOreDictionaryLoader {
    public static void init(){
        for (SusyStoneVariantBlock.StoneType type : SusyStoneVariantBlock.StoneType.values()) {
            ItemStack item = SuSyBlocks.SUSY_STONE_BLOCKS.get(SusyStoneVariantBlock.StoneVariant.SMOOTH).getItemVariant(type);
            OreDictUnifier.registerOre(item, type.getOrePrefix(), type.getMaterial());
        }
    }
}
