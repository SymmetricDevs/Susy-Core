package supersymmetry.common.blocks.rocketry;

import java.util.List;

import net.minecraft.block.SoundType;
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
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.ore.OrePrefix;
import supersymmetry.api.blocks.VariantDirectionalCoverableBlock;
import supersymmetry.api.rocketry.WeightedBlock;
import supersymmetry.common.materials.SusyMaterials;

public class BlockTankShell extends VariantDirectionalCoverableBlock<BlockTankShell.TankCoverType>
                            implements WeightedBlock<BlockTankShell.TankCoverType> {

    public BlockTankShell() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("rocket_tank_shell");
        setHardness(5f);
        setResistance(15f);
        setSoundType(SoundType.METAL);
        setDefaultState(getState(TankCoverType.TANK_SHELL));
        setHarvestLevel("wrench", 2);
        validCover = itemStack -> OreDictUnifier.get(OrePrefix.plate, SusyMaterials.AluminiumAlloyMg6)
                .isItemEqual(itemStack);
    }

    public enum TankCoverType implements IStringSerializable, IStateHarvestLevel {

        TANK_SHELL("al_2219", 2),
        STEEL_SHELL("steel", 3);

        private String name;
        private int harvestLevel;

        TankCoverType(String name, int harvestLevel) {
            this.name = name;
            this.harvestLevel = harvestLevel;
        }

        @Override
        public int getHarvestLevel(IBlockState iBlockState) {
            return harvestLevel;
        }

        @Override
        public String getHarvestTool(IBlockState state) {
            return "wrench";
        }

        public String getName() {
            return name;
        }
    }

    @Override
    public double getMass(TankCoverType type) {
        // Using a 4.7mm thickness
        return switch (type) {
            case TANK_SHELL -> 12.2;
            case STEEL_SHELL -> 36.9;
        };
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(@NotNull ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag advanced) {
        tooltip.add(I18n.format("susy.tooltip.mass", getMass(stack)));
    }
}
