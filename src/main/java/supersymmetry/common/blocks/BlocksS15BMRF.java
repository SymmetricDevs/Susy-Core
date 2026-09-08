package supersymmetry.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

import org.jetbrains.annotations.NotNull;

import supersymmetry.api.blocks.VariantHorizontalRotatableBlock;

import gregtech.api.block.IStateHarvestLevel;

public class BlocksS15BMRF extends VariantHorizontalRotatableBlock<BlocksS15BMRF.S15BMRFBlockType> {

    public BlocksS15BMRF() {
        super(Material.ROCK);
        this.setHardness(3.0F);
        this.setResistance(5.0F);
        this.setSoundType(SoundType.STONE);
        this.setTranslationKey("s15bmrf_blocks");
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
    public ItemStack getItemVariant(S15BMRFBlockType variant, int amount) {
        return new ItemStack(this, amount, variant.ordinal());
    }

    @Override
    public int damageDropped(@NotNull IBlockState state) {
        return state.getValue(VARIANT).ordinal();
    }

    public static enum S15BMRFBlockType implements IStringSerializable, IStateHarvestLevel {

        S15BMRF1("s15bmrf1", 2),
        S15BMRF2("s15bmrf2", 2),
        S15BMRF3("s15bmrf3", 2),
        S15BMRF4("s15bmrf4", 2),
        S15BMRF5("s15bmrf5", 2),
        S15BMRF6("s15bmrf6", 2),
        S15BMRF7("s15bmrf7", 2),
        S15BMRF8("s15bmrf8", 2),
        S15BMRF9("s15bmrf9", 2),
        S15BMRF10("s15bmrf10", 2),
        S15BMRF11("s15bmrf11", 2),
        S15BMRF12("s15bmrf12", 2),
        S15BMRF13("s15bmrf13", 2),
        S15BMRF14("s15bmrf14", 2),
        S15BMRF15("s15bmrf15", 2),
        S15BMRF16("s15bmrf16", 2);

        private final String name;
        private final int harvestLevel;

        private S15BMRFBlockType(String name, int harvestLevel) {
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
