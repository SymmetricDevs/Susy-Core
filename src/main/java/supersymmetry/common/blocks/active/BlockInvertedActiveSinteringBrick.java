package supersymmetry.common.blocks.active;

public class BlockInvertedActiveSinteringBrick extends BlockActiveSinteringBrick {

    public BlockInvertedActiveSinteringBrick() {
        super(true);
        setTranslationKey("sintering_brick_active_inverted");
        setDefaultState(getState(ActiveSinteringBrickType.BRICK));
    }
}
