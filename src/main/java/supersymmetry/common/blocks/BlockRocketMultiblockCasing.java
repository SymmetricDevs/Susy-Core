package supersymmetry.common.blocks;

import javax.annotation.Nonnull;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

import gregtech.api.block.VariantBlock;

public class BlockRocketMultiblockCasing extends VariantBlock<BlockRocketMultiblockCasing.CasingType> {

    public BlockRocketMultiblockCasing() {
        super(Material.IRON);
        setTranslationKey("rocket_multiblock_casing");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(CasingType.VINYL_CEILING_TILE));
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos,
                                    @Nonnull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canRenderInLayer(@NotNull IBlockState state, @NotNull BlockRenderLayer layer) {
        return super.canRenderInLayer(state, layer);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isOpaqueCube(@NotNull IBlockState state) {
        return super.isOpaqueCube(state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getLightOpacity(@NotNull IBlockState state) {
        return super.getLightOpacity(state);
    }

    public enum CasingType implements IStringSerializable {

        VINYL_CEILING_TILE("vinyl_ceiling_tile"),
        CEILING_GRID_FILTER_UNIT("ceiling_grid_filter_unit"),
        VINYL_COMPOSITE_FLOORING("vinyl_composite_flooring"),
        AEROSPACE_GASKET("aerospace_gasket");

        private final String name;

        CasingType(String name) {
            this.name = name;
        }

        @Nonnull
        @Override
        public String getName() {
            return this.name;
        }
    }
}
