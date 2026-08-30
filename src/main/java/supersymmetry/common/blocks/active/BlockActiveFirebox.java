package supersymmetry.common.blocks.active;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import org.jetbrains.annotations.NotNull;

import gregtech.common.blocks.BlockFireboxCasing.FireboxCasingType;

public class BlockActiveFirebox extends RedstoneActiveBlock<FireboxCasingType> {

    public BlockActiveFirebox() {
        this(false);
    }

    protected BlockActiveFirebox(boolean inverted) {
        super(Material.IRON, inverted);
        setTranslationKey("firebox_active");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(FireboxCasingType.BRONZE_FIREBOX));
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    protected boolean isBloomEnabled(FireboxCasingType value) {
        return true;
    }
}
