package supersymmetry.common.blocks;

public class BlockInvertedActiveAssemblyLine extends BlockActiveAssemblyLine {

    public BlockInvertedActiveAssemblyLine() {
        super(true);
        setTranslationKey("assembly_line_active_inverted");
        setDefaultState(getState(BlockActiveAssemblyLine.AssemblyLineType.ASSEMBLY_LINE));
    }
}
