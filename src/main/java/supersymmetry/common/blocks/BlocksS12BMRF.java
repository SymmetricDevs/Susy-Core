package supersymmetry.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

import org.jetbrains.annotations.NotNull;

import supersymmetry.api.blocks.VariantHorizontalRotatableBlock;

import gregtech.api.block.IStateHarvestLevel;

public class BlocksS12BMRF extends VariantHorizontalRotatableBlock<BlocksS12BMRF.S12BMRFBlockType> {

    public BlocksS12BMRF() {
        super(Material.ROCK);
        this.setHardness(3.0F);
        this.setResistance(5.0F);
        this.setSoundType(SoundType.STONE);
        this.setTranslationKey("s12bmrf_blocks");
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
    public ItemStack getItemVariant(S12BMRFBlockType variant, int amount) {
        return new ItemStack(this, amount, variant.ordinal());
    }

    @Override
    public int damageDropped(@NotNull IBlockState state) {
        return state.getValue(VARIANT).ordinal();
    }

    public static enum S12BMRFBlockType implements IStringSerializable, IStateHarvestLevel {

        S12BMRF1("s12bmrf1", 2),
        S12BMRF2("s12bmrf2", 2),
        S12BMRF3("s12bmrf3", 2),
        S12BMRF4("s12bmrf4", 2),
        S12BMRF5("s12bmrf5", 2),
        S12BMRF6("s12bmrf6", 2),
        S12BMRF7("s12bmrf7", 2),
        S12BMRF8("s12bmrf8", 2),
        S12BMRF9("s12bmrf9", 2),
        S12BMRF10("s12bmrf10", 2),
        S12BMRF11("s12bmrf11", 2),
        S12BMRF12("s12bmrf12", 2),
        S12BMRF13("s12bmrf13", 2),
        S12BMRF14("s12bmrf14", 2),
        S12BMRF15("s12bmrf15", 2),
        S12BMRF16("s12bmrf16", 2);

        private final String name;
        private final int harvestLevel;

        private S12BMRFBlockType(String name, int harvestLevel) {
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
