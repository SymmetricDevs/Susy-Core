package supersymmetry.common.blocks;

import gregtech.api.block.IStateHarvestLevel;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.blocks.VariantHorizontalRotatableBlock;

import javax.annotation.Nonnull;

public class BlockBWEConveyorBelt extends VariantHorizontalRotatableBlock<BlockBWEConveyorBelt.BWEConveyorType> {

    public BlockBWEConveyorBelt() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("bwe_conveyor_belt");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(BWEConveyorType.DEFAULT));
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @NotNull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.SOLID;
    }

    public enum BWEConveyorType implements IStringSerializable, IStateHarvestLevel {

        DEFAULT("default", 2);

        private final String name;
        private final int harvestLevel;

        BWEConveyorType(String name, int harvestLevel) {
            this.name = name;
            this.harvestLevel = harvestLevel;
        }

        @Nonnull
        public String getName() {
            return this.name;
        }

        public int getHarvestLevel(IBlockState state) {
            return this.harvestLevel;
        }

        public String getHarvestTool(IBlockState state) {
            return "wrench";
        }
    }
}
