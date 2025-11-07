package supersymmetry.common.blocks;

import gregtech.api.block.VariantActiveBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class BlocksActiveCasing extends VariantActiveBlock<BlocksActiveCasing.ActiveBlockType> {

    public BlocksActiveCasing() {
        super(Material.IRON);
        setTranslationKey("active_casing");
        setHardness(0.5f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(ActiveBlockType.BASIC_INTAKE_CASING));
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

    public enum ActiveBlockType implements IStringSerializable {

        BASIC_INTAKE_CASING("basic_intake_casing");

        public final String name;

        ActiveBlockType(String name) {
            this.name = name;
        }

        @Nonnull
        @Override
        public String getName() {
            return this.name;
        }

        public String toString() {
            return this.getName();
        }
    }
}
