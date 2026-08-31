package supersymmetry.common.blocks.active;

import supersymmetry.common.blocks.BlockCoolingCoil;

public class BlockInvertedActiveCoolingCoil extends BlockActiveCoolingCoil {

    public BlockInvertedActiveCoolingCoil() {
        super(true);
        setTranslationKey("cooling_coil_active_inverted");
        setDefaultState(getState(BlockCoolingCoil.CoolingCoilType.MANGANESE_IRON_ARSENIC_PHOSPHIDE));
    }
}
