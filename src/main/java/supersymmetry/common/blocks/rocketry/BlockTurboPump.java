package supersymmetry.common.blocks.rocketry;

import java.util.List;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.block.IStateHarvestLevel;
import supersymmetry.api.blocks.VariantDirectionalRotatableBlock;
import supersymmetry.api.rocketry.WeightedBlock;

public class BlockTurboPump extends VariantDirectionalRotatableBlock<BlockTurboPump.HPPType>
                            implements WeightedBlock<BlockTurboPump.HPPType> {

    public BlockTurboPump() {
        super(Material.IRON);
        setTranslationKey("rocket_turbopump");
        setHardness(5f);
        setResistance(15f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    public enum HPPType implements IStringSerializable, IStateHarvestLevel {

        BASIC("basic", 3, 2000);

        private String name;
        private int harvestLevel;
        private double throughput; // kg/s

        HPPType(String name, int harvestLevel, double throughput) {
            this.name = name;
            this.harvestLevel = harvestLevel;
            this.throughput = throughput;
        }

        @Override
        public int getHarvestLevel(IBlockState iBlockState) {
            return harvestLevel;
        }

        @Override
        public String getHarvestTool(IBlockState state) {
            return "wrench";
        }

        @Override
        public String getName() {
            return this.name;
        }

        public double getThroughput() {
            return this.throughput;
        }
    }

    @Override
    public double getMass(HPPType type) {
        return switch (type) {
            case BASIC -> 1100;
        };
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(@NotNull ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag advanced) {
        tooltip.add(I18n.format("susy.tooltip.mass", getMass(stack)));
    }
}
