package supersymmetry.common.blocks.rocketry;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import supersymmetry.api.blocks.VariantDirectionalCoverableBlock;

public class BlockTankShell extends VariantDirectionalCoverableBlock<BlockTankShell.TankCoverType> {

    public BlockTankShell() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("rocket_tank_shell");
        setHardness(5f);
        setResistance(15f);
        setSoundType(SoundType.METAL);
        setDefaultState(getState(TankCoverType.TANK_SHELL));
        setHarvestLevel("wrench", 2);
        validCover = itemStack -> OreDictUnifier.get(OrePrefix.plate, Materials.Aluminium).isItemEqual(itemStack);
    }

    public enum TankCoverType implements IStringSerializable, IStateHarvestLevel {

        TANK_SHELL("al_2219", 2),
        STEEL_SHELL("steel", 3);

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
