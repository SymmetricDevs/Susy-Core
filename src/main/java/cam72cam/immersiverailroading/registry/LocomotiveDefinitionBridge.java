package cam72cam.immersiverailroading.registry;

import cam72cam.immersiverailroading.entity.EntityRollingStock;
import com.google.gson.JsonObject;

public abstract class LocomotiveDefinitionBridge extends LocomotiveDefinition {

    public LocomotiveDefinitionBridge(Class<? extends EntityRollingStock> type, String defID, JsonObject data) throws Exception {
        super(type, defID, data);
    }
}
