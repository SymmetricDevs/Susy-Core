package supersymmetry.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

import org.jetbrains.annotations.NotNull;

import supersymmetry.api.blocks.VariantHorizontalRotatableBlock;

import gregtech.api.block.IStateHarvestLevel;

public class BlocksS8BMRF extends VariantHorizontalRotatableBlock<BlocksS8BMRF.S8BMRFBlockType> {

    public BlocksS8BMRF() {
        super(Material.ROCK);
        this.setHardness(3.0F);
        this.setResistance(5.0F);
        this.setSoundType(SoundType.STONE);
        this.setTranslationKey("s8bmrf_blocks");
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
    public ItemStack getItemVariant(S8BMRFBlockType variant, int amount) {
        return new ItemStack(this, amount, variant.ordinal());
    }

    @Override
    public int damageDropped(@NotNull IBlockState state) {
        return state.getValue(VARIANT).ordinal();
    }

    public static enum S8BMRFBlockType implements IStringSerializable, IStateHarvestLevel {

        S8BMRF1("s8bmrf1", 2),
        S8BMRF2("s8bmrf2", 2),
        S8BMRF3("s8bmrf3", 2),
        S8BMRF4("s8bmrf4", 2),
        S8BMRF5("s8bmrf5", 2),
        S8BMRF6("s8bmrf6", 2),
        S8BMRF7("s8bmrf7", 2),
        S8BMRF8("s8bmrf8", 2),
        S8BMRF9("s8bmrf9", 2),
        S8BMRF10("s8bmrf10", 2),
        S8BMRF11("s8bmrf11", 2),
        S8BMRF12("s8bmrf12", 2),
        S8BMRF13("s8bmrf13", 2),
        S8BMRF14("s8bmrf14", 2),
        S8BMRF15("s8bmrf15", 2),
        S8BMRF16("s8bmrf16", 2);

        private final String name;
        private final int harvestLevel;

        private S8BMRFBlockType(String name, int harvestLevel) {
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
