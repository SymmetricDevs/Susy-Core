package supersymmetry.common.blocks.bmrf;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

import org.jetbrains.annotations.NotNull;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;

public class BlocksS17BMRF extends VariantBlock<BlocksS17BMRF.S17BMRFBlockType> {

    public BlocksS17BMRF() {
        super(Material.ROCK);
        this.setHardness(3.0F);
        this.setResistance(5.0F);
        this.setSoundType(SoundType.STONE);
        this.setTranslationKey("s17bmrf_blocks");
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
    public ItemStack getItemVariant(S17BMRFBlockType variant, int amount) {
        return new ItemStack(this, amount, variant.ordinal());
    }

    @Override
    public int damageDropped(@NotNull IBlockState state) {
        return state.getValue(VARIANT).ordinal();
    }

    public static enum S17BMRFBlockType implements IStringSerializable, IStateHarvestLevel {

        S17BMRF1("s17bmrf1", 2),
        S17BMRF2("s17bmrf2", 2),
        S17BMRF3("s17bmrf3", 2),
        S17BMRF4("s17bmrf4", 2),
        S17BMRF5("s17bmrf5", 2),
        S17BMRF6("s17bmrf6", 2),
        S17BMRF7("s17bmrf7", 2),
        S17BMRF8("s17bmrf8", 2),
        S17BMRF9("s17bmrf9", 2),
        S17BMRF10("s17bmrf10", 2),
        S17BMRF11("s17bmrf11", 2),
        S17BMRF12("s17bmrf12", 2),
        S17BMRF13("s17bmrf13", 2),
        S17BMRF14("s17bmrf14", 2),
        S17BMRF15("s17bmrf15", 2),
        S17BMRF16("s17bmrf16", 2);

        private final String name;
        private final int harvestLevel;

        private S17BMRFBlockType(String name, int harvestLevel) {
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
