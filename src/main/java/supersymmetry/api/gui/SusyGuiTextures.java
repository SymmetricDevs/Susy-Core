package supersymmetry.api.gui;

import com.cleanroommc.modularui.drawable.UITexture;
import gregtech.api.GTValues;
import gregtech.api.gui.resources.SteamTexture;
import gregtech.api.gui.resources.TextureArea;

import static com.cleanroommc.modularui.drawable.UITexture.fullImage;

public class SusyGuiTextures {
    public static final SteamTexture PROGRESS_BAR_MIXER_STEAM = SteamTexture.fullImage("textures/gui/progress_bar/progress_bar_mixer_%s.png");
    public static final SteamTexture PROGRESS_BAR_EXTRACTION_STEAM = SteamTexture.fullImage("textures/gui/progress_bar/progress_bar_extraction_%s.png");
    public static final SteamTexture FLUID_SLOT_STEAM = SteamTexture.fullImage("textures/gui/base/fluid_slot_%s.png");
    public static final SteamTexture MOLD_OVERLAY_STEAM = SteamTexture.fullImage("textures/gui/overlay/mold_overlay_%s.png");
    public static final SteamTexture INT_CIRCUIT_OVERLAY_STEAM = SteamTexture.fullImage("textures/gui/progress_bar/int_circuit_overlay_%s.png");
    public static final SteamTexture BUTTON_INT_CIRCUIT_PLUS_STEAM = SteamTexture.fullImage("textures/gui/widget/button_circuit_plus_%s.png");
    public static final SteamTexture BUTTON_INT_CIRCUIT_MINUS_STEAM = SteamTexture.fullImage("textures/gui/widget/button_circuit_minus_%s.png");

    public static final TextureArea PROGRESS_BAR_EXTRACTION = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_extraction.png");
    public static final TextureArea CATALYST_BED_OVERLAY  = TextureArea.fullImage("textures/gui/overlay/catalyst_bed_overlay.png");
    public static final TextureArea CATALYST_PELLET_OVERLAY = TextureArea.fullImage("textures/gui/overlay/catalyst_pellet_overlay.png");
    public static final TextureArea ELECTRODE_OVERLAY = TextureArea.fullImage("textures/gui/overlay/electrode_overlay.png");
    public static final TextureArea ELECTROMAGNETIC_SEPARATOR_FLUID_OVERLAY = TextureArea.fullImage("textures/gui/overlay/electromagnetic_separator_fluid_overlay.png");
    public static final TextureArea ELECTROMAGNETIC_SEPARATOR_ITEM_OVERLAY = TextureArea.fullImage("textures/gui/overlay/electromagnetic_separator_item_overlay.png");
    public static final TextureArea LARGE_REACTOR_FLUID_OVERLAY = TextureArea.fullImage("textures/gui/overlay/large_reactor_fluid_overlay.png");
    public static final TextureArea LARGE_REACTOR_ITEM_OVERLAY = TextureArea.fullImage("textures/gui/overlay/large_reactor_item_overlay.png");
    public static final TextureArea SIFTER_FLUID_OVERLAY = TextureArea.fullImage("textures/gui/overlay/sifter_fluid_overlay.png");
    public static final TextureArea SIFTER_ITEM_INPUT_OVERLAY = TextureArea.fullImage("textures/gui/overlay/sifter_item_input_overlay.png");
    public static final TextureArea SIFTER_ITEM_OUTPUT_OVERLAY = TextureArea.fullImage("textures/gui/overlay/sifter_item_output_overlay.png");
    public static final TextureArea CUBIC_LATTICE_OVERLAY = TextureArea.fullImage("textures/gui/overlay/cubic_lattice_overlay.png");
    public static final TextureArea ORE_CHUNK_OVERLAY = TextureArea.fullImage("textures/gui/overlay/ore_chunk_overlay.png");
    public static final TextureArea VERTICAL_SLIDER_BACKGROUND = TextureArea.fullImage("textures/gui/widget/vertical_slider_background.png");
    public static final TextureArea VERTICAL_SLIDER_ICON = TextureArea.fullImage("textures/gui/widget/vertical_slider.png");
    public static final TextureArea BUTTON_INT_CIRCUIT_PLUS_PRIMITIVE = TextureArea.fullImage("textures/gui/widget/button_circuit_plus_primitive.png");
    public static final TextureArea BUTTON_INT_CIRCUIT_MINUS_PRIMITIVE = TextureArea.fullImage("textures/gui/widget/button_circuit_minus_primitive.png");
    public static final TextureArea FLUID_SLOT_PRIMITIVE = TextureArea.fullImage("textures/gui/base/fluid_slot_primitive.png");

    public static final UITexture BACKGROUND_POPUP = UITexture.builder()
            .location(GTValues.MODID, "textures/gui/base/background_popup.png")
            .imageSize(195, 136)
            .adaptable(4)
            .name("gregtech_cover_bg")
            .canApplyTheme()
            .build();

    public static final UITexture MC_BUTTON = new UITexture.Builder()
            .location("modularui", "gui/widgets/mc_button.png")
            .imageSize(16, 32)
            .uv(0.0f, 0.0f, 1.0f, 0.5f)
            .adaptable(2)
            .build();

    public static final UITexture GREGTECH_LOGO = fullImage(GTValues.MODID, "textures/gui/icon/gregtech_logo.png");
    public static final UITexture GREGTECH_LOGO_XMAS = fullImage(GTValues.MODID, "textures/gui/icon/gregtech_logo_xmas.png");
    public static final UITexture OREDICT_ERROR = fullImage(GTValues.MODID, "textures/gui/widget/ore_filter/error.png");
    public static final UITexture OREDICT_INFO = fullImage(GTValues.MODID, "textures/gui/widget/ore_filter/info.png");
    public static final UITexture OREDICT_SUCCESS = fullImage(GTValues.MODID, "textures/gui/widget/ore_filter/success.png");
    public static final UITexture OREDICT_WAITING = fullImage(GTValues.MODID, "textures/gui/widget/ore_filter/waiting.png");
    public static final UITexture RENDER_AREA_OVERLAY = fullImage(GTValues.MODID, "textures/gui/overlay/render_area_overlay.png");
    public static final UITexture FILTER_SETTINGS_OVERLAY = fullImage(GTValues.MODID, "textures/gui/overlay/filter_settings_overlay.png");
    public static final UITexture BRAKE_ACTIVE = fullImage(GTValues.MODID, "textures/gui/widget/icon_brake_active.png");
    public static final UITexture BRAKE_INACTIVE = fullImage(GTValues.MODID, "textures/gui/widget/icon_brake_inactive.png");
    public static final UITexture THROTTLE_ACTIVE = fullImage(GTValues.MODID, "textures/gui/widget/icon_throttle_active.png");
    public static final UITexture THROTTLE_INACTIVE = fullImage(GTValues.MODID, "textures/gui/widget/icon_throttle_inactive.png");
}
