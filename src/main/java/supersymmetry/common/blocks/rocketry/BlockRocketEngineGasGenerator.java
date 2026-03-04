package supersymmetry.common.blocks.rocketry;

import gregtech.api.block.IStateHarvestLevel;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;
import supersymmetry.api.blocks.VariantHorizontalRotatableBlock;

public class BlockRocketEngineGasGenerator extends VariantHorizontalRotatableBlock<BlockRocketEngineGasGenerator.GasGeneratorType> {

    public BlockRocketEngineGasGenerator() {
        super(Material.IRON);
        setTranslationKey("rocket_engine_gas_generator");
        setHardness(5f);
        setResistance(15f);
        setSoundType(SoundType.METAL);
        setDefaultState(getState(GasGeneratorType.OPEN_CYCLE));
        setHarvestLevel("wrench", 4);
    }

    public enum GasGeneratorType implements IStringSerializable, IStateHarvestLevel {

        OPEN_CYCLE("open_cycle", 4);

        private String name;
        private int harvest;

        GasGeneratorType(String name, int harvest) {
            this.name = name;
            this.harvest = harvest;
        }

        @Override
        public int getHarvestLevel(IBlockState iBlockState) {
            return harvest;
        }

        @Override
        public String getHarvestTool(IBlockState state) {
            return "wrench";
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
