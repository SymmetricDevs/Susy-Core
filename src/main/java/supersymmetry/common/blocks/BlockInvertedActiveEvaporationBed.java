package supersymmetry.common.blocks;

public class BlockInvertedActiveEvaporationBed extends BlockActiveEvaporationBed {

    public BlockInvertedActiveEvaporationBed() {
        super(true);
        setTranslationKey("evaporation_bed_active_inverted");
        setDefaultState(getState(BlockEvaporationBed.EvaporationBedType.DIRT));
    }
}
