package supersymmetry.common.blocks.rocketry;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;
import supersymmetry.api.rocketry.WeightedBlock;

public class BlockRocketNozzle extends VariantBlock<BlockRocketNozzle.NozzleShapeType> implements WeightedBlock {

    public BlockRocketNozzle() {
        super(Material.IRON);
        setTranslationKey("rocket_nozzle");
        setHardness(7f);
        setResistance(25f);
        setSoundType(SoundType.METAL);
        setDefaultState(getState(NozzleShapeType.BELL_NOZZLE));
        setHarvestLevel("wrench", 4);
    }

    public enum NozzleShapeType implements IStringSerializable, IStateHarvestLevel {

        BELL_NOZZLE("bell_basic", 4),
        PLUG_NOZZLE("plug", 4), // note: these must be used with plug blocks
        EXPANDING_NOZZLE("expanding", 4);

        private String name;
        private int harvestLevel;

        NozzleShapeType(String name, int harvestLevel) {
            this.name = name;
            this.harvestLevel = harvestLevel;
        }

        @Override
        public int getHarvestLevel(IBlockState iBlockState) {
            return this.harvestLevel;
        }

        @Override
        public String getHarvestTool(IBlockState state) {
            return "wrench";
        }

        @Override
        public String getName() {
            return this.name;
        }
    }

    @Override
    public double getMass(IBlockState state) {
        NozzleShapeType type = getState(state);
        double multiplier = switch (type) {
            case BELL_NOZZLE -> 60.0;
            case PLUG_NOZZLE -> 65.0;
            case EXPANDING_NOZZLE -> 80.0;
        };
        return 500 + 100 * multiplier;
    }
}
