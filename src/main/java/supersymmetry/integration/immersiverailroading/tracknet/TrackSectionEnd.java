package supersymmetry.integration.immersiverailroading.tracknet;

import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class TrackSectionEnd extends TrackSectionBorder{
    public TrackSectionEnd(Vec3d pos) {
        super(pos);
    }

    @Override
    public List<TrackSection> getNeighbours(TrackSection section) {
        return new ArrayList<>();
    }
}
