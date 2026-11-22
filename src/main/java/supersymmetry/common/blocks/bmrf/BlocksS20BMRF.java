package supersymmetry.common.blocks.bmrf;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

import org.jetbrains.annotations.NotNull;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;

public class BlocksS20BMRF extends VariantBlock<BlocksS20BMRF.S20BMRFBlockType> {

    public BlocksS20BMRF() {
        super(Material.ROCK);
        this.setHardness(3.0F);
        this.setResistance(5.0F);
        this.setSoundType(SoundType.STONE);
        this.setTranslationKey("s20bmrf_blocks");
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
    public ItemStack getItemVariant(S20BMRFBlockType variant, int amount) {
        return new ItemStack(this, amount, variant.ordinal());
    }

    @Override
    public int damageDropped(@NotNull IBlockState state) {
        return state.getValue(VARIANT).ordinal();
    }

    public static enum S20BMRFBlockType implements IStringSerializable, IStateHarvestLevel {

        S20BMRF1("s20bmrf1", 2),
        S20BMRF2("s20bmrf2", 2),
        S20BMRF3("s20bmrf3", 2),
        S20BMRF4("s20bmrf4", 2),
        S20BMRF5("s20bmrf5", 2),
        S20BMRF6("s20bmrf6", 2),
        S20BMRF7("s20bmrf7", 2),
        S20BMRF8("s20bmrf8", 2),
        S20BMRF9("s20bmrf9", 2),
        S20BMRF10("s20bmrf10", 2),
        S20BMRF11("s20bmrf11", 2),
        S20BMRF12("s20bmrf12", 2),
        S20BMRF13("s20bmrf13", 2),
        S20BMRF14("s20bmrf14", 2),
        S20BMRF15("s20bmrf15", 2),
        S20BMRF16("s20bmrf16", 2);

        private final String name;
        private final int harvestLevel;

        private S20BMRFBlockType(String name, int harvestLevel) {
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
