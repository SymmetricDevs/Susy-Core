package supersymmetry.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import org.jetbrains.annotations.NotNull;

public class BlockActiveSinteringBrick extends RedstoneActiveBlock<BlockActiveSinteringBrick.ActiveSinteringBrickType> {

    public BlockActiveSinteringBrick() {
        this(false);
    }

    protected BlockActiveSinteringBrick(boolean inverted) {
        super(Material.ROCK, inverted);
        setTranslationKey("sintering_brick_active");
        setHardness(0.5f);
        setSoundType(SoundType.STONE);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(ActiveSinteringBrickType.BRICK));
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.SOLID;
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    protected boolean isBloomEnabled(ActiveSinteringBrickType value) {
        return true;
    }

    public enum ActiveSinteringBrickType implements IStringSerializable {

        BRICK("sintering_block_brick"),
        MAGNETOPLATED("sintering_block_magnetoplated");

        public final String name;

        ActiveSinteringBrickType(String name) {
            this.name = name;
        }

        @NotNull @Override
        public String getName() {
            return this.name;
        }

        public String toString() {
            return this.getName();
        }
    }
}
