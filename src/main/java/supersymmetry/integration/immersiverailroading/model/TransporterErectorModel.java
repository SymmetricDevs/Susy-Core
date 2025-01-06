package supersymmetry.integration.immersiverailroading.model;

import cam72cam.immersiverailroading.model.StockModel;
import cam72cam.immersiverailroading.model.components.ComponentProvider;
import supersymmetry.common.entities.EntityTransporterErector;
import supersymmetry.integration.immersiverailroading.model.part.Rocket;
import supersymmetry.integration.immersiverailroading.registry.TransporterErectorDefinition;

public class TransporterErectorModel extends StockModel<EntityTransporterErector, TransporterErectorDefinition> {

    public Rocket rocket;

    public TransporterErectorModel(TransporterErectorDefinition def) throws Exception {
        super(def);
    }

    @Override
    protected void parseComponents(ComponentProvider provider, TransporterErectorDefinition def) {
        super.parseComponents(provider, def);
        this.rocket = new Rocket(provider, this.base, (stock) -> stock.isRocketLoaded());
    }

}
