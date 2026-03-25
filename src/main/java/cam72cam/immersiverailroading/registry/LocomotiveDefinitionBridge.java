package cam72cam.immersiverailroading.registry;

import cam72cam.immersiverailroading.entity.EntityRollingStock;
import cam72cam.immersiverailroading.util.DataBlock;

public abstract class LocomotiveDefinitionBridge extends LocomotiveDefinition {

    public LocomotiveDefinitionBridge(Class<? extends EntityRollingStock> type, String defID,
                                      DataBlock data) throws Exception {
        super(type, defID, data);
    }
}
