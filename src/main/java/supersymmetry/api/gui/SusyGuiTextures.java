package supersymmetry.api.gui;

import static com.cleanroommc.modularui.drawable.UITexture.fullImage;

import org.jetbrains.annotations.ApiStatus;

import com.cleanroommc.modularui.drawable.UITexture;

import gregtech.api.GTValues;
import gregtech.api.gui.resources.SteamTexture;
import gregtech.api.gui.resources.TextureArea;

public class SusyGuiTextures {

    public static final SteamTexture PROGRESS_BAR_MIXER_STEAM = SteamTexture
            .fullImage("textures/gui/progress_bar/progress_bar_mixer_%s.png");
    public static final SteamTexture PROGRESS_BAR_EXTRACTION_STEAM = SteamTexture
            .fullImage("textures/gui/progress_bar/progress_bar_extraction_%s.png");
    public static final SteamTexture FLUID_SLOT_STEAM = SteamTexture.fullImage("textures/gui/base/fluid_slot_%s.png");
    public static final SteamTexture MOLD_OVERLAY_STEAM = SteamTexture
            .fullImage("textures/gui/overlay/mold_overlay_%s.png");
    public static final SteamTexture INT_CIRCUIT_OVERLAY_STEAM = SteamTexture
            .fullImage("textures/gui/progress_bar/int_circuit_overlay_%s.png");
    public static final SteamTexture BUTTON_INT_CIRCUIT_PLUS_STEAM = SteamTexture
            .fullImage("textures/gui/widget/button_circuit_plus_%s.png");
    public static final SteamTexture BUTTON_INT_CIRCUIT_MINUS_STEAM = SteamTexture
            .fullImage("textures/gui/widget/button_circuit_minus_%s.png");

    public static final TextureArea PROGRESS_BAR_EXTRACTION = TextureArea
            .fullImage("textures/gui/progress_bar/progress_bar_extraction.png");
    public static final TextureArea CATALYST_BED_OVERLAY = TextureArea
            .fullImage("textures/gui/overlay/catalyst_bed_overlay.png");
    public static final TextureArea CATALYST_PELLET_OVERLAY = TextureArea
            .fullImage("textures/gui/overlay/catalyst_pellet_overlay.png");
    public static final TextureArea ELECTRODE_OVERLAY = TextureArea
            .fullImage("textures/gui/overlay/electrode_overlay.png");
    public static final TextureArea ELECTROMAGNETIC_SEPARATOR_FLUID_OVERLAY = TextureArea
            .fullImage("textures/gui/overlay/electromagnetic_separator_fluid_overlay.png");
    public static final TextureArea ELECTROMAGNETIC_SEPARATOR_ITEM_OVERLAY = TextureArea
            .fullImage("textures/gui/overlay/electromagnetic_separator_item_overlay.png");
    public static final TextureArea LARGE_REACTOR_FLUID_OVERLAY = TextureArea
            .fullImage("textures/gui/overlay/large_reactor_fluid_overlay.png");
    public static final TextureArea LARGE_REACTOR_ITEM_OVERLAY = TextureArea
            .fullImage("textures/gui/overlay/large_reactor_item_overlay.png");
    public static final TextureArea SIFTER_FLUID_OVERLAY = TextureArea
            .fullImage("textures/gui/overlay/sifter_fluid_overlay.png");
    public static final TextureArea SIFTER_ITEM_INPUT_OVERLAY = TextureArea
            .fullImage("textures/gui/overlay/sifter_item_input_overlay.png");
    public static final TextureArea SIFTER_ITEM_OUTPUT_OVERLAY = TextureArea
            .fullImage("textures/gui/overlay/sifter_item_output_overlay.png");
    public static final TextureArea CUBIC_LATTICE_OVERLAY = TextureArea
            .fullImage("textures/gui/overlay/cubic_lattice_overlay.png");
    public static final TextureArea ORE_CHUNK_OVERLAY = TextureArea
            .fullImage("textures/gui/overlay/ore_chunk_overlay.png");
    public static final TextureArea BUTTON_INT_CIRCUIT_PLUS_PRIMITIVE = TextureArea
            .fullImage("textures/gui/widget/button_circuit_plus_primitive.png");
    public static final TextureArea BUTTON_INT_CIRCUIT_MINUS_PRIMITIVE = TextureArea
            .fullImage("textures/gui/widget/button_circuit_minus_primitive.png");
    public static final TextureArea FLUID_SLOT_PRIMITIVE = TextureArea
            .fullImage("textures/gui/base/fluid_slot_primitive.png");
    public static final TextureArea BUTTON_QUARRY_MODES = TextureArea
            .fullImage("textures/gui/widget/button_quarry_modes.png");
    public static final TextureArea ARROW = TextureArea.fullImage("textures/gui/widget/icon_indicator_arrow.png");
    public static final TextureArea BUTTON_ENERGY_VOIDING = TextureArea
            .fullImage("textures/gui/widget/button_energy_voiding.png");
    public static final TextureArea BLUEPRINT_ASSEMBLER_SLIDER_BACKGROUND = TextureArea
            .fullImage("textures/gui/widget/slider_background_dark.png");
    public static final TextureArea BLUEPRINT_ASSEMBLER_SLIDER = TextureArea
            .fullImage("textures/gui/widget/slider_dark.png");
    public static final TextureArea BLUEPRINT_ASSEMBLER_BUTTON_LEFT = TextureArea
            .fullImage("textures/gui/widget/button_left_dark.png");
    public static final TextureArea BLUEPRINT_ASSEMBLER_BUTTON_RIGHT = TextureArea
            .fullImage("textures/gui/widget/button_right_dark.png");
    public static final TextureArea BLUEPRINT_ASSEMBLER_BUTTON_SHORTVIEW = TextureArea
            .fullImage("textures/gui/widget/circle_green.png");
    public static final TextureArea ROCKET_ASSEMBLER_BUTTON_STOP = TextureArea
            .fullImage("textures/gui/widget/hazard.png");
    public static final TextureArea ROCKET_ASSEMBLER_BUTTON_START = TextureArea
            .fullImage("textures/gui/widget/suspicious_button.png");

