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

import gregtech.api.block.VariantActiveBlock;

public class BlockSinteringBrick extends VariantActiveBlock<BlockSinteringBrick.SinteringBrickType> {

    public BlockSinteringBrick() {
        super(Material.ROCK);
        setTranslationKey("sintering_brick");
        setHardness(0.5f);
        setSoundType(SoundType.STONE);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(SinteringBrickType.BRICK));
    }

    @NotNull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.SOLID;
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    public enum SinteringBrickType implements IStringSerializable {

        BRICK("sintering_block_brick", false),
        MAGNETOPLATED("sintering_block_magnetoplated", true),
        BRICK_BLOOM("sintering_block_brick_bloom_deco", false)
        MAGNETOPLATED_BLOOM("sintering_block_magnetoplated_bloom_deco", false);
        

        public final String name;
        public final boolean canResistPlasma;

        SinteringBrickType(String name, boolean canResistPlasma) {
            this.name = name;
            this.canResistPlasma = canResistPlasma;
        }

        @NotNull
        @Override
        public String getName() {
            return this.name;
        }

        public String toString() {
            return this.getName();
        }
    }
}
