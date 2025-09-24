package supersymmetry.common.blocks;

import javax.annotation.Nonnull;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import org.jetbrains.annotations.NotNull;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantActiveBlock;

/// This should actually be a [VariantBlock]. But changing it would void evaporation beds in existing saves.
/// Also, it will need me to do extra code for exposure block counting, so I'll just leave it here untouched.
public class BlockEvaporationBed extends VariantActiveBlock<BlockEvaporationBed.EvaporationBedType> {

    public BlockEvaporationBed() {
        super(net.minecraft.block.material.Material.CLAY);
        setTranslationKey("evaporation_bed");
        setHardness(0.5f);
        setResistance(0.5f);
        setSoundType(SoundType.GROUND);
        setHarvestLevel("shovel", 0);
        setDefaultState(getState(EvaporationBedType.DIRT));
    }

    @NotNull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.SOLID;
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    public enum EvaporationBedType implements IStringSerializable, IStateHarvestLevel {

        DIRT("dirt", 0);

        private final String name;
        private final int harvestLevel;

        EvaporationBedType(String name, int harvestLevel) {
            this.name = name;
            this.harvestLevel = harvestLevel;
        }

        @Nonnull
        public String getName() {
            return this.name;
        }

        public int getHarvestLevel(IBlockState state) {
            return this.harvestLevel;
        }

        public String getHarvestTool(IBlockState state) {
            return "shovel";
        }
    }
}
