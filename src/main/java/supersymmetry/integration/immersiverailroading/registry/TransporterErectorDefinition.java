package supersymmetry.integration.immersiverailroading.registry;

import cam72cam.immersiverailroading.model.StockModel;
import cam72cam.immersiverailroading.registry.FreightDefinitionBridge;
import cam72cam.immersiverailroading.util.DataBlock;
import supersymmetry.common.entities.EntityTransporterErector;
import supersymmetry.integration.immersiverailroading.model.TransporterErectorModel;

public class TransporterErectorDefinition extends FreightDefinitionBridge {

    public TransporterErectorDefinition(String defID, DataBlock data) throws Exception {
        super(EntityTransporterErector.class, defID, data);
    }

    protected StockModel<EntityTransporterErector, TransporterErectorDefinition> createModel() throws Exception {
        return new TransporterErectorModel(this);
    }
}
