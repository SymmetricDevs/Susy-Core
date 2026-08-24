package supersymmetry.common.blocks;

public class BlockInvertedActiveSerpentine extends BlockActiveSerpentine {

    public BlockInvertedActiveSerpentine() {
        super(true);
        setTranslationKey("serpentine_active_inverted");
        setDefaultState(getState(BlockSerpentine.SerpentineType.BASIC));
    }
}
