package supersymmetry.api.metatileentity.steam;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.widgets.ProgressWidget;
import supersymmetry.api.gui.SusyGuiTextures;

public class SuSySteamProgressIndicators {

    public static final SuSySteamProgressIndicator COMPRESS = new SuSySteamProgressIndicator(GuiTextures.PROGRESS_BAR_COMPRESS_STEAM, ProgressWidget.MoveType.HORIZONTAL, 20, 15);
    public static final SuSySteamProgressIndicator ARROW = new SuSySteamProgressIndicator(GuiTextures.PROGRESS_BAR_ARROW_STEAM, ProgressWidget.MoveType.HORIZONTAL, 20, 15);
    public static final SuSySteamProgressIndicator MIXER = new SuSySteamProgressIndicator(SusyGuiTextures.PROGRESS_BAR_MIXER_STEAM, ProgressWidget.MoveType.CIRCULAR, 20, 20);


}
