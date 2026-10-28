package supersymmetry.common.blocks.bmrf;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

import org.jetbrains.annotations.NotNull;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;

public class BlocksS22BMRF extends VariantBlock<BlocksS22BMRF.S22BMRFBlockType> {

    public BlocksS22BMRF() {
        super(Material.ROCK);
        this.setHardness(3.0F);
        this.setResistance(5.0F);
        this.setSoundType(SoundType.STONE);
        this.setTranslationKey("s22bmrf_blocks");
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
    public ItemStack getItemVariant(S22BMRFBlockType variant, int amount) {
        return new ItemStack(this, amount, variant.ordinal());
    }

    @Override
    public int damageDropped(@NotNull IBlockState state) {
        return state.getValue(VARIANT).ordinal();
    }

    public static enum S22BMRFBlockType implements IStringSerializable, IStateHarvestLevel {

        S22BMRF1("s22bmrf1", 2),
        S22BMRF2("s22bmrf2", 2),
        S22BMRF3("s22bmrf3", 2),
        S22BMRF4("s22bmrf4", 2),
        S22BMRF5("s22bmrf5", 2),
        S22BMRF6("s22bmrf6", 2),
        S22BMRF7("s22bmrf7", 2),
        S22BMRF8("s22bmrf8", 2),
        S22BMRF9("s22bmrf9", 2),
        S22BMRF10("s22bmrf10", 2),
        S22BMRF11("s22bmrf11", 2),
        S22BMRF12("s22bmrf12", 2),
        S22BMRF13("s22bmrf13", 2),
        S22BMRF14("s22bmrf14", 2),
        S22BMRF15("s22bmrf15", 2),
        S22BMRF16("s22bmrf16", 2);

        private final String name;
        private final int harvestLevel;

        private S22BMRFBlockType(String name, int harvestLevel) {
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
