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
import gregtech.api.block.VariantBlock;
import supersymmetry.api.rocketry.WeightedBlock;

public class BlockLifeSupport extends VariantBlock<BlockLifeSupport.Ability>
                              implements WeightedBlock<BlockLifeSupport.Ability> {

    public BlockLifeSupport() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("spacecraft_life_support");
        setHardness(5f);
        setResistance(15f);
        setSoundType(SoundType.METAL);
        setDefaultState(getState(Ability.OXYGEN_REGEN));
        setHarvestLevel("wrench", 4);
    }

    public enum Ability implements IStringSerializable, IStateHarvestLevel {

        OXYGEN_REGEN("oxygen_regen", 4);

        private String name;
        private int harvest;

        Ability(String name, int h) {
            this.name = name;
            this.harvest = h;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public int getHarvestLevel(IBlockState iBlockState) {
            return harvest;
        }
    }

    @Override
    public double getMass(BlockLifeSupport.Ability type) {
        return switch (type) {
            case OXYGEN_REGEN -> 1500;
        };
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(@NotNull ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag advanced) {
        tooltip.add(I18n.format("susy.tooltip.mass", getMass(stack)));

        tooltip.add(I18n.format("susy.tooltip.power_consumption_space", 1500));
    }
}
