package supersymmetry.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import org.jetbrains.annotations.NotNull;

import supersymmetry.api.blocks.VariantHorizontalRotatableBlock;

public class BlockMetallurgy2 extends VariantHorizontalRotatableBlock<BlockMetallurgy2.BlockMetallurgy2Type> {

    public BlockMetallurgy2() {
        super(Material.IRON);
        setTranslationKey("metallurgy_2");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(BlockMetallurgy2Type.FLYING_SHEAR_SAW));
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    public enum BlockMetallurgy2Type implements IStringSerializable {

        FLYING_SHEAR_SAW("flying_shear_saw");

        private String name;

        BlockMetallurgy2Type(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
