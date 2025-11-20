package supersymmetry.common.blocks.rocketry;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import org.jetbrains.annotations.NotNull;

import gregtech.api.block.IStateHarvestLevel;
import supersymmetry.api.blocks.VariantHorizontalRotatableBlock;

public class BlockOuterHatch extends VariantHorizontalRotatableBlock<BlockOuterHatch.OuterHatchType> {

    public BlockOuterHatch() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("rocket_outer_hatch");
        setHardness(5f);
        setResistance(15f);
        setSoundType(SoundType.METAL);
        setDefaultState(getState(OuterHatchType.OUTER_HATCH));
        setHarvestLevel("wrench", 2);
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    public enum OuterHatchType implements IStringSerializable, IStateHarvestLevel {

        OUTER_HATCH("al_2219", 2);

        private final String name;
        private final int harvestLevel;

        OuterHatchType(String name, int harvestLevel) {
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
}
