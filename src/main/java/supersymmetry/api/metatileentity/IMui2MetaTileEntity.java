package supersymmetry.api.metatileentity;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.gui.SusyGuiTextures;

@Deprecated
@ApiStatus.ScheduledForRemoval(inVersion = "Next CEu update")
public interface IMui2MetaTileEntity extends IGuiHolder<PosGuiData> {

    @NotNull
    static UITexture getLogo() {
        return GTValues.XMAS.get() ? SusyGuiTextures.GREGTECH_LOGO_XMAS : SusyGuiTextures.GREGTECH_LOGO;
    }

    static ModularPanel createPanel(String name, int width, int height) {
        return ModularPanel.defaultPanel(name, width, height);
    }

    static ModularPanel createPanel(MetaTileEntity mte, int width, int height) {
        return createPanel(mte.metaTileEntityId.getPath(), width, height);
    }

    static ModularPanel defaultPanel(MetaTileEntity mte) {
        return createPanel(mte, 176, 186);
    }

    static ModularPanel createPopupPanel(String name, int width, int height) {
        return createPopupPanel(name, width, height, false, false);
    }

    static ModularPanel createPopupPanel(String name, int width, int height, boolean disableBelow,
                                         boolean closeOnOutsideClick) {
        return new PopupPanel(name, width, height, disableBelow, closeOnOutsideClick);
    }

    @Override
    ModularPanel buildUI(PosGuiData guiData, PanelSyncManager syncManager, UISettings settings);

    boolean useMui2();

    class PopupPanel extends ModularPanel {

        private final boolean disableBelow;
        private final boolean closeOnOutsideClick;

        public PopupPanel(@NotNull String name, int width, int height, boolean disableBelow,
                          boolean closeOnOutsideClick) {
            super(name);
            size(width, height).align(Alignment.Center);
            background(SusyGuiTextures.BACKGROUND_POPUP);
            child(ButtonWidget.panelCloseButton().top(5).right(5)
                    .onMousePressed(mouseButton -> {
                        if (mouseButton == 0 || mouseButton == 1) {
                            this.closeIfOpen(true);
                            return true;
                        }
                        return false;
                    }));
            this.disableBelow = disableBelow;
            this.closeOnOutsideClick = closeOnOutsideClick;
        }

        @Override
        public boolean disablePanelsBelow() {
            return disableBelow;
        }

        @Override
        public boolean closeOnOutOfBoundsClick() {
            return closeOnOutsideClick;
        }
    }
}
