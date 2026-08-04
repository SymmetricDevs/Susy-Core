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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;

public class BlockGuidanceSystem extends VariantBlock<BlockGuidanceSystem.GuidanceSystemType> {

    public BlockGuidanceSystem() {
        super(Material.IRON);
        setTranslationKey("guidance_system");
        setHardness(5f);
        setResistance(15f);
        setSoundType(SoundType.METAL);
        setDefaultState(getState(BlockGuidanceSystem.GuidanceSystemType.SOYUZ));
        setHarvestLevel("wrench", 4);
    }

    public enum GuidanceSystemType implements IStringSerializable, IStateHarvestLevel {

        SOYUZ("soyuz", 4, 0.9);

        String name;
        int harvest;
        double successChanceMultiplier;

        GuidanceSystemType(String name, int harvest, double successChanceMultiplier) {
            this.name = name;
            this.harvest = harvest;
            this.successChanceMultiplier = successChanceMultiplier;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public int getHarvestLevel(IBlockState iBlockState) {
            return harvest;
        }

        public double getSuccessChanceMultiplier() {
            return successChanceMultiplier;
        }

        @Override
        public String getHarvestTool(IBlockState state) {
            return IStateHarvestLevel.super.getHarvestTool(state);
        }
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("susy.tooltip.guidance_multiplier", getState(stack).successChanceMultiplier));
    }
}
