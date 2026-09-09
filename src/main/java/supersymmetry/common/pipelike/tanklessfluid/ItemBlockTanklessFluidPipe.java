package supersymmetry.common.pipelike.tanklessfluid;

import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.pipenet.block.material.BlockMaterialPipe;
import gregtech.api.pipenet.block.material.ItemBlockMaterialPipe;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.ConfigHolder;
import lombok.val;
import supersymmetry.api.unification.material.properties.TanklessFluidPipeProperties;

public class ItemBlockTanklessFluidPipe
                                        extends
                                        ItemBlockMaterialPipe<TanklessFluidPipeType, TanklessFluidPipeProperties> {

    public ItemBlockTanklessFluidPipe(BlockTanklessFluidPipe block) {
        super(block);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@NotNull ItemStack stack, @Nullable World worldIn, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        val pipeProperties = blockPipe.createItemProperties(stack);
        tooltip.add(I18n.format("susy.tankless_fluid_pipe.tooltip"));
        tooltip.add(I18n.format("gregtech.universal.tooltip.fluid_transfer_rate", pipeProperties.getThroughput()));
        tooltip.add(I18n.format("gregtech.fluid_pipe.max_temperature", pipeProperties.getMaxFluidTemperature()));
        tooltip.add(I18n.format("susy.tankless_fluid_pipe.tooltip.resistance", pipeProperties.getResistance()));

        pipeProperties.appendTooltips(tooltip, false, false);

        if (TooltipHelper.isShiftDown()) {
            tooltip.add(I18n.format("gregtech.tool_action.wrench.connect_and_block"));
            tooltip.add(I18n.format("susy.tool_action.hammer.toggle_flange"));
            tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
            tooltip.add(I18n.format("gregtech.tool_action.crowbar"));
        }

        val blockMaterialPipe = (BlockMaterialPipe<?, ?, ?>) blockPipe;
        if (ConfigHolder.misc.debug) {
            tooltip.add("MetaItem Id: " + blockMaterialPipe.getPrefix().name +
                    blockMaterialPipe.getItemMaterial(stack).toCamelCaseString());
        }
    }
}
