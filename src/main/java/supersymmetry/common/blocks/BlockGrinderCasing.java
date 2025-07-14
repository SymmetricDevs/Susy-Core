package supersymmetry.common.blocks;

import gregtech.api.block.VariantBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

public class BlockGrinderCasing extends VariantBlock<BlockGrinderCasing.Type> {

    public BlockGrinderCasing() {
        super(Material.IRON);
        setTranslationKey("grinder_casing");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(Type.ABRASION_RESISTANT_CASING));
    }

    @Override
    public boolean canCreatureSpawn(
            @NotNull IBlockState state,
            @NotNull IBlockAccess world,
            @NotNull BlockPos pos,
            @NotNull SpawnPlacementType type) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canRenderInLayer(@NotNull IBlockState state, @NotNull BlockRenderLayer layer) {
        if (state.getValue(VARIANT) == Type.INTERMEDIATE_DIAPHRAGM) {
            return layer == BlockRenderLayer.CUTOUT;
        }
        return super.canRenderInLayer(state, layer);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isOpaqueCube(@NotNull IBlockState state) {
        if (state.getValue(VARIANT) == Type.INTERMEDIATE_DIAPHRAGM) {
            return false;
        }
        return super.isOpaqueCube(state);
    }

    public enum Type implements IStringSerializable {

        ABRASION_RESISTANT_CASING("abrasion_resistant_casing"),
        HYDRAULIC_MECHANICAL_GEARBOX("hydraulic_mechanical_gearbox"),
        WEAR_RESISTANT_LINED_MILL_SHELL("wear_resistant_lined_mill_shell"),
        WEAR_RESISTANT_LINED_SHELL_HEAD("wear_resistant_lined_shell_head"),
        INTERMEDIATE_DIAPHRAGM("intermediate_diaphragm"),
        ;

        private final String name;

        Type(String name) {
            this.name = name;
        }

        @NotNull
        public String getName() {
            return name;
        }
    }
}
