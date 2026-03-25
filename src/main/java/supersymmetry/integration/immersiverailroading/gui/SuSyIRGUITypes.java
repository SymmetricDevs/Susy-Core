package supersymmetry.integration.immersiverailroading.gui;

import cam72cam.mod.gui.GuiRegistry;
import supersymmetry.common.entities.EntityTunnelBore;

public class SuSyIRGUITypes {

    public static final GuiRegistry.EntityGUI<EntityTunnelBore> TUNNEL_BORE = GuiRegistry
            .registerEntityContainer(EntityTunnelBore.class, TunnelBoreContainer::new);
}
