package supersymmetry.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

import org.jetbrains.annotations.NotNull;

import supersymmetry.api.blocks.VariantHorizontalRotatableBlock;

import gregtech.api.block.IStateHarvestLevel;

public class BlocksS10BMRF extends VariantHorizontalRotatableBlock<BlocksS10BMRF.S10BMRFBlockType> {

    public BlocksS10BMRF() {
        super(Material.ROCK);
        this.setHardness(3.0F);
        this.setResistance(5.0F);
        this.setSoundType(SoundType.STONE);
        this.setTranslationKey("s10bmrf_blocks");
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
    public ItemStack getItemVariant(S10BMRFBlockType variant, int amount) {
        return new ItemStack(this, amount, variant.ordinal());
    }

    @Override
    public int damageDropped(@NotNull IBlockState state) {
        return state.getValue(VARIANT).ordinal();
    }

    public static enum S10BMRFBlockType implements IStringSerializable, IStateHarvestLevel {

        S10BMRF1("s10bmrf1", 2),
        S10BMRF2("s10bmrf2", 2),
        S10BMRF3("s10bmrf3", 2),
        S10BMRF4("s10bmrf4", 2),
        S10BMRF5("s10bmrf5", 2),
        S10BMRF6("s10bmrf6", 2),
        S10BMRF7("s10bmrf7", 2),
        S10BMRF8("s10bmrf8", 2),
        S10BMRF9("s10bmrf9", 2),
        S10BMRF10("s10bmrf10", 2),
        S10BMRF11("s10bmrf11", 2),
        S10BMRF12("s10bmrf12", 2),
        S10BMRF13("s10bmrf13", 2),
        S10BMRF14("s10bmrf14", 2),
        S10BMRF15("s10bmrf15", 2),
        S10BMRF16("s10bmrf16", 2);

        private final String name;
        private final int harvestLevel;

        private S10BMRFBlockType(String name, int harvestLevel) {
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
