package supersymmetry.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

import org.jetbrains.annotations.NotNull;

import gregtech.api.block.IStateHarvestLevel;
import supersymmetry.api.blocks.VariantHorizontalRotatableBlock;

public class BlocksS9BMRF extends VariantHorizontalRotatableBlock<BlocksS9BMRF.S9BMRFBlockType> {

    public BlocksS9BMRF() {
        super(Material.ROCK);
        this.setHardness(3.0F);
        this.setResistance(5.0F);
        this.setSoundType(SoundType.STONE);
        this.setTranslationKey("s9bmrf_blocks");
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
    public ItemStack getItemVariant(S9BMRFBlockType variant, int amount) {
        return new ItemStack(this, amount, variant.ordinal());
    }

    @Override
    public int damageDropped(@NotNull IBlockState state) {
        return state.getValue(VARIANT).ordinal();
    }

    public static enum S9BMRFBlockType implements IStringSerializable, IStateHarvestLevel {

        S9BMRF1("s9bmrf1", 2),
        S9BMRF2("s9bmrf2", 2),
        S9BMRF3("s9bmrf3", 2),
        S9BMRF4("s9bmrf4", 2),
        S9BMRF5("s9bmrf5", 2),
        S9BMRF6("s9bmrf6", 2),
        S9BMRF7("s9bmrf7", 2),
        S9BMRF8("s9bmrf8", 2),
        S9BMRF9("s9bmrf9", 2),
        S9BMRF10("s9bmrf10", 2),
        S9BMRF11("s9bmrf11", 2),
        S9BMRF12("s9bmrf12", 2),
        S9BMRF13("s9bmrf13", 2),
        S9BMRF14("s9bmrf14", 2),
        S9BMRF15("s9bmrf15", 2),
        S9BMRF16("s9bmrf16", 2);

        private final String name;
        private final int harvestLevel;

        private S9BMRFBlockType(String name, int harvestLevel) {
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
