package supersymmetry.common.blocks.rocketry;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;

import gregtech.api.block.IStateHarvestLevel;
import supersymmetry.api.blocks.VariantHorizontalRotatableBlock;

public class BlockRocketControl extends VariantHorizontalRotatableBlock<BlockRocketControl.RocketControlType> {

    public BlockRocketControl() {
        super(Material.IRON);
        setTranslationKey("rocket_control");
        setHardness(5f);
        setResistance(15f);
        setSoundType(SoundType.METAL);
        setDefaultState(getState(RocketControlType.ROCKET_CONTROL));
        setHarvestLevel("wrench", 1);
    }

    public enum RocketControlType implements IStringSerializable, IStateHarvestLevel {

        ROCKET_CONTROL("basic", 1);

        private String name;
        private int harvest;

        RocketControlType(String name, int harvest) {
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
