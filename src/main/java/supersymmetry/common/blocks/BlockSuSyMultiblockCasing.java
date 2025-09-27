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

    @Override
    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos,
                                    @Nonnull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canRenderInLayer(@NotNull IBlockState state, @NotNull BlockRenderLayer layer) {
        if (state.getValue(VARIANT) == CasingType.COALESCENCE_PLATE) {
            return layer == BlockRenderLayer.CUTOUT;
        }
        return super.canRenderInLayer(state, layer);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isOpaqueCube(@NotNull IBlockState state) {
        if (state.getValue(VARIANT) == CasingType.COALESCENCE_PLATE) {
            return false;
        }
        return super.isOpaqueCube(state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getLightOpacity(@NotNull IBlockState state) {
        if (state.getValue(VARIANT) == CasingType.COALESCENCE_PLATE) {
            return 3; // Some random number IDK
        }
        return super.getLightOpacity(state);
    }

    public enum CasingType implements IStringSerializable {

        SILICON_CARBIDE_CASING("silicon_carbide_casing"),
        SIEVE_TRAY("sieve_tray"),
        STRUCTURAL_PACKING("structural_packing"),
        ULV_STRUCTURAL_CASING("ulv_structural_casing"),
        DRONE_PAD("drone_pad"),
        MONEL_500_CASING("monel_casing"),
        MONEL_500_PIPE("monel_casing_pipe"),
        COPPER_PIPE("copper_casing_pipe"),
        HEAVY_DUTY_PAD("heavy_duty_pad"),
        ADVANCED_REFRACTORY_LINING("advanced_refractory_lining"),
        COALESCENCE_PLATE("coalescence_plate");

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
