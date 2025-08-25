package supersymmetry.api.metatileentity;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import gregtech.api.GTValues;
import gregtech.api.fluids.GTFluid;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supersymmetry.api.gui.SusyGuiTextures;

@Deprecated
@ApiStatus.ScheduledForRemoval(inVersion = "Next CEu update")
public class Mui2Utils {

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
        return createPanel(mte, 176, 186);
    }

    public static ModularPanel createPopupPanel(String name, int width, int height) {
        return createPopupPanel(name, width, height, false, false);
    }

    public static ModularPanel createPopupPanel(
            String name, int width, int height, boolean disableBelow, boolean closeOnOutsideClick) {
        return new PopupPanel(name, width, height, disableBelow, closeOnOutsideClick);
    }


    @NotNull
    public static IKey fluid(@Nullable Fluid fluid) {
        return fluid(fluid, null);
    }

    @NotNull
    public static IKey fluid(@Nullable Fluid fluid, @Nullable FluidStack stack) {
        if (fluid == null) return IKey.EMPTY;
        if (fluid instanceof GTFluid.GTMaterialFluid gtFluid) {
            return getLocalizedKey(gtFluid);
        }
        if (stack == null) return IKey.lang(fluid.getUnlocalizedName());
        else return IKey.lang(fluid.getUnlocalizedName(stack));
    }

    @NotNull
    public static IKey fluid(@Nullable FluidStack fluid) {
        if (fluid == null) return IKey.EMPTY;
        return fluid(fluid.getFluid(), fluid);
    }

    public static @NotNull IKey getLocalizedKey(GTFluid.GTMaterialFluid gtFluid) {
        IKey localizedName;
        String customMaterialTranslation = "fluid." + gtFluid.getMaterial().getUnlocalizedName();

        if (net.minecraft.util.text.translation.I18n.canTranslate(customMaterialTranslation)) {
            localizedName = IKey.lang(customMaterialTranslation);
        } else {
            localizedName = IKey.lang(gtFluid.getMaterial().getUnlocalizedName());
        }

        if (gtFluid.getUnlocalizedName() != null) {
            return IKey.lang(gtFluid.getUnlocalizedName(), localizedName);
        }

        return localizedName;
    }

    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "Next CEu update")
    public static class PopupPanel extends ModularPanel {

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
                            this.closeIfOpen();
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
