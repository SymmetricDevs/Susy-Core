package supersymmetry.common.blocks;

public class BlockInvertedActiveMolybdenumDisilicideCoil extends BlockActiveMolybdenumDisilicideCoil {

    public BlockInvertedActiveMolybdenumDisilicideCoil() {
        super(true);
        setTranslationKey("molybdenum_disilicide_coil_active_inverted");
        setDefaultState(getState(CoilType.MOLYBDENUM_DISILICIDE_COIL));
    }
}
