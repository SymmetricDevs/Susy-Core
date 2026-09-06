package supersymmetry.common.blocks.active;

import java.util.Collections;
import java.util.List;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import org.jetbrains.annotations.NotNull;

import gregicality.multiblocks.common.block.blocks.BlockUniqueCasing.UniqueCasingType;

public class BlockActiveHeatVent extends RedstoneActiveBlock<UniqueCasingType> {

    private static final List<UniqueCasingType> BLOOM_VARIANTS = Collections.singletonList(
            UniqueCasingType.HEAT_VENT);

    public BlockActiveHeatVent() {
        this(false);
    }

    protected BlockActiveHeatVent(boolean inverted) {
        super(Material.IRON, inverted);
        setTranslationKey("heat_vent_active");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(UniqueCasingType.HEAT_VENT));
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    protected boolean isBloomEnabled(UniqueCasingType value) {
        return true;
    }

    @NotNull @Override
    protected BlockStateContainer createBlockState() {
        this.VARIANT = PropertyEnum.create("variant", UniqueCasingType.class, BLOOM_VARIANTS);
        this.VALUES = VARIANT.getAllowedValues().toArray(new UniqueCasingType[0]);
        return new ExtendedBlockState(this,
                new IProperty[] { VARIANT, ACTIVE_DEPRECATED, POWERED },
                new IUnlistedProperty[] { ACTIVE });
    }

    @Override
    public IBlockState getState(UniqueCasingType variant) {
        return super.getState(BLOOM_VARIANTS.contains(variant) ? variant : UniqueCasingType.HEAT_VENT);
    }
}