    public static final TextureArea GREEN_CIRCLE = TextureArea.fullImage("textures/gui/widget/green.png");
    public static final TextureArea RED_CIRCLE = TextureArea.fullImage("textures/gui/widget/red.png");
    public static final TextureArea RED_X = TextureArea.fullImage("textures/gui/widget/x.png");

    public static final UITexture BACKGROUND_POPUP = UITexture.builder()
            .location(GTValues.MODID, "textures/gui/base/background_popup.png")
            .imageSize(195, 136)
            .adaptable(4)
            .name("gregtech_cover_bg")
            .canApplyTheme()
            .build();

    public static final UITexture BUTTON_POWER = UITexture.builder()
            .location(GTValues.MODID, "textures/gui/widget/button_power_2.png")
            .imageSize(18, 36)
            .build();

    public static final UITexture GREGTECH_LOGO = fullImage(GTValues.MODID, "textures/gui/icon/gregtech_logo.png");
    public static final UITexture GREGTECH_LOGO_XMAS = fullImage(GTValues.MODID,
            "textures/gui/icon/gregtech_logo_xmas.png");
    public static final UITexture OREDICT_ERROR = fullImage(GTValues.MODID, "textures/gui/widget/ore_filter/error.png");
    public static final UITexture OREDICT_INFO = fullImage(GTValues.MODID, "textures/gui/widget/ore_filter/info.png");
    public static final UITexture OREDICT_SUCCESS = fullImage(GTValues.MODID,
            "textures/gui/widget/ore_filter/success.png");
    public static final UITexture OREDICT_WAITING = fullImage(GTValues.MODID,
            "textures/gui/widget/ore_filter/waiting.png");
    public static final UITexture BUTTON_RENDER_AREA = fullImage(GTValues.MODID,
            "textures/gui/widget/button_render_area.png");
    public static final UITexture BUTTON_SETTINGS = fullImage(GTValues.MODID,
            "textures/gui/widget/button_settings.png");
    public static final UITexture BUTTON_STOCK_FILTER = fullImage(GTValues.MODID,
            "textures/gui/widget/button_stock_filter.png");
    public static final UITexture BRAKE_ACTIVE = fullImage(GTValues.MODID, "textures/gui/widget/icon_brake_active.png");
    public static final UITexture BRAKE_INACTIVE = fullImage(GTValues.MODID,
            "textures/gui/widget/icon_brake_inactive.png");
    public static final UITexture THROTTLE_ACTIVE = fullImage(GTValues.MODID,
            "textures/gui/widget/icon_throttle_active.png");
    public static final UITexture THROTTLE_INACTIVE = fullImage(GTValues.MODID,
            "textures/gui/widget/icon_throttle_inactive.png");

    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "GTCEu 2.9")
    public static final UITexture SLOT = new UITexture.Builder()
            .location(GTValues.MODID, "textures/gui/base/slot.png")
            .imageSize(18, 18)
            .adaptable(1)
            .name("standard_slot")
            .canApplyTheme()
            .build();

    public static final UITexture FLUID_SLOT = new UITexture.Builder()
            .location(GTValues.MODID, "textures/gui/base/fluid_slot.png")
            .imageSize(18, 18)
            .adaptable(1)
            .name("standard_fluid_slot")
            .canApplyTheme()
            .build();

    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "GTCEu 2.9")
    public static final UITexture ICON_RIGHT = UITexture.builder()
            .location(GTValues.MODID, "textures/gui/terminal/icon/right_hover.png")
            .name("right_button")
            .canApplyTheme()
            .build();

    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "GTCEu 2.9")
    public static final UITexture ICON_LEFT = UITexture.builder()
            .location(GTValues.MODID, "textures/gui/terminal/icon/left_hover.png")
            .name("left_button")
            .canApplyTheme()
            .build();
}
