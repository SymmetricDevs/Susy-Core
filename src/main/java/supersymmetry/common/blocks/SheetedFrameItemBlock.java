package supersymmetry.common.blocks;

import gregtech.api.unification.material.Material;
import gregtech.common.ConfigHolder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import supersymmetry.api.unification.ore.SusyOrePrefix;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class SheetedFrameItemBlock extends ItemBlock {

    private final BlockSheetedFrame frameBlock;

    public SheetedFrameItemBlock(BlockSheetedFrame block) {
        super(block);
        this.frameBlock = block;
        setHasSubtypes(true);
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    public IBlockState getBlockState(ItemStack stack) {
        return frameBlock.getStateFromMeta(getMetadata(stack.getItemDamage()));
    }

    @Nonnull
    @Override
    public String getItemStackDisplayName(@Nonnull ItemStack stack) {
        Material material = getBlockState(stack).getValue(frameBlock.variantProperty);
        //String localizedPartialName = net.minecraft.client.resources.I18n.format("tile.sheeted_frame");
        //return String.format(localizedPartialName, material.getLocalizedName());
        return SusyOrePrefix.sheetedFrame.getLocalNameForItem(material);
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        if (ConfigHolder.misc.debug) {
            tooltip.add("MetaItem Id: sheeted_frame" + frameBlock.getGtMaterial(stack.getMetadata()).toCamelCaseString());
        }
    }
}
