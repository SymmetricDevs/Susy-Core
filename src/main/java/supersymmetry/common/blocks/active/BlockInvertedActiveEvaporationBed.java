package supersymmetry.common.blocks.active;

import supersymmetry.common.blocks.BlockEvaporationBed;

public class BlockInvertedActiveEvaporationBed extends BlockActiveEvaporationBed {

    public BlockInvertedActiveEvaporationBed() {
        super(true);
        setTranslationKey("evaporation_bed_active_inverted");
        setDefaultState(getState(BlockEvaporationBed.EvaporationBedType.DIRT));
    }
}
