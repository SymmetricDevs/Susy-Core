package supersymmetry.common.blocks.active;

import java.util.Arrays;
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

public class BlockActiveHeatingCoil extends RedstoneActiveBlock<BlockActiveHeatingCoil.CoilType> {

    public enum CoilType implements IStringSerializable {

        MOLYBDENUM_DISILICIDE("molybdenum_disilicide"),
        TUNGSTEN("tungsten");

        private final String name;

        CoilType(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    public static final List<CoilType> ALL_VARIANTS = Arrays.asList(CoilType.values());

    public BlockActiveHeatingCoil() {
        this(false);
    }

    protected BlockActiveHeatingCoil(boolean inverted) {
        super(Material.IRON, inverted);
        setTranslationKey("heating_coil_active");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(CoilType.MOLYBDENUM_DISILICIDE));
    }

    @Override
    protected boolean isBloomEnabled(CoilType value) {
        return true;
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @NotNull @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.SOLID;
    }

    @NotNull @Override
    protected BlockStateContainer createBlockState() {
        this.VARIANT = PropertyEnum.create("variant", CoilType.class);
        this.VALUES = VARIANT.getAllowedValues().toArray(new CoilType[0]);
        return new ExtendedBlockState(this,
                new IProperty[] { VARIANT, ACTIVE_DEPRECATED, POWERED },
                new IUnlistedProperty[] { ACTIVE });
    }
}
