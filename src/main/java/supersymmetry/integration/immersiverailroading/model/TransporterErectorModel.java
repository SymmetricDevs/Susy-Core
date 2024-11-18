package supersymmetry.integration.immersiverailroading.model;

import cam72cam.immersiverailroading.model.StockModel;
import supersymmetry.common.entities.EntityTransporterErector;
import supersymmetry.integration.immersiverailroading.registry.TransporterErectorDefinition;

public class TransporterErectorModel extends StockModel<EntityTransporterErector, TransporterErectorDefinition> {

    public TransporterErectorModel(TransporterErectorDefinition def) throws Exception {
        super(def);
    }

}
