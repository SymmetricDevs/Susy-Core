package supersymmetry.integration.immersiverailroading.tracknet;

import java.util.List;
import java.util.UUID;

public class TrackBlock {
    public UUID id;
    public List<TrackSection> sections;

    public boolean isDirectional = false;
    public TrackNode entryNode; // Set when unidirectional
}