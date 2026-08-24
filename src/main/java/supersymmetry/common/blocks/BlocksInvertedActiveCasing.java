package supersymmetry.common.blocks;

public class BlocksInvertedActiveCasing extends BlocksActiveCasing {

    public BlocksInvertedActiveCasing() {
        super();
        this.inverted = true;
        setTranslationKey("active_casing_inverted");
        setDefaultState(getState(BlocksActiveCasing.ActiveBlockType.BASIC_INTAKE_CASING));
    }
}
