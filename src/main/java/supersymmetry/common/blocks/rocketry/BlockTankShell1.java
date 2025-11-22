package supersymmetry.common.blocks.rocketry;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import supersymmetry.api.blocks.VariantDirectionalCoverableBlock;

public class BlockTankShell1 extends VariantDirectionalCoverableBlock<BlockTankShell1.TankCoverType> {

    public BlockTankShell1() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("rocket_tank_shell1");
        setHardness(5f);
        setResistance(15f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(BlockTankShell1.TankCoverType.CARBON_COMPOSITE));
        validCover = itemStack -> OreDictUnifier.get(OrePrefix.plate, Materials.Aluminium).isItemEqual(itemStack);
    }

    public enum TankCoverType implements IStringSerializable, IStateHarvestLevel {

        CARBON_COMPOSITE("carbon", 2);

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
