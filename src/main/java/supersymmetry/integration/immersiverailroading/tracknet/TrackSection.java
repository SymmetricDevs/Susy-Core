package supersymmetry.integration.immersiverailroading.tracknet;

import cam72cam.immersiverailroading.tile.TileRail;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TrackSection {
    public List<TileRail> rails;
    public TrackNode startNode;
    public TrackNode endNode;
    // What block it belongs to
    UUID trackBlockId;
    boolean bidirectional; // Determined from signals

    public TrackSection(TrackNode startNode, TrackNode endNode) {
        this.rails = new ArrayList<>();
        this.startNode = startNode;
        this.endNode = endNode;
    }


    public TrackNode oppositeNode(TrackNode node) {
        if (node == startNode) return endNode;
        if (node == endNode) return startNode;
        return null;
    }
}
