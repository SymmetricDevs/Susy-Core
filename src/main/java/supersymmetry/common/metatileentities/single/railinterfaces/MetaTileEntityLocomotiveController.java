package supersymmetry.common.metatileentities.single.railinterfaces;

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
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.gui.SusyGuiTextures;
import supersymmetry.api.metatileentity.Mui2MetaTileEntity;
import supersymmetry.client.renderer.textures.SusyTextures;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

public class MetaTileEntityLocomotiveController extends MetaTileEntityStockInteractor {

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

    public void performControl() {
        if (stock != null && stock instanceof Locomotive loco) {
            boolean redstonePowered = isBlockRedstonePowered();
            if (controlActive && redstonePowered) {
                loco.setTrainBrake(this.activeBrake);
                loco.setThrottle(this.activeThrottle);
            } else if (controlInactive && !redstonePowered) {
                loco.setTrainBrake(this.inactiveBrake);
                loco.setThrottle(this.inactiveThrottle);
            }
        }
    }

    @Override
    public void update() {
        super.update();
        if (!this.getWorld().isRemote && getOffsetTimer() % 20 == 0) {
            performControl();
        }
    }

    @Override
    protected <T> T getStockCapability(Capability<T> capability, EnumFacing side) {
        return null; // This is not a delegator
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("susy.stock_interfaces.locomotive_controller.description"));
        tooltip.add(I18n.format("susy.stock_interfaces.right_click_for_gui"));
    }

    //#fix# does detected need to be saved or just refreshed on load? does ticks-alive need to be saved to prevent every one ticking at once?
    //update system based on chunk and global time instead of ticks alive?
    //should detection area be changeable and saved?
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("controlActive", controlActive);
        data.setDouble("activeBrake", activeBrake);
        data.setDouble("activeThrottle", activeThrottle);
        data.setBoolean("controlInactive", controlInactive);
        data.setDouble("inactiveBrake", inactiveBrake);
        data.setDouble("inactiveThrottle", inactiveThrottle);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.controlActive = data.getBoolean("controlActive");
        this.activeBrake = data.getFloat("activeBrake");
        this.activeThrottle = data.getFloat("activeThrottle");
        this.controlInactive = data.getBoolean("controlInactive");
        this.inactiveBrake = data.getFloat("inactiveBrake");
        this.inactiveThrottle = data.getFloat("inactiveThrottle");
    }

    @Override
    protected boolean canMachineConnectRedstone(EnumFacing side) {
        return true;
    }
}
