package supersymmetry.common.blocks.bmrf;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

import org.jetbrains.annotations.NotNull;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;

public class BlocksS26BMRF extends VariantBlock<BlocksS26BMRF.S26BMRFBlockType> {

    public BlocksS26BMRF() {
        super(Material.ROCK);
        this.setHardness(3.0F);
        this.setResistance(5.0F);
        this.setSoundType(SoundType.STONE);
        this.setTranslationKey("s26bmrf_blocks");
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
    public ItemStack getItemVariant(S26BMRFBlockType variant, int amount) {
        return new ItemStack(this, amount, variant.ordinal());
    }

    @Override
    public int damageDropped(@NotNull IBlockState state) {
        return state.getValue(VARIANT).ordinal();
    }

    public static enum S26BMRFBlockType implements IStringSerializable, IStateHarvestLevel {

        S26BMRF1("s26bmrf1", 2),
        S26BMRF2("s26bmrf2", 2),
        S26BMRF3("s26bmrf3", 2),
        S26BMRF4("s26bmrf4", 2),
        S26BMRF5("s26bmrf5", 2),
        S26BMRF6("s26bmrf6", 2),
        S26BMRF7("s26bmrf7", 2),
        S26BMRF8("s26bmrf8", 2),
        S26BMRF9("s26bmrf9", 2),
        S26BMRF10("s26bmrf10", 2),
        S26BMRF11("s26bmrf11", 2),
        S26BMRF12("s26bmrf12", 2),
        S26BMRF13("s26bmrf13", 2),
        S26BMRF14("s26bmrf14", 2),
        S26BMRF15("s26bmrf15", 2),
        S26BMRF16("s26bmrf16", 2);

        private final String name;
        private final int harvestLevel;

        private S26BMRFBlockType(String name, int harvestLevel) {
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
