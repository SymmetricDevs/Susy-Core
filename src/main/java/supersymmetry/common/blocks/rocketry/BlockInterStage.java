package supersymmetry.common.blocks.rocketry;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;
import supersymmetry.api.blocks.VariantDirectionalCoverableBlock;

public class BlockInterStage extends VariantDirectionalCoverableBlock<BlockInterStage.InterStageType> {
    public BlockInterStage() {
        super(Material.IRON);
        setTranslationKey("rocket_interstage");
        setHardness(5f);
        setResistance(15f);
        setSoundType(SoundType.METAL);
        setDefaultState(getState(InterStageType.AL_7075));
        validCover = itemStack -> OreDictUnifier.get(OrePrefix.plate, Materials.Aluminium).isItemEqual(itemStack);
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
