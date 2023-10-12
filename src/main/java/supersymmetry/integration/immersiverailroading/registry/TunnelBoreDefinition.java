package supersymmetry.integration.immersiverailroading.registry;

import cam72cam.immersiverailroading.gui.overlay.GuiBuilder;
import cam72cam.immersiverailroading.registry.LocomotiveDefinitionBridge;
import cam72cam.immersiverailroading.util.DataBlock;
import cam72cam.mod.resource.Identifier;
import supersymmetry.common.entities.EntityTunnelBore;

import java.io.IOException;

public class TunnelBoreDefinition extends LocomotiveDefinitionBridge {
    public TunnelBoreDefinition(String defID, DataBlock data) throws Exception {
        super(EntityTunnelBore.class, defID, data);
    }
    protected GuiBuilder getDefaultOverlay(DataBlock data) throws IOException {
        return GuiBuilder.parse(new Identifier("immersiverailroading", "gui/default/handcar.caml"));
    }
    @Override
    protected boolean multiUnitDefault() {
        return false;
    }
}
