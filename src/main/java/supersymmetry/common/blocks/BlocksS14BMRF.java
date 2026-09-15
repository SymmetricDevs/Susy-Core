package supersymmetry.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

import org.jetbrains.annotations.NotNull;

import gregtech.api.block.IStateHarvestLevel;
import supersymmetry.api.blocks.VariantHorizontalRotatableBlock;

public class BlocksS14BMRF extends VariantHorizontalRotatableBlock<BlocksS14BMRF.S14BMRFBlockType> {

    public BlocksS14BMRF() {
        super(Material.ROCK);
        this.setHardness(3.0F);
        this.setResistance(5.0F);
        this.setSoundType(SoundType.STONE);
        this.setTranslationKey("s14bmrf_blocks");
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
    public ItemStack getItemVariant(S14BMRFBlockType variant, int amount) {
        return new ItemStack(this, amount, variant.ordinal());
    }

    @Override
    public int damageDropped(@NotNull IBlockState state) {
        return state.getValue(VARIANT).ordinal();
    }

    public static enum S14BMRFBlockType implements IStringSerializable, IStateHarvestLevel {

        S14BMRF1("s14bmrf1", 2),
        S14BMRF2("s14bmrf2", 2),
        S14BMRF3("s14bmrf3", 2),
        S14BMRF4("s14bmrf4", 2),
        S14BMRF5("s14bmrf5", 2),
        S14BMRF6("s14bmrf6", 2),
        S14BMRF7("s14bmrf7", 2),
        S14BMRF8("s14bmrf8", 2),
        S14BMRF9("s14bmrf9", 2),
        S14BMRF10("s14bmrf10", 2),
        S14BMRF11("s14bmrf11", 2),
        S14BMRF12("s14bmrf12", 2),
        S14BMRF13("s14bmrf13", 2),
        S14BMRF14("s14bmrf14", 2),
        S14BMRF15("s14bmrf15", 2),
        S14BMRF16("s14bmrf16", 2);

        private final String name;
        private final int harvestLevel;

        private S14BMRFBlockType(String name, int harvestLevel) {
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
