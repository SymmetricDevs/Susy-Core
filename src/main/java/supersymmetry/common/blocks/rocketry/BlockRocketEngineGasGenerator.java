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
import supersymmetry.api.blocks.VariantHorizontalRotatableBlock;
import supersymmetry.api.rocketry.WeightedBlock;

public class BlockRocketEngineGasGenerator extends
                                           VariantHorizontalRotatableBlock<BlockRocketEngineGasGenerator.GasGeneratorType>
                                           implements
                                           WeightedBlock<BlockRocketEngineGasGenerator.GasGeneratorType> {

    public BlockRocketEngineGasGenerator() {
        super(Material.IRON);
        setTranslationKey("rocket_engine_gas_generator");
        setHardness(5f);
        setResistance(15f);
        setSoundType(SoundType.METAL);
        setDefaultState(getState(GasGeneratorType.OPEN_CYCLE));
        setHarvestLevel("wrench", 4);
    }

    @Override
    public double getMass(GasGeneratorType type) {
        return switch (type) {
            case OPEN_CYCLE -> 40;
            case STAGED_COMBUSTION -> 200;
        };
    }

    public enum GasGeneratorType implements IStringSerializable, IStateHarvestLevel {

        OPEN_CYCLE("open_cycle", 4, 0.8),
        STAGED_COMBUSTION("staged_combustion", 4, 1.0);

        private String name;
        private int harvest;
        private double efficiency;

        GasGeneratorType(String name, int harvest, double efficiency) {
            this.name = name;
            this.harvest = harvest;
            this.efficiency = efficiency;
        }

        @Override
        public int getHarvestLevel(IBlockState iBlockState) {
            return harvest;
        }

        @Override
        public String getHarvestTool(IBlockState state) {
            return "wrench";
        }

        @Override
        public String getName() {
            return name;
        }

        public double getEfficiency() {
            return this.efficiency;
        }
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(@NotNull ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag advanced) {
        tooltip.add(I18n.format("susy.tooltip.mass", getMass(stack)));
    }
}
