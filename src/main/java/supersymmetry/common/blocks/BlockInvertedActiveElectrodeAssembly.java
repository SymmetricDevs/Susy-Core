package supersymmetry.common.blocks;

public class BlockInvertedActiveElectrodeAssembly extends BlockActiveElectrodeAssembly {

    public BlockInvertedActiveElectrodeAssembly() {
        super(true);
        setTranslationKey("electrode_assembly_active_inverted");
        setDefaultState(getState(BlockElectrodeAssembly.ElectrodeAssemblyType.CARBON));
    }
}
