package supersymmetry.common.blocks.bmrf;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

import org.jetbrains.annotations.NotNull;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;

public class BlocksS27BMRF extends VariantBlock<BlocksS27BMRF.S27BMRFBlockType> {

    public BlocksS27BMRF() {
        super(Material.ROCK);
        this.setHardness(3.0F);
        this.setResistance(5.0F);
        this.setSoundType(SoundType.STONE);
        this.setTranslationKey("s27bmrf_blocks");
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
    public ItemStack getItemVariant(S27BMRFBlockType variant, int amount) {
        return new ItemStack(this, amount, variant.ordinal());
    }

    @Override
    public int damageDropped(@NotNull IBlockState state) {
        return state.getValue(VARIANT).ordinal();
    }

    public static enum S27BMRFBlockType implements IStringSerializable, IStateHarvestLevel {

        S27BMRF1("s27bmrf1", 2),
        S27BMRF2("s27bmrf2", 2),
        S27BMRF3("s27bmrf3", 2),
        S27BMRF4("s27bmrf4", 2),
        S27BMRF5("s27bmrf5", 2),
        S27BMRF6("s27bmrf6", 2),
        S27BMRF7("s27bmrf7", 2),
        S27BMRF8("s27bmrf8", 2),
        S27BMRF9("s27bmrf9", 2),
        S27BMRF10("s27bmrf10", 2),
        S27BMRF11("s27bmrf11", 2),
        S27BMRF12("s27bmrf12", 2),
        S27BMRF13("s27bmrf13", 2),
        S27BMRF14("s27bmrf14", 2),
        S27BMRF15("s27bmrf15", 2),
        S27BMRF16("s27bmrf16", 2);

        private final String name;
        private final int harvestLevel;

        private S27BMRFBlockType(String name, int harvestLevel) {
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
