package supersymmetry.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

import org.jetbrains.annotations.NotNull;

import supersymmetry.api.blocks.VariantHorizontalRotatableBlock;

import gregtech.api.block.IStateHarvestLevel;

public class BlocksS13BMRF extends VariantHorizontalRotatableBlock<BlocksS13BMRF.S13BMRFBlockType> {

    public BlocksS13BMRF() {
        super(Material.ROCK);
        this.setHardness(3.0F);
        this.setResistance(5.0F);
        this.setSoundType(SoundType.STONE);
        this.setTranslationKey("s13bmrf_blocks");
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
    public ItemStack getItemVariant(S13BMRFBlockType variant, int amount) {
        return new ItemStack(this, amount, variant.ordinal());
    }

    @Override
    public int damageDropped(@NotNull IBlockState state) {
        return state.getValue(VARIANT).ordinal();
    }

    public static enum S13BMRFBlockType implements IStringSerializable, IStateHarvestLevel {

        S13BMRF1("s13bmrf1", 2),
        S13BMRF2("s13bmrf2", 2),
        S13BMRF3("s13bmrf3", 2),
        S13BMRF4("s13bmrf4", 2),
        S13BMRF5("s13bmrf5", 2),
        S13BMRF6("s13bmrf6", 2),
        S13BMRF7("s13bmrf7", 2),
        S13BMRF8("s13bmrf8", 2),
        S13BMRF9("s13bmrf9", 2),
        S13BMRF10("s13bmrf10", 2),
        S13BMRF11("s13bmrf11", 2),
        S13BMRF12("s13bmrf12", 2),
        S13BMRF13("s13bmrf13", 2),
        S13BMRF14("s13bmrf14", 2),
        S13BMRF15("s13bmrf15", 2),
        S13BMRF16("s13bmrf16", 2);

        private final String name;
        private final int harvestLevel;

        private S13BMRFBlockType(String name, int harvestLevel) {
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