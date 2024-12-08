package supersymmetry.common.blocks.rocketry;

import gregtech.api.block.IStateHarvestLevel;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;
import supersymmetry.api.blocks.VariantDirectionalRotatableBlock;

public class BlockTankShell extends VariantDirectionalRotatableBlock<BlockTankShell.TankCoverType> {
    public BlockTankShell() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("rocket_tank_shell");
        setHardness(5f);
        setResistance(15f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench",2);
        setDefaultState(getState(TankCoverType.TANK_SHELL));
    }
    public enum TankCoverType implements IStringSerializable, IStateHarvestLevel {
        TANK_SHELL("al_2219",2);

        private String name;
        private int harvestLevel;

        TankCoverType(String name, int harvestLevel) {
            this.name = name;
            this.harvestLevel = harvestLevel;
        }
        @Override
        public int getHarvestLevel(IBlockState iBlockState) {
            return harvestLevel;
        }

        @Override
        public String getHarvestTool(IBlockState state) {
            return "wrench";
        }

        public String getName() {
            return name;
        }
    }
}
