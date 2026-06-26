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
import gregtech.api.block.VariantBlock;
import supersymmetry.api.rocketry.WeightedBlock;

public class BlockRocketNozzle extends VariantBlock<BlockRocketNozzle.NozzleShapeType>
                               implements WeightedBlock<BlockRocketNozzle.NozzleShapeType> {

    public BlockRocketNozzle() {
        super(Material.IRON);
        setTranslationKey("rocket_nozzle");
        setHardness(7f);
        setResistance(25f);
        setSoundType(SoundType.METAL);
        setDefaultState(getState(NozzleShapeType.BELL_NOZZLE));
        setHarvestLevel("wrench", 4);
    }

    public enum NozzleShapeType implements IStringSerializable, IStateHarvestLevel {

        BELL_NOZZLE("bell_basic", 4);

        private String name;
        private int harvestLevel;

        NozzleShapeType(String name, int harvestLevel) {
            this.name = name;
            this.harvestLevel = harvestLevel;
        }

        @Override
        public int getHarvestLevel(IBlockState iBlockState) {
            return this.harvestLevel;
        }

        @Override
        public String getHarvestTool(IBlockState state) {
            return "wrench";
        }

        @Override
        public String getName() {
            return this.name;
        }
    }

    @Override
    public double getMass(NozzleShapeType type) {
        return switch (type) {
            case BELL_NOZZLE -> 70;
        };
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(@NotNull ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag advanced) {
        tooltip.add(I18n.format("susy.tooltip.mass", getMass(stack)));
    }
}
