package supersymmetry.common.metatileentities.single.railinterfaces;

import static supersymmetry.common.entities.EntityAbstractRocket.ROCKET_CONFIG_KEY;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widgets.CycleButtonWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;

import cam72cam.immersiverailroading.entity.EntityRollingStock;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import supersymmetry.api.capability.SuSyDataCodes;
import supersymmetry.api.gui.SusyGuiTextures;
import supersymmetry.api.metatileentity.Mui2Utils;
import supersymmetry.api.stockinteraction.StockFilter;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.entities.EntityTransporterErector;
import supersymmetry.common.item.SuSyMetaItems;
import supersymmetry.common.rocketry.RocketConfiguration;

public class MetaTileEntityRocketProgrammer extends MetaTileEntityStockInteractor {

    protected IItemHandlerModifiable circuitHolder = new ItemStackHandler(1);
    protected AxisAlignedBB structureAABB;
    protected boolean canHandleFullConfig = true;

    public MetaTileEntityRocketProgrammer(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SusyTextures.ROCKET_PROGRAMMER_OVERLAY);
        this.stockFilter = new StockFilter(1) {

            @Override
            public boolean test(EntityRollingStock entityRollingStock) {
                return entityRollingStock instanceof EntityTransporterErector &&
                        ((EntityTransporterErector) entityRollingStock).isRocketLoaded();
            }
        };
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityRocketProgrammer(metaTileEntityId);
    }

    @Override
    public void updateStock() {
        super.updateStock();
        if (this.getOffsetTimer() % 10 == 0 && this.getConfig() != null) {
            EntityTransporterErector rocket = (EntityTransporterErector) this.stock;
            if (rocket != null) {
                RocketConfiguration config = new RocketConfiguration(this.getConfig());
                // Set budget to 2
                // TODO: Make the transporter erector hold rocket types for IV
                setLowTierWarning(config.setBudget(this.getWorld().provider.getDimension(), 2));
                rocket.getRocketNBT().setTag(ROCKET_CONFIG_KEY, config.serialize());
            }
        }
    }

    @Override
    public void clearMachineInventory(NonNullList<ItemStack> itemBuffer) {
        super.clearMachineInventory(itemBuffer);
        itemBuffer.add(circuitHolder.getStackInSlot(0));
    }

    public NBTTagCompound getConfig() {
        if (!circuitHolder.getStackInSlot(0).isEmpty()) {
            return circuitHolder.getStackInSlot(0).getTagCompound();
        }
        return null;
    }

    public void writeConfigItemToNBT(NBTTagCompound tag) {
        if (!circuitHolder.getStackInSlot(0).isEmpty()) {
            NBTTagCompound item = new NBTTagCompound();
            circuitHolder.getStackInSlot(0).writeToNBT(item);
            tag.setTag("config", item);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        writeConfigItemToNBT(data);
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.hasKey("config")) {
            this.circuitHolder.setStackInSlot(0, new ItemStack(data.getCompoundTag("config")));
        }
    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager syncManager, UISettings settings) {
        BooleanSyncValue canHandleFullConfig = new BooleanSyncValue(() -> this.canHandleFullConfig,
                x -> this.canHandleFullConfig = x);

        BooleanSyncValue workingStateValue = new BooleanSyncValue(() -> workingEnabled, this::setWorkingEnabled);
        BooleanSyncValue renderBoundingBoxValue = new BooleanSyncValue(() -> renderBoundingBox,
                val -> renderBoundingBox = val);
        BooleanSyncValue highlightSelectedStockValue = new BooleanSyncValue(() -> highlightSelectedStock,
                val -> highlightSelectedStock = val);

        ModularPanel mainPanel = Mui2Utils.defaultPanel(this)
                .child(IKey.lang(getMetaFullName()).asWidget().pos(5, 5))
                .child(SlotGroupWidget.playerInventory(true).left(7).bottom(7))
                .child(Mui2Utils.getLogo().asWidget().size(17).right(7).bottom(88))
                .child(new CycleButtonWidget()
                        .left(7)
                        .bottom(90)
                        .background(GuiTextures.BUTTON_CLEAN)
                        .hoverBackground(GuiTextures.BUTTON_CLEAN)
                        .stateCount(2)
                        .stateOverlay(SusyGuiTextures.BUTTON_POWER)
                        .value(workingStateValue))
                .child(Flow.column()
                        .top(18)
                        .margin(7, 0)
                        .widthRel(1f)
                        .coverChildrenHeight()
                        .child(Flow.row()
                                .coverChildrenHeight()
                                .marginBottom(2)
                                .widthRel(1f)
                                .child(new ToggleButton()
                                        .overlay(SusyGuiTextures.BUTTON_RENDER_AREA
                                                .asIcon()
                                                .size(16))
                                        .addTooltipLine(IKey.lang(
                                                "susy.gui.stock_interactor.button.render_bounding_box.tooltip"))
                                        .value(renderBoundingBoxValue))
                                .child(IKey.lang("susy.gui.stock_interactor.title.render_bounding_box")
                                        .asWidget()
                                        .align(Alignment.CenterRight)
                                        .height(18)))
                        .child(Flow.row()
                                .coverChildrenHeight()
                                .marginBottom(2)
                                .widthRel(1f)
                                .child(new ToggleButton()
                                        .overlay(SusyGuiTextures.BUTTON_RENDER_AREA
                                                .asIcon()
                                                .size(16))
                                        .addTooltipLine(IKey.lang(
                                                "susy.gui.stock_interactor.button.highlight_selected_stock.tooltip"))
                                        .value(highlightSelectedStockValue))
                                .child(IKey.lang("susy.gui.stock_interactor.title.highlight_selected_stock")
                                        .asWidget()
                                        .align(Alignment.CenterRight)
                                        .height(18)))
                        .child(Flow.row()
                                .coverChildrenHeight()
                                .marginBottom(2)
                                .widthRel(1f)
                                .child(IKey.lang("susy.rocket_programmer.circuit_slot").asWidget()
                                        .align(Alignment.CenterRight).height(18))
                                .child(new ItemSlot().slot(SyncHandlers.itemSlot(this.circuitHolder, 0)
                                        .singletonSlotGroup()
                                        .filter((itemStack -> itemStack
                                                .isItemEqual(SuSyMetaItems.ROCKET_CONFIGURER.getStackForm(1))))))
                                .child(IKey.lang("susy.rocket_programmer.not_enough_budget").asWidget()
                                        .align(Alignment.CenterRight).height(18)
                                        .setEnabledIf((p) -> !canHandleFullConfig.getValue()))));

        return mainPanel;
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(this.canHandleFullConfig);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.canHandleFullConfig = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        if (dataId == SuSyDataCodes.UPDATE_CAN_HANDLE_FULL_CONFIG) {
            this.canHandleFullConfig = buf.readBoolean();
        } else {
            super.receiveCustomData(dataId, buf);
        }
    }

    @Override
    protected <T> T getStockCapability(Capability<T> capability, EnumFacing side) {
        return null; // Not a delegator
    }

    public void setLowTierWarning(boolean setWarning) {
        if (this.canHandleFullConfig != setWarning) {
            this.canHandleFullConfig = setWarning;
            if (!getWorld().isRemote) {
                writeCustomData(SuSyDataCodes.UPDATE_CAN_HANDLE_FULL_CONFIG, buf -> buf.writeBoolean(setWarning));
            }
        }
    }
}
