package supersymmetry.integration.immersiverailroading.model;

import cam72cam.immersiverailroading.model.StockModel;
import cam72cam.immersiverailroading.model.components.ComponentProvider;
import supersymmetry.common.entities.EntityTunnelBore;
import supersymmetry.integration.immersiverailroading.model.part.Borer;
import supersymmetry.integration.immersiverailroading.registry.TunnelBoreDefinition;

public class TunnelBoreModel extends StockModel<EntityTunnelBore, TunnelBoreDefinition> {

    public Borer borer;

    public TunnelBoreModel(TunnelBoreDefinition def) throws Exception {
        super(def);
    }

    @Override
    protected void parseComponents(ComponentProvider provider, TunnelBoreDefinition def) {
        super.parseComponents(provider, def);
        this.borer = new Borer(provider, this.base, (stock) -> stock.getBorerAngle());
    }
}
