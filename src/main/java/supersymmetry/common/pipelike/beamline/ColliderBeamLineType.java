package supersymmetry.common.pipelike.beamline;

import supersymmetry.api.pipenet.beamline.BeamLineType;

public class ColliderBeamLineType extends BeamLineType {

    public static final ColliderBeamLineType INSTANCE = new ColliderBeamLineType();

    private ColliderBeamLineType() {
        super("collider");
    }

    @Override
    public int getMinLength() {
        return 1;
    }
}
