package supersymmetry.common.blocks.bmrf;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

import org.jetbrains.annotations.NotNull;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;

public class BlocksS24BMRF extends VariantBlock<BlocksS24BMRF.S24BMRFBlockType> {

    public BlocksS24BMRF() {
        super(Material.ROCK);
        this.setHardness(3.0F);
        this.setResistance(5.0F);
        this.setSoundType(SoundType.STONE);
        this.setTranslationKey("s24bmrf_blocks");
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
    public ItemStack getItemVariant(S24BMRFBlockType variant, int amount) {
        return new ItemStack(this, amount, variant.ordinal());
    }

    @Override
    public int damageDropped(@NotNull IBlockState state) {
        return state.getValue(VARIANT).ordinal();
    }

    public static enum S24BMRFBlockType implements IStringSerializable, IStateHarvestLevel {

        S24BMRF1("s24bmrf1", 2),
        S24BMRF2("s24bmrf2", 2),
        S24BMRF3("s24bmrf3", 2),
        S24BMRF4("s24bmrf4", 2),
        S24BMRF5("s24bmrf5", 2),
        S24BMRF6("s24bmrf6", 2),
        S24BMRF7("s24bmrf7", 2),
        S24BMRF8("s24bmrf8", 2),
        S24BMRF9("s24bmrf9", 2),
        S24BMRF10("s24bmrf10", 2);

        private final String name;
        private final int harvestLevel;

        private S24BMRFBlockType(String name, int harvestLevel) {
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
