package supersymmetry.common.blocks.bmrf;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

import org.jetbrains.annotations.NotNull;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;

public class BlocksS23BMRF extends VariantBlock<BlocksS23BMRF.S23BMRFBlockType> {

    public BlocksS23BMRF() {
        super(Material.ROCK);
        this.setHardness(3.0F);
        this.setResistance(5.0F);
        this.setSoundType(SoundType.STONE);
        this.setTranslationKey("s23bmrf_blocks");
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
    public ItemStack getItemVariant(S23BMRFBlockType variant, int amount) {
        return new ItemStack(this, amount, variant.ordinal());
    }

    @Override
    public int damageDropped(@NotNull IBlockState state) {
        return state.getValue(VARIANT).ordinal();
    }

    public static enum S23BMRFBlockType implements IStringSerializable, IStateHarvestLevel {

        S23BMRF1("s23bmrf1", 2),
        S23BMRF2("s23bmrf2", 2),
        S23BMRF3("s23bmrf3", 2),
        S23BMRF4("s23bmrf4", 2),
        S23BMRF5("s23bmrf5", 2),
        S23BMRF6("s23bmrf6", 2),
        S23BMRF7("s23bmrf7", 2),
        S23BMRF8("s23bmrf8", 2),
        S23BMRF9("s23bmrf9", 2),
        S23BMRF10("s23bmrf10", 2),
        S23BMRF11("s23bmrf11", 2),
        S23BMRF12("s23bmrf12", 2),
        S23BMRF13("s23bmrf13", 2),
        S23BMRF14("s23bmrf14", 2),
        S23BMRF15("s23bmrf15", 2),
        S23BMRF16("s23bmrf16", 2);

        private final String name;
        private final int harvestLevel;

        private S23BMRFBlockType(String name, int harvestLevel) {
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
