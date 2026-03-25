package supersymmetry.common.blocks;

import javax.annotation.Nonnull;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import gregtech.api.block.IStateHarvestLevel;
import supersymmetry.api.blocks.VariantBlockFalling;

public class BlockRegolith extends VariantBlockFalling<BlockRegolith.BlockRegolithType> {

    public BlockRegolith() {
        super(Material.SAND);
        setTranslationKey("regolith");
        setHardness(1.0f);
        setResistance(2.0f);
        setSoundType(SoundType.SAND);
    }

    public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return MapColor.STONE;
    }

    @SideOnly(Side.CLIENT)
    public int getDustColor(IBlockState state) {
        return -8356741;
    }

    public enum BlockRegolithType implements IStringSerializable, IStateHarvestLevel {

        HIGHLAND("highland", 1),
        LOWLAND("lowland", 1);

        private final String name;
        private final int harvestLevel;

        BlockRegolithType(String name, int harvestLevel) {
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
            return "shovel";
        }
    }
}
