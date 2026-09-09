package supersymmetry.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

import org.jetbrains.annotations.NotNull;

import gregtech.api.block.IStateHarvestLevel;
import supersymmetry.api.blocks.VariantHorizontalRotatableBlock;

public class BlocksS3BMRF extends VariantHorizontalRotatableBlock<BlocksS3BMRF.S3BMRFBlockType> {

    public BlocksS3BMRF() {
        super(Material.ROCK);
        this.setHardness(3.0F);
        this.setResistance(5.0F);
        this.setSoundType(SoundType.STONE);
        this.setTranslationKey("s3bmrf_blocks");
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(VARIANT, VALUES[meta % VALUES.length]);
    }

    @Override
    public int getMetaFromState(@NotNull IBlockState state) {
        return state.getValue(VARIANT).ordinal();
    }

    @Override
    public ItemStack getItemVariant(S3BMRFBlockType variant, int amount) {
        return new ItemStack(this, amount, variant.ordinal());
    }

    @Override
    public int damageDropped(@NotNull IBlockState state) {
        return state.getValue(VARIANT).ordinal();
    }

    public static enum S3BMRFBlockType implements IStringSerializable, IStateHarvestLevel {

        S3BMRF1("s3bmrf1", 2),
        S3BMRF2("s3bmrf2", 2),
        S3BMRF3("s3bmrf3", 2),
        S3BMRF4("s3bmrf4", 2),
        S3BMRF5("s3bmrf5", 2),
        S3BMRF6("s3bmrf6", 2),
        S3BMRF7("s3bmrf7", 2),
        S3BMRF8("s3bmrf8", 2),
        S3BMRF9("s3bmrf9", 2),
        S3BMRF10("s3bmrf10", 2),
        S3BMRF11("s3bmrf11", 2),
        S3BMRF12("s3bmrf12", 2),
        S3BMRF13("s3bmrf13", 2),
        S3BMRF14("s3bmrf14", 2),
        S3BMRF15("s3bmrf15", 2),
        S3BMRF16("s3bmrf16", 2);

        private final String name;
        private final int harvestLevel;

        private S3BMRFBlockType(String name, int harvestLevel) {
            this.name = name;
            this.harvestLevel = harvestLevel;
        }

        @Override
        public int getHarvestLevel(IBlockState iBlockState) {
            return this.harvestLevel;
        }

        @Override
        public String getName() {
            return this.name;
        }
    }
}
