package supersymmetry.common.blocks.rocketry;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;
import supersymmetry.api.blocks.VariantHorizontalRotatableBlock;

public class BlockRocketNozzle extends VariantBlock<BlockRocketNozzle.NozzleShapeType> {
    public BlockRocketNozzle() {
        super(Material.IRON);
        setTranslationKey("rocket_nozzle");
        setHardness(7f);
        setResistance(25f);
        setSoundType(SoundType.METAL);
        setDefaultState(getState(NozzleShapeType.BELL_NOZZLE));
    }

    public enum NozzleShapeType implements IStringSerializable, IStateHarvestLevel {
        BELL_NOZZLE("bell_basic",3),
        PLUG_NOZZLE("plug",4), //note: these must be used with plug blocks
        EXPANDING_NOZZLE("expanding", 3);

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
}
