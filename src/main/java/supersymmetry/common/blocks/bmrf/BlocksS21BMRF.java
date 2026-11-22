package supersymmetry.common.blocks.bmrf;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

import org.jetbrains.annotations.NotNull;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;

public class BlocksS21BMRF extends VariantBlock<BlocksS21BMRF.S21BMRFBlockType> {

    public BlocksS21BMRF() {
        super(Material.ROCK);
        this.setHardness(3.0F);
        this.setResistance(5.0F);
        this.setSoundType(SoundType.STONE);
        this.setTranslationKey("s21bmrf_blocks");
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
    public ItemStack getItemVariant(S21BMRFBlockType variant, int amount) {
        return new ItemStack(this, amount, variant.ordinal());
    }

    @Override
    public int damageDropped(@NotNull IBlockState state) {
        return state.getValue(VARIANT).ordinal();
    }

    public static enum S21BMRFBlockType implements IStringSerializable, IStateHarvestLevel {

        S21BMRF1("s21bmrf1", 2),
        S21BMRF2("s21bmrf2", 2),
        S21BMRF3("s21bmrf3", 2),
        S21BMRF4("s21bmrf4", 2),
        S21BMRF5("s21bmrf5", 2),
        S21BMRF6("s21bmrf6", 2),
        S21BMRF7("s21bmrf7", 2),
        S21BMRF8("s21bmrf8", 2),
        S21BMRF9("s21bmrf9", 2),
        S21BMRF10("s21bmrf10", 2),
        S21BMRF11("s21bmrf11", 2),
        S21BMRF12("s21bmrf12", 2),
        S21BMRF13("s21bmrf13", 2),
        S21BMRF14("s21bmrf14", 2),
        S21BMRF15("s21bmrf15", 2),
        S21BMRF16("s21bmrf16", 2);

        private final String name;
        private final int harvestLevel;

        private S21BMRFBlockType(String name, int harvestLevel) {
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
