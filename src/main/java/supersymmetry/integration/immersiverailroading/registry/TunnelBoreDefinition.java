package supersymmetry.integration.immersiverailroading.registry;

import java.io.IOException;

import cam72cam.immersiverailroading.gui.overlay.GuiBuilder;
import cam72cam.immersiverailroading.model.StockModel;
import cam72cam.immersiverailroading.registry.LocomotiveDefinitionBridge;
import cam72cam.immersiverailroading.util.DataBlock;
import cam72cam.mod.resource.Identifier;
import supersymmetry.common.entities.EntityTunnelBore;
import supersymmetry.integration.immersiverailroading.model.TunnelBoreModel;

public class TunnelBoreDefinition extends LocomotiveDefinitionBridge {

    public TunnelBoreDefinition(String defID, DataBlock data) throws Exception {
        super(EntityTunnelBore.class, defID, data);
    }

    protected GuiBuilder getDefaultOverlay(DataBlock data) throws IOException {
        return GuiBuilder.parse(new Identifier("immersiverailroading", "gui/default/tunnelbore.caml"));
    }

    protected StockModel<EntityTunnelBore, TunnelBoreDefinition> createModel() throws Exception {
        return new TunnelBoreModel(this);
    }
}
