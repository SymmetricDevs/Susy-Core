package supersymmetry.common.metatileentities.single.railinterfaces;

import cam72cam.immersiverailroading.entity.EntityRollingStock;
import cam72cam.immersiverailroading.entity.Locomotive;
import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.DoubleSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.SliderWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Flow;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.CycleButtonWidget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.gui.SusyGuiTextures;
import supersymmetry.api.gui.widgets.VerticalSliderWidget;
import supersymmetry.api.metatileentity.Mui2MetaTileEntity;
import supersymmetry.api.stockinteraction.StockHelperFunctions;
import supersymmetry.client.renderer.textures.SusyTextures;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

public class MetaTileEntityLocomotiveController extends MetaTileEntityStockInteractor {

    AxisAlignedBB interactionBoundingBox;

    public boolean active;
    //#fix# may need to stockFilter types of locomotives later on
    public static final int filterIndex = 1;

    //control settings
    protected boolean controlActive = true;
    protected float activeBrake = 0;
    protected float activeThrottle = 0;
    protected boolean controlInactive = true;
    protected float inactiveBrake = 0;
    protected float inactiveThrottle = 0;

    public MetaTileEntityLocomotiveController(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SusyTextures.STOCK_CONTROLLER);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityLocomotiveController(this.metaTileEntityId);
    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager syncManager) {

        PanelSyncHandler panel = (PanelSyncHandler) syncManager.panel("controller_panel",
                (panelSyncManager, syncHandler) -> createPopupPanel(panelSyncManager),
                true);

        ModularPanel mainPanel = super.buildUI(guiData, syncManager);
        mainPanel.height(186);
        Flow flow = (Flow) mainPanel.getChildren().get(4);
        flow.child(Flow.row().coverChildrenHeight()
                .marginBottom(2).widthRel(1f)
                .child(new ButtonWidget<>()
                        .overlay(SusyGuiTextures.FILTER_SETTINGS_OVERLAY.asIcon().size(16))
                        .onMousePressed(mouseButton -> {
                            if (!panel.isPanelOpen()) {
                                panel.openPanel();
                            } else {
                                panel.closePanel();
                            }
                            return true;
                        })
                )
                .child(IKey.lang("Controller Settings").asWidget()
                        .align(Alignment.CenterRight).height(18))
        );
        return mainPanel;
    }

    public ModularPanel createPopupPanel(PanelSyncManager syncManager) {

        BooleanSyncValue controlActive = new BooleanSyncValue(() -> this.controlActive, x -> this.controlActive = x);
        BooleanSyncValue controlInactive = new BooleanSyncValue(() -> this.controlInactive, x -> this.controlInactive = x);

        DoubleSyncValue activeBrake = new DoubleSyncValue(() -> this.activeBrake, x -> this.activeBrake = (float) x);
        DoubleSyncValue activeThrottle = new DoubleSyncValue(() -> this.activeThrottle, x -> this.activeThrottle = (float) x);
        DoubleSyncValue inactiveBrake = new DoubleSyncValue(() -> this.inactiveBrake, x -> this.inactiveBrake = (float) x);
        DoubleSyncValue inactiveThrottle = new DoubleSyncValue(() -> this.inactiveThrottle, x -> this.inactiveThrottle = (float) x);

        return Mui2MetaTileEntity.createPopupPanel("controller_settings", 128, 150)
                .padding(4)
                .coverChildrenWidth()
                .child(IKey.str("Settings").asWidget()
                        .pos(5, 5))
                .child(Flow.row()
                        .top(18)
                        .coverChildren()
                        .child(Flow.column()
                                .coverChildren()
                                .child(new ToggleButton()
                                        .value(controlActive)
                                        .invertSelected(true)
                                        .size(24, 8)
                                        .marginBottom(2))
                                .child(Flow.row()
                                        .setEnabledIf(widget -> controlActive.getValue())
                                        .coverChildrenWidth()
                                        .height(116)
                                        .child(createSliderColumn("active_brake", SusyGuiTextures.BRAKE_ACTIVE, activeBrake, 0, 1))
                                        .child(createSliderColumn("active_throttle", SusyGuiTextures.THROTTLE_ACTIVE, activeThrottle, -1, 1))))
                        .child(new Rectangle().setColor(0xFF555555).asWidget()
                                .width(1)
                                .height(124)
                                .margin(4, 0))
                        .child(Flow.column()
                                .coverChildrenWidth()
                                .coverChildrenHeight()
                                .child(new ToggleButton()
                                        .value(controlInactive)
                                        .invertSelected(true)
                                        .size(24, 8)
                                        .marginBottom(2))
                                .child(Flow.row()
                                        .setEnabledIf(widget -> controlInactive.getValue())
                                        .coverChildrenWidth()
                                        .height(116)
                                        .child(createSliderColumn("inactive_brake", SusyGuiTextures.BRAKE_INACTIVE, inactiveBrake, 0, 1))
                                        .child(createSliderColumn("inactive_throttle", SusyGuiTextures.THROTTLE_INACTIVE, inactiveThrottle, -1, 1)))));
    }

    @SuppressWarnings("SameParameterValue")
    @ParametersAreNonnullByDefault
    protected Widget<?> createSliderColumn(String name, UITexture texture, DoubleSyncValue value, double min, double max) {
        return Flow.column()
                .width(16)
                .child(texture.asWidget()
                        .size(12, 12)
                        .marginBottom(2))
                .child(new SliderWidget()
                        .background(GuiTextures.SLOT_FLUID)
                        .setAxis(GuiAxis.Y)
                        .size(8, 100)
                        .sliderSize(8, 6)
                        .value(value)
                        .bounds(min, max)
                        .tooltipAutoUpdate(true) // TODO: ↓ this might be kinda buggy idk.
                        .tooltipDynamic(tooltip -> tooltip.add(String.format("%.2f", value.getValue()))));
    }

    public void writeStatsToBuffer(PacketBuffer buf) {

        buf.writeFloat(this.activeBrake);
        buf.writeFloat(this.activeThrottle);
        buf.writeFloat(this.inactiveBrake);
        buf.writeFloat(this.inactiveThrottle);
    }

    public void readStatsFromBuffer(PacketBuffer buf) {
        this.activeBrake = buf.readFloat();
        this.activeThrottle = buf.readFloat();
        this.inactiveBrake = buf.readFloat();
        this.inactiveThrottle = buf.readFloat();
    }


    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(this.active);
        this.writeStatsToBuffer(buf);
    }

    @Override
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.active = buf.readBoolean();
        this.readStatsFromBuffer(buf);
        this.scheduleRenderUpdate();
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if((dataId & 0b1) > 0) {
            this.active = buf.readBoolean();
        }
        if((dataId & 0b10) > 0) {
            this.readStatsFromBuffer(buf);
        }
    }

    @Override
    public void update() {
        super.update();

        if(this.getWorld().isRemote)
            return;

        if (getOffsetTimer() % 20 == 0)
        {
            this.onNeighborChanged();

            List<EntityRollingStock> stocks = StockHelperFunctions.getStocksInArea(this.getWorld(), this.getInteractionBoundingBox());

            if (stocks.isEmpty())
                return;

            if(!(stocks.get(0) instanceof Locomotive loco))
                return;

            this.active = this.isBlockRedstonePowered();
            this.writeCustomData(0b1, buf -> buf.writeBoolean(this.active));

            if (this.active && this.activeBrake >= 0) {
                loco.setTrainBrake(this.activeBrake);
                loco.setThrottle(this.activeThrottle);
            } else if (!this.active && this.inactiveBrake >= 0) {
                loco.setTrainBrake(this.inactiveBrake);
                loco.setThrottle(this.inactiveThrottle);
            }
        }
    }

    @Override
    protected <T> T getStockCapability(Capability<T> capability, EnumFacing side) {
        return null;
    }

    @Override
    public void onNeighborChanged() {
        if(this.getWorld().isRemote)
            return;
        this.updateInputRedstoneSignals();
        this.active = this.isBlockRedstonePowered();
        this.writeCustomData(0b1, (buf) -> buf.writeBoolean(this.active));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("susy.stock_interfaces.locomotive_controller.description"));
        tooltip.add(I18n.format("susy.stock_interfaces.right_click_for_gui"));
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {

        int w = 192;
        int h = 256;
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        int fontHeight = fontRenderer.FONT_HEIGHT;
        int buffer = 8;

        List<VerticalSliderWidget> leftWidgets = new ArrayList<>();
        List<VerticalSliderWidget> rightWidgets = new ArrayList<>();

        int r1y = buffer;
        int r2y = r1y; // = r1y + fontHeight + buffer * 2;
        int r3y = r2y + fontHeight + buffer;
        int r3h = 25;

        int rsw = 16;

        //row 1
        LabelWidget header = new LabelWidget(w / 2, r1y, I18n.format("gregtech.machine.stock_controller.name")); //getMetaFullName()) #fix# add translations
        header.setXCentered(true);

        int bufBar = buffer + rsw;
        int midB = bufBar + buffer / 2;
        int trw = fontRenderer.getStringWidth("unpowered (off)") + 8;
        int btnMid = midB - (trw / 2);

        int r4y = r3y + buffer + 32;

        LabelWidget infoLabel = new LabelWidget(w / 2, r4y - 12, "  break | throttle");
        infoLabel.setXCentered(true);

        CycleButtonWidget leftToggleWidget = new CycleButtonWidget(btnMid + 22, r3y, trw, r3h, new String[]{"powered (off)", "powered (on)"}, () -> (this.activeBrake < 0f) ? 0 : 1,
                (x) -> {
                    if(x == 0) {
                        this.activeBrake = -1.0f;
                        this.activeThrottle = 0;
                        leftWidgets.forEach((l) -> l.setVisible(false));
                    }
                    else {
                        this.activeBrake = 0.0f;
                        this.activeThrottle = 0.0f;
                        leftWidgets.forEach((l) -> l.setVisible(true));
                        leftWidgets.get(0).setSliderValue(1.0f - this.activeBrake);
                        leftWidgets.get(1).setSliderValue((1.0f - this.activeThrottle) / 2.0f);
                    }
                });

        CycleButtonWidget rightToggleWidget = new CycleButtonWidget(w - (btnMid + trw + 22), r3y, trw, r3h, new String[]{"unpowered (off)", "unpowered (on)"}, () -> (this.inactiveBrake < 0f) ? 0 : 1,
                (x) -> {
                    if(x == 0) {
                        this.inactiveBrake = -1.0f;
                        this.inactiveThrottle = 0.0f;
                        rightWidgets.forEach((l) -> l.setVisible(false));
                    }
                    else {
                        this.inactiveBrake = 0.0f;
                        this.inactiveThrottle = 0.0f;
                        rightWidgets.forEach((l) -> l.setVisible(true));
                        rightWidgets.get(0).setSliderValue(1.0f - this.inactiveBrake);
                        rightWidgets.get(1).setSliderValue((1.0f - this.inactiveThrottle) / 2.0f);
                    }
                });

        int ySpaceLeft = h - buffer - r4y - buffer;
        VerticalSliderWidget leftBreakSlide = new VerticalSliderWidget("leftBreak", buffer, r4y, rsw, ySpaceLeft, 0.0f, 1.0f, 1.0f - this.activeBrake, (x) -> this.activeBrake = 1.0f - x);
        leftWidgets.add(leftBreakSlide);
        VerticalSliderWidget leftThrottleSide = new VerticalSliderWidget("leftThrottle", buffer + bufBar + buffer, r4y, rsw, ySpaceLeft, 0.0f, 1.0f, (1.0f - this.activeThrottle) / 2.0f, (x) -> this.activeThrottle = 1.0f - 2 * x);
        leftWidgets.add(leftThrottleSide);

        VerticalSliderWidget rightBreakSlide = new VerticalSliderWidget("rightBreak", w - (buffer + bufBar + buffer + rsw), r4y, rsw, ySpaceLeft, 0.0f, 1.0f, 1.0f - this.inactiveBrake, (x) -> this.inactiveBrake = 1.0f - x);
        rightWidgets.add(rightBreakSlide);
        VerticalSliderWidget rightThrottleSide = new VerticalSliderWidget("rightThrottle", w - (buffer + rsw), r4y, rsw, ySpaceLeft, 0.0f, 1.0f, (1.0f - this.inactiveThrottle) / 2.0f, (x) -> this.inactiveThrottle = 1.0f  - 2 * x);
        rightWidgets.add(rightThrottleSide);

        int sy = r4y;
        int ey = h - buffer - fontHeight - buffer - buffer;
        int hy = (ey + sy) / 2;
        LabelWidget oneLabel = new LabelWidget(w / 2, sy, "1.0");
        oneLabel.setXCentered(true);
        LabelWidget halfLabel = new LabelWidget(w / 2, hy, "0.5");
        halfLabel.setXCentered(true);
        LabelWidget zeroLabel = new LabelWidget(w / 2, ey, "0.0");
        zeroLabel.setXCentered(true);


        ModularUI.Builder builder = ModularUI.builder(SusyGuiTextures.VERTICAL_SLIDER_BACKGROUND, w, h)
                .widget(header)

                .widget(infoLabel)

                .widget(leftToggleWidget)
                .widget(leftBreakSlide)
                .widget(leftThrottleSide)

                .widget(rightToggleWidget)
                .widget(rightBreakSlide)
                .widget(rightThrottleSide)

                .widget(oneLabel)
                .widget(halfLabel)
                .widget(zeroLabel)
                ;

        ModularUI modularUI =  builder.build(getHolder(), entityPlayer);
        modularUI.holder.markAsDirty();

        return modularUI;
    }

    //#fix# does detected need to be saved or just refreshed on load? does ticks-alive need to be saved to prevent every one ticking at once?
    //update system based on chunk and global time instead of ticks alive?
    //should detection area be changeable and saved?
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("active", this.active);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.active = data.getBoolean("active");
    }

    @Override
    public boolean useMui() {
        return getPos().getY() % 2 == 0; // For testing purposes
    }


    @Override
    protected boolean canMachineConnectRedstone(EnumFacing side) {
        return true;
    }

    @Override
    public AxisAlignedBB getInteractionBoundingBox() {
        return interactionBoundingBox == null ? interactionBoundingBox = StockHelperFunctions.getBox(this.getPos(), this.getFrontFacing(), 8, 8) : interactionBoundingBox;
    }
}
