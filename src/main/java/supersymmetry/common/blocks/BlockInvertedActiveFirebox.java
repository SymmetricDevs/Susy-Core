package supersymmetry.common.blocks;

import gregtech.common.blocks.BlockFireboxCasing.FireboxCasingType;

public class BlockInvertedActiveFirebox extends BlockActiveFirebox {

    public BlockInvertedActiveFirebox() {
        super(true);
        setTranslationKey("firebox_active_inverted");
        setDefaultState(getState(FireboxCasingType.BRONZE_FIREBOX));
    }
}
