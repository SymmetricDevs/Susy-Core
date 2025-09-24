package supersymmetry.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import org.jetbrains.annotations.NotNull;

import supersymmetry.api.blocks.VariantDirectionalRotatableBlock;

public class BlockMetallurgy extends VariantDirectionalRotatableBlock<BlockMetallurgy.BlockMetallurgyType> {

    public BlockMetallurgy() {
        super(Material.IRON);
        setTranslationKey("metallurgy");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(BlockMetallurgyType.HYDRAULIC_CYLINDER));
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    public enum BlockMetallurgyType implements IStringSerializable {

        HYDRAULIC_CYLINDER("hydraulic_cylinder");

        private String name;

        BlockMetallurgyType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
