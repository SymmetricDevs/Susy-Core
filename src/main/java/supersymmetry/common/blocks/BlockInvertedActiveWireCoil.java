package supersymmetry.common.blocks;

import gregtech.common.blocks.BlockWireCoil;

public class BlockInvertedActiveWireCoil extends BlockActiveWireCoil {

    public BlockInvertedActiveWireCoil() {
        super(true);
        setTranslationKey("wire_coil_active_inverted");
        setDefaultState(getState(BlockWireCoil.CoilType.CUPRONICKEL));
    }
}
