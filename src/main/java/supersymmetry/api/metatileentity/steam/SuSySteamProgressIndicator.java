package supersymmetry.api.metatileentity.steam;

import gregtech.api.gui.resources.SteamTexture;
import gregtech.api.gui.widgets.ProgressWidget;

public class SuSySteamProgressIndicator {

    public SteamTexture progressBarTexture;
    public ProgressWidget.MoveType progressMoveType;
    public int width, height;

    public SuSySteamProgressIndicator(SteamTexture progressBarTexture, ProgressWidget.MoveType progressMoveType,
                                      int width, int height) {
        this.progressBarTexture = progressBarTexture;
        this.progressMoveType = progressMoveType;
        this.width = width;
        this.height = height;
    }
}
