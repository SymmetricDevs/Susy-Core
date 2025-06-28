package supersymmetry.common.blocks.rocketry;

import supersymmetry.api.blocks.VariantDirectionalCoverableBlock;

public class BlockFairingConnector extends BlockFairingHull {
    public BlockFairingConnector() {
        super();
        setTranslationKey("rocket_fairing_connector");
        setDefaultState(getState(FairingType.ALUMINIUM_FAIRING));
    }
}
