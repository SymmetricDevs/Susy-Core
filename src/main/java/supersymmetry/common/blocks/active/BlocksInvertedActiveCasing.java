package supersymmetry.common.blocks.active;

import supersymmetry.common.blocks.BlocksActiveCasing;

public class BlocksInvertedActiveCasing extends BlocksActiveCasing {

    public BlocksInvertedActiveCasing() {
        super();
        this.inverted = true;
        setTranslationKey("active_casing_inverted");
        setDefaultState(getState(BlocksActiveCasing.ActiveBlockType.BASIC_INTAKE_CASING));
    }
}
