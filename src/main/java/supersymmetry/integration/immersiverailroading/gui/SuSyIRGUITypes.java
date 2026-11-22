package supersymmetry.integration.immersiverailroading.gui;

import cam72cam.mod.gui.GuiRegistry;
import supersymmetry.common.entities.EntityTunnelBore;

public class SuSyIRGUITypes {

    public static final GuiRegistry.EntityGUI<EntityTunnelBore> TUNNEL_BORE = GuiRegistry
            .registerEntityContainer(EntityTunnelBore.class, TunnelBoreContainer::new);

    /**
     * Loads this class, so the GUIs above are registered. Must run on both sides during startup: a client that
     * joins a dedicated server otherwise never loads this class, and can't open the GUIs the server asks for.
     */
    public static void init() {}
}
