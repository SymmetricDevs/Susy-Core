package supersymmetry.common.blocks.bmrf;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

import org.jetbrains.annotations.NotNull;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;

public class BlocksS18BMRF extends VariantBlock<BlocksS18BMRF.S18BMRFBlockType> {

    public BlocksS18BMRF() {
        super(Material.ROCK);
        this.setHardness(3.0F);
        this.setResistance(5.0F);
        this.setSoundType(SoundType.STONE);
        this.setTranslationKey("s18bmrf_blocks");
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
    public ItemStack getItemVariant(S18BMRFBlockType variant, int amount) {
        return new ItemStack(this, amount, variant.ordinal());
    }

    @Override
    public int damageDropped(@NotNull IBlockState state) {
        return state.getValue(VARIANT).ordinal();
    }

    public static enum S18BMRFBlockType implements IStringSerializable, IStateHarvestLevel {

        S18BMRF1("s18bmrf1", 2),
        S18BMRF2("s18bmrf2", 2),
        S18BMRF3("s18bmrf3", 2),
        S18BMRF4("s18bmrf4", 2),
        S18BMRF5("s18bmrf5", 2),
        S18BMRF6("s18bmrf6", 2),
        S18BMRF7("s18bmrf7", 2),
        S18BMRF8("s18bmrf8", 2),
        S18BMRF9("s18bmrf9", 2),
        S18BMRF10("s18bmrf10", 2),
        S18BMRF11("s18bmrf11", 2),
        S18BMRF12("s18bmrf12", 2),
        S18BMRF13("s18bmrf13", 2),
        S18BMRF14("s18bmrf14", 2),
        S18BMRF15("s18bmrf15", 2),
        S18BMRF16("s18bmrf16", 2);

        private final String name;
        private final int harvestLevel;

        private S18BMRFBlockType(String name, int harvestLevel) {
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
