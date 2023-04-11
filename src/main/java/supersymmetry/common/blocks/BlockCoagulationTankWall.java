package supersymmetry.common.blocks;

import gregtech.api.block.IStateHarvestLevel;

import gregtech.api.block.VariantBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;

public class BlockCoagulationTankWall extends VariantBlock<BlockCoagulationTankWall.CoagulationTankWallType> {
    public BlockCoagulationTankWall(){
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("coagulation_tank_wall");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(BlockCoagulationTankWall.CoagulationTankWallType.WOODEN_COAGULATION_TANK_WALL));
    }

    public enum CoagulationTankWallType implements IStringSerializable, IStateHarvestLevel {
        WOODEN_COAGULATION_TANK_WALL("wooden_coagulation_tank_wall", 1);

        private final String name;
        private final int harvestLevel;

        CoagulationTankWallType(String name, int harvestLevel) {
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
