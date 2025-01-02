package supersymmetry.common.blocks.rocketry;

import gregtech.api.block.IStateHarvestLevel;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;
import supersymmetry.api.blocks.VariantHorizontalRotatableBlock;

public class BlockInterStage extends VariantHorizontalRotatableBlock<BlockInterStage.InterStageType> {
    public BlockInterStage() {
        super(Material.IRON);
        setTranslationKey("rocket_interstage");
        setHardness(5f);
        setResistance(15f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench",2);
        setDefaultState(getState(InterStageType.AL_7075));
    }

    public enum InterStageType implements IStringSerializable, IStateHarvestLevel {
        AL_7075("al_7075",2);
        String name;
        int harvest;

        InterStageType(String name, int harvest) {
            this.name = name;
            this.harvest = harvest;
        }

        @Override
        public int getHarvestLevel(IBlockState iBlockState) {
            return harvest;
        }

        @Override
        public String getHarvestTool(IBlockState state) {
            return IStateHarvestLevel.super.getHarvestTool(state);
        }

        @Override
        public String getName() {
            return name;
        }
    }
}