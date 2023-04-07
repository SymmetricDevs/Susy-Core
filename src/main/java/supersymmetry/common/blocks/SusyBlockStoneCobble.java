package supersymmetry.common.blocks;

import gregtech.api.block.VariantBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;

public class SusyBlockStoneCobble extends VariantBlock<SusyBlockStoneCobble.BlockType> {
    public SusyBlockStoneCobble() {
        super(Material.IRON);
        this.setTranslationKey("stone_cobble");
        this.setHardness(5.0F);
        this.setResistance(10.0F);
        this.setSoundType(SoundType.METAL);
        this.setHarvestLevel("pickaxe", 1);
        this.setDefaultState(this.getState(SusyBlockStoneCobble.BlockType.GABBRO));
    }
    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EntityLiving.SpawnPlacementType type) {
        return true;
    }

    public BlockType getVariant(IBlockState blockState) {
        return (BlockType) blockState.getValue(this.VARIANT);
    }
    public enum BlockType implements IStringSerializable {
        GABBRO("gabbro"),
        GNEISS("gneiss"),
        GRAPHITE("graphite"),
        LIMESTONE("limestone"),
        MICA("mica"),
        PHYLLITE("phyllite"),
        QUARTZITE("quartzite"),
        SHALE("shale"),
        SLATE("slate"),
        SOAPSTONE("soapstone"),
        KIMBERLITE("kimberlite");

        private final String name;

        BlockType(String name) {
            this.name = name;
        }

        @Nonnull
        public String getName() {
            return this.name;
        }
    }
}
