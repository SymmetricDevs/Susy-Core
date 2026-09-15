package supersymmetry.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

import org.jetbrains.annotations.NotNull;

import gregtech.api.block.IStateHarvestLevel;
import supersymmetry.api.blocks.VariantHorizontalRotatableBlock;

public class BlocksS11BMRF extends VariantHorizontalRotatableBlock<BlocksS11BMRF.S11BMRFBlockType> {

    public BlocksS11BMRF() {
        super(Material.ROCK);
        this.setHardness(3.0F);
        this.setResistance(5.0F);
        this.setSoundType(SoundType.STONE);
        this.setTranslationKey("s11bmrf_blocks");
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
    public ItemStack getItemVariant(S11BMRFBlockType variant, int amount) {
        return new ItemStack(this, amount, variant.ordinal());
    }

    @Override
    public int damageDropped(@NotNull IBlockState state) {
        return state.getValue(VARIANT).ordinal();
    }

    public static enum S11BMRFBlockType implements IStringSerializable, IStateHarvestLevel {

        S11BMRF1("s11bmrf1", 2),
        S11BMRF2("s11bmrf2", 2),
        S11BMRF3("s11bmrf3", 2),
        S11BMRF4("s11bmrf4", 2),
        S11BMRF5("s11bmrf5", 2),
        S11BMRF6("s11bmrf6", 2),
        S11BMRF7("s11bmrf7", 2),
        S11BMRF8("s11bmrf8", 2),
        S11BMRF9("s11bmrf9", 2),
        S11BMRF10("s11bmrf10", 2),
        S11BMRF11("s11bmrf11", 2),
        S11BMRF12("s11bmrf12", 2),
        S11BMRF13("s11bmrf13", 2),
        S11BMRF14("s11bmrf14", 2),
        S11BMRF15("s11bmrf15", 2),
        S11BMRF16("s11bmrf16", 2);

        private final String name;
        private final int harvestLevel;

        private S11BMRFBlockType(String name, int harvestLevel) {
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
