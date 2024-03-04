package supersymmetry.integration.immersiverailroading.registry;

import cam72cam.immersiverailroading.registry.LocomotiveDefinitionBridge;
import com.google.gson.JsonObject;
import supersymmetry.common.entities.EntityTunnelBore;


public class TunnelBoreDefinition extends LocomotiveDefinitionBridge {
    public TunnelBoreDefinition(String defID, JsonObject data) throws Exception {
        super(EntityTunnelBore.class, defID, data);
    }
}
