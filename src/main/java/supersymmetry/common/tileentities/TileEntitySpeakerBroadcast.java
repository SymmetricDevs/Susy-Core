package supersymmetry.common.tileentities;

import supersymmetry.integration.opencomputers.ComponentSpeakerBroadcast;

public class TileEntitySpeakerBroadcast extends TileEntitySpeaker {

    public TileEntitySpeakerBroadcast() {
        speaker = new ComponentSpeakerBroadcast(this);
    }
}
