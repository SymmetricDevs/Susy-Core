package supersymmetry.common.blocks.active;

import supersymmetry.common.blocks.BlockSerpentine;

public class BlockInvertedActiveSerpentine extends BlockActiveSerpentine {

    public BlockInvertedActiveSerpentine() {
        super(true);
        setTranslationKey("serpentine_active_inverted");
        setDefaultState(getState(BlockSerpentine.SerpentineType.BASIC));
    }
}
