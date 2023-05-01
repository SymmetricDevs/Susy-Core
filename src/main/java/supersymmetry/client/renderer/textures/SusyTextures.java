package supersymmetry.client.renderer.textures;

import gregtech.api.gui.resources.SteamTexture;
import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.relauncher.Side;
import supersymmetry.Supersymmetry;

import static gregtech.client.renderer.texture.cube.OrientedOverlayRenderer.OverlayFace.FRONT;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID, value = {Side.CLIENT})
public class SusyTextures {
    public static SimpleSidedCubeRenderer WOODEN_COAGULATION_TANK_WALL;
    public static OrientedOverlayRenderer VULCANIZING_PRESS_OVERLAY;
    public static OrientedOverlayRenderer LATEX_COLLECTOR_OVERLAY;
    public static OrientedOverlayRenderer ROASTER_OVERLAY;
    public static OrientedOverlayRenderer MIXER_OVERLAY_STEAM;
    public static OrientedOverlayRenderer CONTINUOUS_STIRRED_TANK_REACTOR_OVERLAY;
    public static OrientedOverlayRenderer FIXED_BED_REACTOR_OVERLAY;
    public static OrientedOverlayRenderer TRICKLE_BED_REACTOR_OVERLAY;
    public static OrientedOverlayRenderer CRYSTALLIZER_OVERLAY;
    public static OrientedOverlayRenderer BUBBLE_COLUMN_REACTOR_OVERLAY;
    public static OrientedOverlayRenderer DRYER_OVERLAY;
    public static SteamTexture INT_CIRCUIT_OVERLAY;

    public static OrientedOverlayRenderer CATALYTIC_REFORMER_OVERLAY;

    public SusyTextures(){
    }

    public static void preInit(){
        WOODEN_COAGULATION_TANK_WALL = new SimpleSidedCubeRenderer("casings/wooden_coagulation_tank_wall");
        VULCANIZING_PRESS_OVERLAY = new OrientedOverlayRenderer("machines/vulcanizing_press",new OrientedOverlayRenderer.OverlayFace[]{FRONT, OrientedOverlayRenderer.OverlayFace.SIDE, OrientedOverlayRenderer.OverlayFace.TOP});
        LATEX_COLLECTOR_OVERLAY = new OrientedOverlayRenderer("machines/latex_collector");
        ROASTER_OVERLAY = new OrientedOverlayRenderer("machines/roaster");
        MIXER_OVERLAY_STEAM = new OrientedOverlayRenderer("machines/mixer_steam");
        CONTINUOUS_STIRRED_TANK_REACTOR_OVERLAY = new OrientedOverlayRenderer("machines/continuous_stirred_tank_reactor");
        FIXED_BED_REACTOR_OVERLAY = new OrientedOverlayRenderer("machines/fixed_bed_reactor");
        TRICKLE_BED_REACTOR_OVERLAY = new OrientedOverlayRenderer("machines/trickle_bed_reactor");
        CRYSTALLIZER_OVERLAY = new OrientedOverlayRenderer("machines/crystallizer");
        BUBBLE_COLUMN_REACTOR_OVERLAY = new OrientedOverlayRenderer("machines/bubble_column_reactor");
        DRYER_OVERLAY = new OrientedOverlayRenderer("machines/dryer");
        INT_CIRCUIT_OVERLAY = SteamTexture.fullImage("textures/gui/progress_bar/int_circuit_overlay_%s.png");
        CATALYTIC_REFORMER_OVERLAY = new OrientedOverlayRenderer("multiblock/catalytic_reformer", FRONT);
    }
}
