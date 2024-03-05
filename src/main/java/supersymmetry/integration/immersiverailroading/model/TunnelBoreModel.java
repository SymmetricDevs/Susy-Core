package supersymmetry.integration.immersiverailroading.model;

import cam72cam.immersiverailroading.model.ComponentRenderer;
import cam72cam.immersiverailroading.model.StockModel;
import cam72cam.immersiverailroading.model.components.ComponentProvider;
import cam72cam.immersiverailroading.registry.EntityRollingStockDefinition;
import supersymmetry.common.entities.EntityTunnelBore;
import supersymmetry.integration.immersiverailroading.model.part.Borer;

public class TunnelBoreModel extends StockModel<EntityTunnelBore> {

    public Borer borer;

    public TunnelBoreModel(EntityRollingStockDefinition def) throws Exception {
        super(def);
    }

    @Override
    protected void parseComponents(ComponentProvider provider, EntityRollingStockDefinition def) {
        super.parseComponents(provider, def);
        this.borer = new Borer(provider);
    }

    @Override
    protected void render(EntityTunnelBore bore, ComponentRenderer draw, double distanceTraveled) {
        super.render(bore, draw, distanceTraveled);
        this.borer.render(bore.getBorerAngle(), draw);
    }
}
