package supersymmetry.integration.immersiverailroading.registry;

import cam72cam.immersiverailroading.registry.EntityRollingStockDefinition;
import com.google.gson.JsonObject;
import supersymmetry.common.entities.EntityTunnelBore;

public class TunnelBoreDefinition extends EntityRollingStockDefinition {
    public TunnelBoreDefinition(String defID, JsonObject data) throws Exception {
        super(EntityTunnelBore.class, defID, data);
    }

    public double getBrakePower() {
        return 0.1;
    }
}
