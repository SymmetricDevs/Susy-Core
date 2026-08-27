package supersymmetry.common.blocks.rocketry;

import java.util.List;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.block.IStateHarvestLevel;
import supersymmetry.api.blocks.VariantHorizontalRotatableBlock;
import supersymmetry.api.rocketry.WeightedBlock;
import supersymmetry.common.blocks.SuSyBlocks;

public class BlockTurboPump extends VariantHorizontalRotatableBlock<BlockTurboPump.HPPType>
                            implements WeightedBlock<BlockTurboPump.HPPType> {

    public BlockTurboPump() {
        super(Material.IRON);
        setTranslationKey("rocket_turbopump");
        setHardness(5f);
        setResistance(15f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
    }

    public static HPPType getTypeFromBlockstate(IBlockState state) {
        for (HPPType value : HPPType.values()) {
            if (state.equals(SuSyBlocks.TURBOPUMP.getState(value, EnumFacing.NORTH)) || // this is so dumb but idk how
                                                                                        // else to do it :(
                    state.equals(SuSyBlocks.TURBOPUMP.getState(value, EnumFacing.SOUTH)) ||
                    state.equals(SuSyBlocks.TURBOPUMP.getState(value, EnumFacing.WEST)) ||
                    state.equals(SuSyBlocks.TURBOPUMP.getState(value, EnumFacing.EAST))) {
                return value;
            }
        }
        return null;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    public enum HPPType implements IStringSerializable, IStateHarvestLevel {

        LOW_SPEED("low_speed", 3, 300, 3), // RD-107 (Soyuz engine) according to some sketchy site
        MEDIUM_SPEED("medium_speed", 3, 1000, 4), // arbitrary value
        HIGH_SPEED("high_speed", 3, 2500, 5); // F-1 (Saturn V first stage)

        private String name;
        private int harvestLevel;
        private double throughput; // kg/s
        private int minNozzleLength;

        HPPType(String name, int harvestLevel, double throughput, int minNozzleLength) {
            this.name = name;
            this.harvestLevel = harvestLevel;
            this.throughput = throughput;
            this.minNozzleLength = minNozzleLength;
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

        public int getMinNozzleLength() {
            return this.minNozzleLength;
        }
    }

    @Override
    public double getMass(HPPType type) {
        return switch (type) {
            case LOW_SPEED -> 300;
            case MEDIUM_SPEED -> 1000;
            case HIGH_SPEED -> 3000;
        };
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(@NotNull ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag advanced) {
        tooltip.add(I18n.format("susy.tooltip.mass", getMass(stack)));
    }
}
