package supersymmetry.integration.immersiverailroading.tracknet;

import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class TrackSwitch extends TrackSectionBorder {

    TrackSection straightEnterSection;
    TrackSection straightLeaveSection;
    TrackSection turnLeaveSection;

    public TrackSwitch(Vec3d pos, TrackSection straightEnterSection, TrackSection straightLeaveSection, TrackSection turnLeaveSection ) {
        super(pos);

        this.straightEnterSection = straightEnterSection;
        this.straightLeaveSection = straightLeaveSection;
        this.turnLeaveSection = turnLeaveSection;
    }

    @Override
    public List<TrackSection> getNeighbours(TrackSection section) {
        List<TrackSection> result = new ArrayList<>();
        if(section == straightEnterSection) {
            result.add(straightLeaveSection);
            result.add(turnLeaveSection);
        } else {
            result.add(straightEnterSection);
        }

        return result;
    }
}
