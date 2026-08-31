package supersymmetry.common.blocks.active;

public class BlockInvertedActiveEngineIntake extends BlockActiveEngineIntake {

    public BlockInvertedActiveEngineIntake() {
        super(true);
        setTranslationKey("engine_intake_active_inverted");
        setDefaultState(getState(BlockActiveEngineIntake.EngineIntakeType.ENGINE_INTAKE));
    }
}
