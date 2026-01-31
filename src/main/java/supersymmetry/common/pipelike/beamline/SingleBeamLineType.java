package supersymmetry.common.pipelike.beamline;

import supersymmetry.api.pipenet.beamline.BeamLineType;

public class SingleBeamLineType extends BeamLineType {

    public static final SingleBeamLineType INSTANCE = new SingleBeamLineType();

    private SingleBeamLineType() {
        super("single");
    }

    @Override
    public int getMinLength() {
        return 1;
    }
}
