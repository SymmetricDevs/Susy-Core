package supersymmetry.common.blocks;

import gregicality.multiblocks.common.block.blocks.BlockUniqueCasing.UniqueCasingType;

public class BlockInvertedActiveHeatVent extends BlockActiveHeatVent {

    public BlockInvertedActiveHeatVent() {
        super(true);
        setTranslationKey("heat_vent_active_inverted");
        setDefaultState(getState(UniqueCasingType.HEAT_VENT));
    }
}
