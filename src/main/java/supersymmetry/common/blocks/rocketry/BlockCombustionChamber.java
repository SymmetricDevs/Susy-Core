package supersymmetry.common.blocks.rocketry;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;

public class BlockCombustionChamber extends VariantBlock<BlockCombustionChamber.CombustionType> {

    public BlockCombustionChamber() {
        super(Material.IRON);
        setTranslationKey("rocket_combustion_chamber");
        setHardness(5f);
        setResistance(15f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(CombustionType.BIPROPELLANT));
    }

    public enum CombustionType implements IStringSerializable, IStateHarvestLevel {
        BIPROPELLANT("bipropellant",2),
        MONOPROPELLANT("monopropellant",2),
        OXIDISER("oxidiser",2);

        String name;
        int harvest;

        CombustionType(String name, int harvest) {
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
            return "wrench";
        }
    }
}
