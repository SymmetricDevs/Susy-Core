package supersymmetry.common.covers;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;

import org.jetbrains.annotations.NotNull;

import com.google.common.math.IntMath;

import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverableView;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import gregtech.common.covers.CoverPump;
import gregtech.common.covers.ManualImportExportMode;

public class CoverSteamPump extends CoverPump {

    public CoverSteamPump(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                          @NotNull EnumFacing attachedSide, int mbPerTick) {
        super(definition, coverableView, attachedSide, 0, mbPerTick);
    }

    @Override
    protected @NotNull TextureAtlasSprite getPlateSprite() {
        return Textures.STEAM_CASING_BRONZE.getSpriteOnSide(SimpleSidedCubeRenderer.RenderSide.SIDE);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public ModularUI createUI(EntityPlayer player) {
        WidgetGroup primaryGroup = new WidgetGroup();
        // TODO remove this override and do the title natively in 2.9
        primaryGroup.addWidget(new LabelWidget(10, 5, getUITitle()));

        primaryGroup.addWidget(new ImageWidget(44, 20, 62, 20, GuiTextures.DISPLAY));

        primaryGroup.addWidget(new IncrementButtonWidget(136, 20, 30, 20, 1, 10, 100, 1000, this::adjustTransferRate)
                .setDefaultTooltip()
                .setShouldClientCallback(false));
        primaryGroup.addWidget(new IncrementButtonWidget(10, 20, 34, 20, -1, -10, -100, -1000, this::adjustTransferRate)
                .setDefaultTooltip()
                .setShouldClientCallback(false));

        TextFieldWidget2 textField = new TextFieldWidget2(45, 26, 60, 20, () -> bucketMode == BucketMode.BUCKET ?
                Integer.toString(transferRate / 1000) : Integer.toString(transferRate), val -> {
                    if (val != null && !val.isEmpty()) {
                        int amount = Integer.parseInt(val);
                        if (this.bucketMode == BucketMode.BUCKET) {
                            amount = IntMath.saturatedMultiply(amount, 1000);
                        }
                        setTransferRate(amount);
                    }
                })
                        .setCentered(true)
                        .setNumbersOnly(1,
                                bucketMode == BucketMode.BUCKET ? maxFluidTransferRate / 1000 : maxFluidTransferRate)
                        .setMaxLength(8);
        primaryGroup.addWidget(textField);

        primaryGroup.addWidget(new CycleButtonWidget(106, 20, 30, 20,
                BucketMode.class, this::getBucketMode, mode -> {
                    if (mode != bucketMode) {
                        setBucketMode(mode);
                    }
                }));

        primaryGroup.addWidget(new CycleButtonWidget(10, 43, 75, 18,
                PumpMode.class, this::getPumpMode, this::setPumpMode));

        primaryGroup.addWidget(new CycleButtonWidget(7, 160, 116, 20,
                ManualImportExportMode.class, this::getManualImportExportMode, this::setManualImportExportMode)
                        .setTooltipHoverString("cover.universal.manual_import_export.mode.description"));

        this.fluidFilter.initUI(88, primaryGroup::addWidget);

        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176, 184 + 82)
                .widget(primaryGroup)
                .bindPlayerInventory(player.inventory, GuiTextures.SLOT, 7, 184);
        return buildUI(builder, player);
    }

    @Override
    protected String getUITitle() {
        return "cover.pump.steam.title";
    }
}
