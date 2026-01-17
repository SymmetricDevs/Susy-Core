package supersymmetry.common.blocks.rocketry;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;

public class BlockGuidanceSystem extends VariantBlock<BlockGuidanceSystem.GuidanceSystemType> {

    public BlockGuidanceSystem() {
        super(Material.IRON);
        setTranslationKey("guidance_system");
        setHardness(5f);
        setResistance(15f);
        setSoundType(SoundType.METAL);
        setDefaultState(getState(BlockGuidanceSystem.GuidanceSystemType.SOYUZ));
        setHarvestLevel("wrench", 4);
    }

    public enum GuidanceSystemType implements IStringSerializable, IStateHarvestLevel {

        SOYUZ("soyuz", 4);

        String name;
        int harvest;

        GuidanceSystemType(String name, int harvest) {
            this.name = name;
            this.harvest = harvest;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public int getHarvestLevel(IBlockState iBlockState) {
            return harvest;
        }

        @Override
        public String getHarvestTool(IBlockState state) {
            return IStateHarvestLevel.super.getHarvestTool(state);
        }
    }
}
