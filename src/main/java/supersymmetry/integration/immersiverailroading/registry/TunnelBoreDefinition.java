package supersymmetry.integration.immersiverailroading.registry;

import cam72cam.immersiverailroading.registry.EntityRollingStockDefinition;
import cam72cam.immersiverailroading.util.DataBlock;
import supersymmetry.common.entities.EntityTunnelBore;

public class TunnelBoreDefinition extends EntityRollingStockDefinition {
    public TunnelBoreDefinition(String defID, DataBlock data) throws Exception {
        super(EntityTunnelBore.class, defID, data);
    }

    public double getBrakePower() {
        return 0.1;
    }
}
