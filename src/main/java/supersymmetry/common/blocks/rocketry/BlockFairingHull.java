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
import supersymmetry.api.blocks.VariantDirectionalRotatableBlock;

public class BlockFairingHull extends VariantDirectionalCoverableBlock<BlockFairingHull.FairingType> {
    public BlockFairingHull() {
        super(Material.IRON);
        setTranslationKey("rocket_fairing");
        setHardness(5f);
        setResistance(15f);
        setSoundType(SoundType.METAL);
        setDefaultState(getState(FairingType.ALUMINIUM_FAIRING));
        validCover = itemStack -> OreDictUnifier.get(OrePrefix.plate, Materials.SiliconDioxide).isItemEqual(itemStack);
    }

    public enum FairingType implements IStringSerializable, IStateHarvestLevel {
        ALUMINIUM_FAIRING("al_7075",3);
        String name;
        int harvest;

        FairingType(String name, int harvest) {
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
