package supersymmetry.common.blocks.bmrf;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

import org.jetbrains.annotations.NotNull;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;

public class BlocksS19BMRF extends VariantBlock<BlocksS19BMRF.S19BMRFBlockType> {

    public BlocksS19BMRF() {
        super(Material.ROCK);
        this.setHardness(3.0F);
        this.setResistance(5.0F);
        this.setSoundType(SoundType.STONE);
        this.setTranslationKey("s19bmrf_blocks");
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
    public ItemStack getItemVariant(S19BMRFBlockType variant, int amount) {
        return new ItemStack(this, amount, variant.ordinal());
    }

    @Override
    public int damageDropped(@NotNull IBlockState state) {
        return state.getValue(VARIANT).ordinal();
    }

    public static enum S19BMRFBlockType implements IStringSerializable, IStateHarvestLevel {

        S19BMRF1("s19bmrf1", 2),
        S19BMRF2("s19bmrf2", 2),
        S19BMRF3("s19bmrf3", 2),
        S19BMRF4("s19bmrf4", 2),
        S19BMRF5("s19bmrf5", 2),
        S19BMRF6("s19bmrf6", 2),
        S19BMRF7("s19bmrf7", 2),
        S19BMRF8("s19bmrf8", 2),
        S19BMRF9("s19bmrf9", 2),
        S19BMRF10("s19bmrf10", 2),
        S19BMRF11("s19bmrf11", 2),
        S19BMRF12("s19bmrf12", 2),
        S19BMRF13("s19bmrf13", 2),
        S19BMRF14("s19bmrf14", 2),
        S19BMRF15("s19bmrf15", 2),
        S19BMRF16("s19bmrf16", 2);

        private final String name;
        private final int harvestLevel;

        private S19BMRFBlockType(String name, int harvestLevel) {
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
