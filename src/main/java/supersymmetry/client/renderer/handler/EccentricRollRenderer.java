package supersymmetry.client.renderer.handler;

import supersymmetry.common.tileentities.TileEntityEccentricRoll;

public class EccentricRollRenderer extends FixedGeoBlockRenderer<TileEntityEccentricRoll> {

    public EccentricRollRenderer() {
        super(new EccentricRollModel());
    }
}
