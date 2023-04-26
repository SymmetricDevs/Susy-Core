package supersymmetry.common.covers;

import com.google.common.math.IntMath;
import gregtech.api.GTValues;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import gregtech.common.covers.CoverPump;
import gregtech.common.covers.ManualImportExportMode;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;

public class SteamCoverPump extends CoverPump {
    public SteamCoverPump(ICoverable coverHolder, EnumFacing attachedSide, int mbPerTick) {
        super(coverHolder, attachedSide, 0, mbPerTick);
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {
        WidgetGroup primaryGroup = new WidgetGroup();
        primaryGroup.addWidget(new LabelWidget(10, 5, this.getUITitle(), "Steam"));
        primaryGroup.addWidget(new ImageWidget(44, 20, 62, 20, GuiTextures.DISPLAY));
        primaryGroup.addWidget((new IncrementButtonWidget(136, 20, 30, 20, 1, 10, 100, 1000, this::adjustTransferRate)).setDefaultTooltip().setShouldClientCallback(false));
        primaryGroup.addWidget((new IncrementButtonWidget(10, 20, 34, 20, -1, -10, -100, -1000, this::adjustTransferRate)).setDefaultTooltip().setShouldClientCallback(false));
        TextFieldWidget2 textField = (new TextFieldWidget2(45, 26, 60, 20, () -> {
            return this.bucketMode == CoverPump.BucketMode.BUCKET ? Integer.toString(this.transferRate / 1000) : Integer.toString(this.transferRate);
        }, (val) -> {
            if (val != null && !val.isEmpty()) {
                int amount = Integer.parseInt(val);
                if (this.bucketMode == CoverPump.BucketMode.BUCKET) {
                    amount = IntMath.saturatedMultiply(amount, 1000);
                }

                this.setTransferRate(amount);
            }

        })).setCentered(true).setNumbersOnly(1, this.bucketMode == CoverPump.BucketMode.BUCKET ? this.maxFluidTransferRate / 1000 : this.maxFluidTransferRate).setMaxLength(8);
        primaryGroup.addWidget(textField);
        primaryGroup.addWidget(new CycleButtonWidget(106, 20, 30, 20, BucketMode.class, this::getBucketMode, (mode) -> {
            if (mode != this.bucketMode) {
                this.setBucketMode(mode);
            }

        }));
        primaryGroup.addWidget(new CycleButtonWidget(10, 43, 75, 18, PumpMode.class, this::getPumpMode, this::setPumpMode));
        primaryGroup.addWidget((new CycleButtonWidget(7, 160, 116, 20, ManualImportExportMode.class, this::getManualImportExportMode, this::setManualImportExportMode)).setTooltipHoverString("cover.universal.manual_import_export.mode.description"));
        this.fluidFilter.initUI(88, primaryGroup::addWidget);
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176, 266).widget(primaryGroup).bindPlayerInventory(player.inventory, GuiTextures.SLOT, 7, 184);
        return this.buildUI(builder, player);
    }

    @Override
    protected TextureAtlasSprite getPlateSprite() {
        return Textures.STEAM_CASING_BRONZE.getSpriteOnSide(SimpleSidedCubeRenderer.RenderSide.SIDE);
    }
}
