package supersymmetry.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

import org.jetbrains.annotations.NotNull;

import supersymmetry.api.blocks.VariantHorizontalRotatableBlock;

import gregtech.api.block.IStateHarvestLevel;

public class BlocksS16BMRF extends VariantHorizontalRotatableBlock<BlocksS16BMRF.S16BMRFBlockType> {

    public BlocksS16BMRF() {
        super(Material.ROCK);
        this.setHardness(3.0F);
        this.setResistance(5.0F);
        this.setSoundType(SoundType.STONE);
        this.setTranslationKey("s16bmrf_blocks");
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
    public ItemStack getItemVariant(S16BMRFBlockType variant, int amount) {
        return new ItemStack(this, amount, variant.ordinal());
    }

    @Override
    public int damageDropped(@NotNull IBlockState state) {
        return state.getValue(VARIANT).ordinal();
    }

    public static enum S16BMRFBlockType implements IStringSerializable, IStateHarvestLevel {

        S16BMRF1("s16bmrf1", 2),
        S16BMRF2("s16bmrf2", 2),
        S16BMRF3("s16bmrf3", 2),
        S16BMRF4("s16bmrf4", 2),
        S16BMRF5("s16bmrf5", 2),
        S16BMRF6("s16bmrf6", 2),
        S16BMRF7("s16bmrf7", 2),
        S16BMRF8("s16bmrf8", 2),
        S16BMRF9("s16bmrf9", 2),
        S16BMRF10("s16bmrf10", 2),
        S16BMRF11("s16bmrf11", 2),
        S16BMRF12("s16bmrf12", 2),
        S16BMRF13("s16bmrf13", 2),
        S16BMRF14("s16bmrf14", 2),
        S16BMRF15("s16bmrf15", 2),
        S16BMRF16("s16bmrf16", 2);

        private final String name;
        private final int harvestLevel;

        private S16BMRFBlockType(String name, int harvestLevel) {
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
