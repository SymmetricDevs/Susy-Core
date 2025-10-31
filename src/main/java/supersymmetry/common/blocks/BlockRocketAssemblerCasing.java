package supersymmetry.common.blocks;

import javax.annotation.Nonnull;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import org.jetbrains.annotations.NotNull;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;

public class BlockRocketAssemblerCasing extends VariantBlock<BlockRocketAssemblerCasing.RocketAssemblerCasingType> {

    public BlockRocketAssemblerCasing() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("rocket_assembler_casing");
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

    public static enum RocketAssemblerCasingType implements IStringSerializable, IStateHarvestLevel {

        REINFORCED_FOUNDATION("reinforced_foundation", 1),
        FOUNDATION("foundation", 1),
        RAILS("rails", 1),
        STRUCTURAL_FRAME("structural_frame", 1);

        private final String name;
        private final int harvestLevel;

        private RocketAssemblerCasingType(String name, int harvestLevel) {
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
            return "wrench";
        }
    }
}
