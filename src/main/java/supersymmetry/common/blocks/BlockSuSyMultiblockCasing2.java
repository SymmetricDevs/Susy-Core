package supersymmetry.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import org.jspecify.annotations.NonNull;

import gregtech.api.block.VariantBlock;

public class BlockSuSyMultiblockCasing2 extends VariantBlock<BlockSuSyMultiblockCasing2.CasingType> {

    public BlockSuSyMultiblockCasing2() {
        super(Material.IRON);
        setTranslationKey("susy_multiblock_casing2");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(CasingType.LIGHT_STAINLESS_CASING));
    }

    @Override
    public boolean canCreatureSpawn(@NonNull IBlockState state, @NonNull IBlockAccess world, @NonNull BlockPos pos,
                                    EntityLiving.@NonNull SpawnPlacementType type) {
        return false;
    }

    public enum CasingType implements IStringSerializable {

        LIGHT_STAINLESS_CASING("light_stainless_casing");

        private final String name;

        CasingType(String name) {
            this.name = name;
        }

        @NonNull @Override
        public String getName() {
            return this.name;
        }
    }
}
