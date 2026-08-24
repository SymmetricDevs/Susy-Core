package supersymmetry.common.blocks;

import java.util.Collections;
import java.util.List;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import org.jetbrains.annotations.NotNull;

public class BlockActiveMolybdenumDisilicideCoil extends RedstoneActiveBlock<BlockActiveMolybdenumDisilicideCoil.CoilType> {

    public enum CoilType implements IStringSerializable {
        MOLYBDENUM_DISILICIDE_COIL("molybdenum_disilicide_coil");

        private final String name;

        CoilType(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    private static final List<CoilType> BLOOM_VARIANTS = Collections.singletonList(
            CoilType.MOLYBDENUM_DISILICIDE_COIL);

    public BlockActiveMolybdenumDisilicideCoil() {
        this(false);
    }

    protected BlockActiveMolybdenumDisilicideCoil(boolean inverted) {
        super(Material.IRON, inverted);
        setTranslationKey("molybdenum_disilicide_coil_active");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(CoilType.MOLYBDENUM_DISILICIDE_COIL));
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.SOLID;
    }

    @Override
    protected boolean isBloomEnabled(CoilType value) {
        return true;
    }

    @NotNull @Override
    protected BlockStateContainer createBlockState() {
        this.VARIANT = PropertyEnum.create("variant", CoilType.class, BLOOM_VARIANTS);
        this.VALUES = VARIANT.getAllowedValues().toArray(new CoilType[0]);
        return new ExtendedBlockState(this,
                new IProperty[] { VARIANT, ACTIVE_DEPRECATED, POWERED },
                new IUnlistedProperty[] { ACTIVE });
    }

    @Override
    public IBlockState getState(CoilType variant) {
        return super.getState(BLOOM_VARIANTS.contains(variant) ? variant : CoilType.MOLYBDENUM_DISILICIDE_COIL);
    }
}
