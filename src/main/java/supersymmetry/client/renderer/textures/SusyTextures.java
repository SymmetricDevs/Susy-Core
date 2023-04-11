package supersymmetry.client.renderer.textures;

import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.relauncher.Side;
import supersymmetry.Supersymmetry;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID, value = {Side.CLIENT})
public class SusyTextures {
    public static SimpleSidedCubeRenderer WOODEN_COAGULATION_TANK_WALL;
    public static OrientedOverlayRenderer VULCANIZING_PRESS_OVERLAY;
    public static OrientedOverlayRenderer LATEX_COLLECTOR_OVERLAY;
    public static OrientedOverlayRenderer ROASTER_OVERLAY;
    public static OrientedOverlayRenderer MIXER_OVERLAY_STEAM;

    public SusyTextures(){
    }

    public static void preInit(){
        WOODEN_COAGULATION_TANK_WALL = new SimpleSidedCubeRenderer("casings/wooden_coagulation_tank_wall");
        VULCANIZING_PRESS_OVERLAY = new OrientedOverlayRenderer("machines/vulcanizing_press", OrientedOverlayRenderer.OverlayFace.FRONT, OrientedOverlayRenderer.OverlayFace.SIDE, OrientedOverlayRenderer.OverlayFace.TOP);
        LATEX_COLLECTOR_OVERLAY = new OrientedOverlayRenderer("machines/latex_collector");
        ROASTER_OVERLAY = new OrientedOverlayRenderer("machines/roaster");
        MIXER_OVERLAY_STEAM = new OrientedOverlayRenderer("machines/mixer_steam");
    }
}
