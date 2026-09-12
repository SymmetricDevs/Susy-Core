package supersymmetry.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

import org.jetbrains.annotations.NotNull;

import gregtech.api.block.IStateHarvestLevel;
import supersymmetry.api.blocks.VariantHorizontalRotatableBlock;

public class BlocksS7BMRF extends VariantHorizontalRotatableBlock<BlocksS7BMRF.S7BMRFBlockType> {

    public BlocksS7BMRF() {
        super(Material.ROCK);
        this.setHardness(3.0F);
        this.setResistance(5.0F);
        this.setSoundType(SoundType.STONE);
        this.setTranslationKey("s7bmrf_blocks");
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
    public ItemStack getItemVariant(S7BMRFBlockType variant, int amount) {
        return new ItemStack(this, amount, variant.ordinal());
    }

    @Override
    public int damageDropped(@NotNull IBlockState state) {
        return state.getValue(VARIANT).ordinal();
    }

    public static enum S7BMRFBlockType implements IStringSerializable, IStateHarvestLevel {

        S7BMRF1("s7bmrf1", 2),
        S7BMRF2("s7bmrf2", 2),
        S7BMRF3("s7bmrf3", 2),
        S7BMRF4("s7bmrf4", 2),
        S7BMRF5("s7bmrf5", 2),
        S7BMRF6("s7bmrf6", 2),
        S7BMRF7("s7bmrf7", 2),
        S7BMRF8("s7bmrf8", 2),
        S7BMRF9("s7bmrf9", 2),
        S7BMRF10("s7bmrf10", 2),
        S7BMRF11("s7bmrf11", 2),
        S7BMRF12("s7bmrf12", 2),
        S7BMRF13("s7bmrf13", 2),
        S7BMRF14("s7bmrf14", 2),
        S7BMRF15("s7bmrf15", 2),
        S7BMRF16("s7bmrf16", 2);

        private final String name;
        private final int harvestLevel;

        private S7BMRFBlockType(String name, int harvestLevel) {
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
