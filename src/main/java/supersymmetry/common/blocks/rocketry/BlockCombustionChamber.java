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

public class BlockCombustionChamber extends VariantBlock<BlockCombustionChamber.CombustionType>
                                    implements WeightedBlock<BlockCombustionChamber.CombustionType> {

    public BlockCombustionChamber() {
        super(Material.IRON);
        setTranslationKey("rocket_combustion_chamber");
        setHardness(5f);
        setResistance(15f);
        setSoundType(SoundType.METAL);
        setDefaultState(getState(CombustionType.BIPROPELLANT));
        setHarvestLevel("wrench", 4);
    }

    public enum CombustionType implements IStringSerializable, IStateHarvestLevel {

        BIPROPELLANT("bipropellant", 4, 2),
        MONOPROPELLANT("monopropellant", 4, 1),
        OXIDISER("oxidiser", 4, 2);

        String name;
        int harvest;
        int pumps;

        CombustionType(String name, int harvest, int pumps) {
            this.name = name;
            this.harvest = harvest;
            this.pumps = pumps;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public int getHarvestLevel(IBlockState iBlockState) {
            return harvest;
        }

        @Override
        public String getHarvestTool(IBlockState state) {
            return "wrench";
        }

        public int getMinPumps() {
            return pumps;
        }
    }

    @Override
    public double getMass(CombustionType type) {
        return switch (type) {
            case BIPROPELLANT -> 280;
            case MONOPROPELLANT -> 120;
            case OXIDISER -> 280;
        };
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(@NotNull ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag advanced) {
        tooltip.add(I18n.format("susy.tooltip.mass", getMass(stack)));
    }
}
