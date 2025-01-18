package supersymmetry.api.metatileentity;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.gui.SusyGuiTextures;

@Deprecated
@ApiStatus.ScheduledForRemoval(inVersion = "Next CEu update")
public abstract class Mui2MetaTileEntity extends MetaTileEntity implements IGuiHolder<PosGuiData> {

    public Mui2MetaTileEntity(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @NotNull
    public static UITexture getLogo() {
        return GTValues.XMAS.get() ? SusyGuiTextures.GREGTECH_LOGO_XMAS : SusyGuiTextures.GREGTECH_LOGO;
    }

    public static ModularPanel createPanel(String name, int width, int height) {
        return ModularPanel.defaultPanel(name, width, height);
    }

    public static ModularPanel createPanel(MetaTileEntity mte, int width, int height) {
        return createPanel(mte.metaTileEntityId.getPath(), width, height);
    }

    public static ModularPanel defaultPanel(MetaTileEntity mte) {
        return createPanel(mte, 176, 166);
    }

    public static ModularPanel createPopupPanel(String name, int width, int height) {
        return createPopupPanel(name, width, height, false, false);
    }

    public static ModularPanel createPopupPanel(String name, int width, int height, boolean disableBelow,
                                                boolean closeOnOutsideClick) {
        return new PopupPanel(name, width, height, disableBelow, closeOnOutsideClick);
    }

    public abstract ModularPanel buildUI(PosGuiData guiData, GuiSyncManager guiSyncManager);

    @Override
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                CuboidRayTraceResult hitResult) {
        if (!playerIn.isSneaking() && openGUIOnRightClick()) {
            if (getWorld() != null && !getWorld().isRemote) {
                MetaTileEntityGuiFactory.open(playerIn, this);
            }
            return true;
        } else {
            return super.onRightClick(playerIn, hand, facing, hitResult);
        }
    }

    private static class PopupPanel extends ModularPanel {

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
                            Interactable.playButtonClickSound();
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
