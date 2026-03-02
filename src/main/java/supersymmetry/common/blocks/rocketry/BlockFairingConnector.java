package supersymmetry.common.blocks.rocketry;

public class BlockFairingConnector extends BlockFairingHull {

    public BlockFairingConnector() {
        super();
        setTranslationKey("rocket_fairing_connector");
        setDefaultState(getState(FairingType.ALUMINIUM_FAIRING));
        setHarvestLevel("wrench", 4);
    }
}
