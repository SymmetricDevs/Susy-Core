package supersymmetry.common.blocks;

import gregtech.api.block.IStateHarvestLevel;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.blocks.VariantHorizontalRotatableBlock;

import javax.annotation.Nonnull;

public class BlockSeparatorRotor extends VariantHorizontalRotatableBlock<BlockSeparatorRotor.BlockSeparatorRotorType> {

    public BlockSeparatorRotor() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("separator_rotor");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    public enum BlockSeparatorRotorType implements IStringSerializable, IStateHarvestLevel {
        STEEL("steel", 2);

        private final String name;
        private final int harvestLevel;

        BlockSeparatorRotorType(String name, int harvestLevel) {
            this.name = name;
            this.harvestLevel = harvestLevel;
        }

        @Nonnull
        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public int getHarvestLevel(IBlockState state) {
            return this.harvestLevel;
        }

        @Override
        public String getHarvestTool(IBlockState state) {
            return "wrench";
        }
    }
}
