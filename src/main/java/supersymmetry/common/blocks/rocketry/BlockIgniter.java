package supersymmetry.common.blocks.rocketry;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;

public class BlockIgniter extends VariantBlock<BlockIgniter.BlockIgniterType> implements IStringSerializable {

    public BlockIgniter() {
        super(Material.IRON);
        setTranslationKey("solid_rocket_igniter");
        setHardness(5f);
        setResistance(15f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 3);
    }

    @Override
    public String getName() {
        return "solid_rocket_igniter";
    }

    public enum BlockIgniterType implements IStringSerializable, IStateHarvestLevel {

        DEFAULT("default", 4);

        private String name;
        private int harvest;

        BlockIgniterType(String name, int harvest) {
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
