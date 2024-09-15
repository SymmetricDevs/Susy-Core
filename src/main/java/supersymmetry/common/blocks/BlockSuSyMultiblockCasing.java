package supersymmetry.common.blocks;

import gregtech.api.block.VariantBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class BlockSuSyMultiblockCasing extends VariantBlock<BlockSuSyMultiblockCasing.CasingType> {

    public BlockSuSyMultiblockCasing() {
        super(Material.IRON);
        setTranslationKey("susy_multiblock_casing");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(CasingType.SILICON_CARBIDE_CASING));
    }

    @SuppressWarnings("deprecation")
    @NotNull
    @Override
    public BlockFaceShape getBlockFaceShape(@NotNull IBlockAccess worldIn, @NotNull IBlockState state, @NotNull BlockPos pos, @NotNull EnumFacing face) {
        if (state == getState(CasingType.DRONE_PAD)) {
            return BlockFaceShape.CENTER_BIG;
        }
        return super.getBlockFaceShape(worldIn, state, pos, face);
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    public enum CasingType implements IStringSerializable {

        SILICON_CARBIDE_CASING("silicon_carbide_casing"),
        SIEVE_TRAY("sieve_tray"),
        STRUCTURAL_PACKING("structural_packing"),
        ULV_STRUCTURAL_CASING("ulv_structural_casing"),
        DRONE_PAD("drone_pad");

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
