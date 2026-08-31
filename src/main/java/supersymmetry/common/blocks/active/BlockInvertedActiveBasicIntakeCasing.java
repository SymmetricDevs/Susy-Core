package supersymmetry.common.blocks.active;

import supersymmetry.common.blocks.BlocksActiveCasing;

public class BlockInvertedActiveBasicIntakeCasing extends BlockActiveBasicIntakeCasing {

    public BlockInvertedActiveBasicIntakeCasing() {
        super(true);
        setTranslationKey("basic_intake_casing_active_inverted");
        setDefaultState(getState(BlocksActiveCasing.ActiveBlockType.BASIC_INTAKE_CASING));
    }
}
