package supersymmetry.common.blocks;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;

public class BlockDeposit extends VariantBlock<BlockDeposit.DepositBlockType> {
    public BlockDeposit() {
        super(net.minecraft.block.material.Material.ANVIL);
        setTranslationKey("deposit_block");
        setHardness(50.0f);
        setResistance(1200.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 100);
        setDefaultState(getState(DepositBlockType.ORTHOMAGMATIC));
    }

    public static enum DepositBlockType implements IStringSerializable, IStateHarvestLevel {
        ORTHOMAGMATIC("orthomagmatic", 10),
        METAMORPHIC("metamorphic", 10),
        SEDIMENTARY("sedimentary", 10),
        HYDROTHERMAL("hydrothermal", 10),
        ALLUVIAL("alluvial", 10),
        MAGMATIC_HYDROTHERMAL("magmatic_hydrothermal", 10);

        private final String name;
        private final int harvestLevel;

        private DepositBlockType(String name, int harvestLevel) {
            this.name = name;
            this.harvestLevel = harvestLevel;
        }

        @Nonnull
        public String getName() {
            return this.name;
        }

        public int getHarvestLevel(IBlockState state) {
            return this.harvestLevel;
        }

        public String getHarvestTool(IBlockState state) {
            return "wrench";
        }
    }
}
