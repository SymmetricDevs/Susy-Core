package supersymmetry.common.blocks.bmrf;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

import org.jetbrains.annotations.NotNull;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;

public class BlocksS25BMRF extends VariantBlock<BlocksS25BMRF.S25BMRFBlockType> {

    public BlocksS25BMRF() {
        super(Material.ROCK);
        this.setHardness(3.0F);
        this.setResistance(5.0F);
        this.setSoundType(SoundType.STONE);
        this.setTranslationKey("s25bmrf_blocks");
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
    public ItemStack getItemVariant(S25BMRFBlockType variant, int amount) {
        return new ItemStack(this, amount, variant.ordinal());
    }

    @Override
    public int damageDropped(@NotNull IBlockState state) {
        return state.getValue(VARIANT).ordinal();
    }

    public static enum S25BMRFBlockType implements IStringSerializable, IStateHarvestLevel {

        S25BMRF1("s25bmrf1", 2),
        S25BMRF2("s25bmrf2", 2),
        S25BMRF3("s25bmrf3", 2),
        S25BMRF4("s25bmrf4", 2),
        S25BMRF5("s25bmrf5", 2),
        S25BMRF6("s25bmrf6", 2),
        S25BMRF7("s25bmrf7", 2),
        S25BMRF8("s25bmrf8", 2),
        S25BMRF9("s25bmrf9", 2),
        S25BMRF10("s25bmrf10", 2),
        S25BMRF11("s25bmrf11", 2),
        S25BMRF12("s25bmrf12", 2),
        S25BMRF13("s25bmrf13", 2),
        S25BMRF14("s25bmrf14", 2),
        S25BMRF15("s25bmrf15", 2),
        S25BMRF16("s25bmrf16", 2);

        private final String name;
        private final int harvestLevel;

        private S25BMRFBlockType(String name, int harvestLevel) {
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
