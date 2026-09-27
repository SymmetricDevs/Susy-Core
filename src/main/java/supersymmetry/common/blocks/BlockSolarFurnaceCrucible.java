package supersymmetry.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import org.jetbrains.annotations.NotNull;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;

public class BlockSolarFurnaceCrucible extends VariantBlock<BlockSolarFurnaceCrucible.SolarFurnaceCrucibleType> {

    public BlockSolarFurnaceCrucible() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("solar_furnace_crucible");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(SolarFurnaceCrucibleType.DEFAULT));
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(@NotNull IBlockState state) {
        return false;
    }

    @NotNull @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    public enum SolarFurnaceCrucibleType implements IStringSerializable, IStateHarvestLevel {

        DEFAULT("default", 3);

        private final String name;
        private final int harvestLevel;

        SolarFurnaceCrucibleType(String name, int harvestLevel) {
            this.name = name;
            this.harvestLevel = harvestLevel;
        }

        @NotNull public String getName() {
            return this.name;
        }

        public int getHarvestLevel(IBlockState state) {
            return this.harvestLevel;
        }

        public String getHarvestTool(IBlockState state) {
            return "wrench";
        }
    }
}
