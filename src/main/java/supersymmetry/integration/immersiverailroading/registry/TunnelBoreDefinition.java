package supersymmetry.integration.immersiverailroading.registry;

import cam72cam.immersiverailroading.model.StockModel;
import cam72cam.immersiverailroading.registry.LocomotiveDefinitionBridge;
import com.google.gson.JsonObject;
import supersymmetry.common.entities.EntityTunnelBore;
import supersymmetry.integration.immersiverailroading.model.TunnelBoreModel;


public class TunnelBoreDefinition extends LocomotiveDefinitionBridge {
    public TunnelBoreDefinition(String defID, JsonObject data) throws Exception {
        super(EntityTunnelBore.class, defID, data);
    }

    protected StockModel<?> createModel() throws Exception {
        return new TunnelBoreModel(this);
    }
}
