package supersymmetry.common.blocks;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;

public class BlockDeposit extends VariantBlock<BlockDeposit.DepositBlockType> {

    public BlockDeposit() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("deposit_block");
        setHardness(50.0f);
        setResistance(1200.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 100);
        setDefaultState(getState(DepositBlockType.DEPOSIT_BLOCK_CARBON));
    }

    public static enum DepositBlockType implements IStringSerializable, IStateHarvestLevel {
        DEPOSIT_BLOCK_CARBON("deposit_block_carbon", 10),
        DEPOSIT_BLOCK_CARBONATE("deposit_block_carbonate", 10),
        DEPOSIT_BLOCK_CLAY("deposit_block_clay", 10),
        DEPOSIT_BLOCK_OXIDE("deposit_block_oxide", 10),
        DEPOSIT_BLOCK_PHOSPHATE("deposit_block_phosphate", 10),
        DEPOSIT_BLOCK_PRECIOUS("deposit_block_precious", 10),
        DEPOSIT_BLOCK_RADIOACTIVE("deposit_block_radioactive", 10),
        DEPOSIT_BLOCK_SEDIMENTARY("deposit_block_sedimentary", 10),
        DEPOSIT_BLOCK_SILICATE("deposit_block_silicate", 10),
        DEPOSIT_BLOCK_SULFUR("deposit_block_sulfur", 10);

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
