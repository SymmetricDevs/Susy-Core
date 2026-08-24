package supersymmetry.common.blocks;

import gregtech.common.blocks.BlockFusionCasing.CasingType;

public class BlockInvertedActiveFusionCasing extends BlockActiveFusionCasing {

    public BlockInvertedActiveFusionCasing() {
        super(true);
        setTranslationKey("fusion_casing_active_inverted");
        setDefaultState(getState(CasingType.FUSION_CASING));
    }
}
