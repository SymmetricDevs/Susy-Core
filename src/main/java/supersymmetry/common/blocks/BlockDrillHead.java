package supersymmetry.common.blocks;

import javax.annotation.Nonnull;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import org.jetbrains.annotations.NotNull;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;

public class BlockDrillHead extends VariantBlock<BlockDrillHead.DrillHeadType> {

    public BlockDrillHead() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("drill_head");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(DrillHeadType.STEEL));
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    // TODO: MAKE THIS CREATE MINING PARTICLES WHEN MINING DRILL IS ACTIVE
    // TODO: MAKE THIS PLAY A LOUD MINING NOISE, PERHAPS HAVE STATUS EFFECTS FOR PLAYERS WHO COME NEAR THE MINING

    @NotNull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.SOLID;
    }

    public enum DrillHeadType implements IStringSerializable, IStateHarvestLevel {

        STEEL("steel", 1);

        private final String name;
        private final int harvestLevel;

        DrillHeadType(String name, int harvestLevel) {
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
