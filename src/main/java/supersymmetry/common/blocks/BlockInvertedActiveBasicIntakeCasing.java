package supersymmetry.common.blocks;

public class BlockInvertedActiveBasicIntakeCasing extends BlockActiveBasicIntakeCasing {

    public BlockInvertedActiveBasicIntakeCasing() {
        super(true);
        setTranslationKey("basic_intake_casing_active_inverted");
        setDefaultState(getState(BlocksActiveCasing.ActiveBlockType.BASIC_INTAKE_CASING));
    }
}
