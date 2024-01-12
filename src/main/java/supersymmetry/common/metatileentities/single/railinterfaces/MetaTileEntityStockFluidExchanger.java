package supersymmetry.common.metatileentities.single.railinterfaces;

import cam72cam.immersiverailroading.entity.EntityRollingStock;
import cam72cam.immersiverailroading.entity.FreightTank;
import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.IFilter;
import gregtech.api.capability.IFilteredFluidContainer;
import gregtech.api.capability.impl.FilteredItemHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.GTFluidHandlerItemStack;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.SusyLog;
import supersymmetry.api.stockinteraction.IStockInteractor;
import supersymmetry.api.stockinteraction.StockHelperFunctions;
import supersymmetry.client.renderer.textures.SusyTextures;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

//#fix# can hold plasma acid gas and lava, maybe change that
public class MetaTileEntityStockFluidExchanger extends MetaTileEntity implements IStockInteractor
{
    public int ticksAlive;

    public boolean validStockNearby;
    public boolean pulling;

    private boolean active; //purely for client side rendering, server checks if block is powered #fix# ask about update order (placing redstone wire borks it)

    public final Vec3d detectionArea = new Vec3d(5, 0, 5);

    public int transferRate = 4000;
    private final int tankSize = 16000;
    private FluidTank fluidTank;
    //private EnumFacing outputFacing;

    private boolean locked;
    private FluidStack lockedFluid;

    //locomotive, tank
    private static final byte[] subClassMap = { 1, 2 };
    private byte subFilterIndex = 0;
    private static final String[] subClassNameMap = { StockHelperFunctions.ClassNameMap[1], StockHelperFunctions.ClassNameMap[2] };

    public MetaTileEntityStockFluidExchanger(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        this.ticksAlive = 0;
        this.setSubFilterIndex((byte)0);
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityStockFluidExchanger(this.metaTileEntityId);
    }

    protected FluidTankList createImportFluidHandler() {
        return new FluidTankList(false, new IFluidTank[]{this.fluidTank});
    }

    protected FluidTankList createExportFluidHandler() {
        return new FluidTankList(false, new IFluidTank[]{this.fluidTank});
    }

    protected IItemHandlerModifiable createImportItemHandler() {
        return (new FilteredItemHandler(1)).setFillPredicate(FilteredItemHandler.getCapabilityFilter(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY));
    }

    protected IItemHandlerModifiable createExportItemHandler() {
        return new ItemStackHandler(1);
    }

    public String getHarvestTool() {
        return "wrench";
    }

    public boolean hasFrontFacing() {
        return true;
    }

    protected void initializeInventory() {
        super.initializeInventory();
        this.fluidTank = (new LockableFluidTank(this.tankSize));
        this.fluidInventory = this.fluidTank;
        this.importFluids = new FluidTankList(false, new IFluidTank[]{this.fluidTank});
        this.exportFluids = new FluidTankList(false, new IFluidTank[]{this.fluidTank});
    }

    public ICapabilityProvider initItemStackCapabilities(ItemStack itemStack) {
        return new GTFluidHandlerItemStack(itemStack, this.tankSize);
    }

    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeByte(this.subFilterIndex);
        buf.writeBoolean(this.validStockNearby);
        buf.writeBoolean(this.pulling);
        buf.writeBoolean(this.active);

        FluidStack fluidStack = this.fluidTank.getFluid();
        buf.writeBoolean(fluidStack != null);
        if (fluidStack != null) {
            NBTTagCompound tagCompound = new NBTTagCompound();
            fluidStack.writeToNBT(tagCompound);
            buf.writeCompoundTag(tagCompound);
        }
    }

    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.setSubFilterIndex(buf.readByte());
        this.validStockNearby = buf.readBoolean();
        this.pulling = buf.readBoolean();
        this.active = buf.readBoolean();
        this.scheduleRenderUpdate();

        FluidStack fluidStack = null;
        if (buf.readBoolean()) {
            try {
                NBTTagCompound tagCompound = buf.readCompoundTag();
                fluidStack = FluidStack.loadFluidStackFromNBT(tagCompound);
            } catch (IOException ignored) {
                SusyLog.logger.warn("Failed to load fluid from NBT in Stock Interactor at " + this.getPos() + "on initial sync.");
            }
        }

        this.fluidTank.setFluid(fluidStack);
    }

    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if(dataId == 6500) {
            this.setSubFilterIndex(buf.readByte());
        }
        if (dataId == 6501) {
            this.validStockNearby = buf.readBoolean();
        }
        if (dataId == 6502) {
            this.pulling = buf.readBoolean();
        }
        if (dataId == 6503) {
            this.active = buf.readBoolean();
        }

        this.scheduleRenderUpdate();
    }

    public void update() {
        super.update();

        if(this.getWorld().isRemote)
            return;

        this.fillContainerFromInternalTank();
        this.fillInternalTankFromFluidContainer();

        if(this.ticksAlive % 20 == 0)
        {
            this.onNeighborChanged();

            List<EntityRollingStock> stocks = StockHelperFunctions.GetStockInArea(getFilterIndex(), this.getFrontFacing(), this, this.getWorld());
            boolean newValidNearby = stocks.size() > 0;
            if(newValidNearby != this.validStockNearby || this.ticksAlive == 0)
            {
                //#fix# if buffer is sent to server, then this should be run twice? test.
                this.validStockNearby = newValidNearby;
                this.writeCustomData(6501, (buf) -> buf.writeBoolean(newValidNearby));
            }

            if(!validStockNearby || !this.isBlockRedstonePowered())
                return;

            FreightTank tankStock = (FreightTank)stocks.get(0);
            cam72cam.mod.fluid.FluidTank umodStockTank = tankStock.theTank;
            FluidTank actualStockTank = umodStockTank.internal;

            if(pulling)
                FluidUtil.tryFluidTransfer(fluidTank, actualStockTank, this.transferRate, true);
            else
                FluidUtil.tryFluidTransfer(actualStockTank, fluidTank, this.transferRate, true);
        }

        this.ticksAlive++;
    }

    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing wrenchSide, CuboidRayTraceResult hitResult) {
        return super.onScrewdriverClick(playerIn, hand, wrenchSide, hitResult);
    }
    public void onNeighborChanged() {
        if(this.getWorld().isRemote)
            return;
        this.updateInputRedstoneSignals();
        this.active = this.isBlockRedstonePowered();
        this.writeCustomData(6503, (buf) -> buf.writeBoolean(this.active));
    }

    public boolean onWrenchClick(EntityPlayer playerIn, EnumHand hand, EnumFacing wrenchSide, CuboidRayTraceResult hitResult) {
        return super.onWrenchClick(playerIn, hand, wrenchSide, hitResult);
    }

    public boolean needsSneakToRotate() {
        return true;
    }

    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        byte state = 0b00;
        state |= active ? 0b01 : 0b00;
        state |= pulling ? 0b10 : 0b00;

        switch (state) {
            case 0b00 ->
                    SusyTextures.STOCK_FLUID_EXCHANGER_PUSHING_OFF.renderOrientedState(renderState, translation, pipeline, Cuboid6.full, this.getFrontFacing(), true, true);
            case 0b01 ->
                    SusyTextures.STOCK_FLUID_EXCHANGER_PUSHING_ON.renderOrientedState(renderState, translation, pipeline, Cuboid6.full, this.getFrontFacing(), true, true);
            case 0b10 ->
                    SusyTextures.STOCK_FLUID_EXCHANGER_PULLING_OFF.renderOrientedState(renderState, translation, pipeline, Cuboid6.full, this.getFrontFacing(), true, true);
            case 0b11 ->
                    SusyTextures.STOCK_FLUID_EXCHANGER_PULLING_ON.renderOrientedState(renderState, translation, pipeline, Cuboid6.full, this.getFrontFacing(), true, true);
        }
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("susy.stock_interfaces.fluid_exchanger.description"));
        tooltip.add(I18n.format("susy.stock_interfaces.right_click_for_gui"));
    }

    protected ModularUI createUI(EntityPlayer entityPlayer) {

        TankWidget tankWidget = (new PhantomTankWidget(this.fluidTank, 69, 43, 18, 18, () -> {
            return this.lockedFluid;
        }, (f) -> {
            if (this.fluidTank.getFluidAmount() == 0) {
                if (f == null) {
                    this.setLocked(false);
                    this.lockedFluid = null;
                } else {
                    this.setLocked(true);
                    this.lockedFluid = f.copy();
                    this.lockedFluid.amount = 1;
                }

            }
        })).setAlwaysShowFull(true).setDrawHoveringText(false);

        CycleButtonWidget filterIndexButton = new CycleButtonWidget(150, 20, 60, 24, subClassNameMap, () -> this.subFilterIndex, (x) -> this.uiCycleFilter());
        CycleButtonWidget transferStateButton = new CycleButtonWidget(150, 44, 60, 24, new String[]{"pulling", "pushing"}, () -> this.pulling ? 0 : 1, (x) -> this.SetTransferState(x == 0));


        return ModularUI.defaultBuilder()
                .widget(filterIndexButton)
                .widget(transferStateButton)
                .widget(new ImageWidget(7, 16, 81, 46, GuiTextures.DISPLAY))
                .widget(new LabelWidget(11, 20, "gregtech.gui.fluid_amount", 16777215))
                .widget(tankWidget).widget(new AdvancedTextWidget(11, 30, this.getFluidAmountText(tankWidget), 16777215))
                .widget(new AdvancedTextWidget(11, 40, this.getFluidNameText(tankWidget), 16777215))
                .label(6, 6, this.getMetaFullName())
                .widget((new FluidContainerSlotWidget(this.importItems, 0, 90, 17, false)).setBackgroundTexture(new IGuiTexture[]{GuiTextures.SLOT, GuiTextures.IN_SLOT_OVERLAY}))
                .widget((new SlotWidget(this.exportItems, 0, 90, 44, true, false)).setBackgroundTexture(new IGuiTexture[]{GuiTextures.SLOT, GuiTextures.OUT_SLOT_OVERLAY}))
                .widget((new ToggleButtonWidget(7, 64, 18, 18, GuiTextures.BUTTON_LOCK, this::isLocked, this::setLocked)).setTooltipText("gregtech.gui.fluid_lock.tooltip", new Object[0]).shouldUseBaseBackground())
                .bindPlayerInventory(entityPlayer.inventory).build(this.getHolder(), entityPlayer);
    }

    private Consumer<List<ITextComponent>> getFluidNameText(TankWidget tankWidget) {
        return (list) -> {
            String fluidName = "";
            if (tankWidget.getFluidUnlocalizedName().isEmpty()) {
                if (this.lockedFluid != null) {
                    fluidName = this.lockedFluid.getUnlocalizedName();
                }
            } else {
                fluidName = tankWidget.getFluidUnlocalizedName();
            }

            if (!fluidName.isEmpty()) {
                list.add(new TextComponentTranslation(fluidName, new Object[0]));
            }

        };
    }

    private Consumer<List<ITextComponent>> getFluidAmountText(TankWidget tankWidget) {
        return (list) -> {
            String fluidAmount = "";
            if (tankWidget.getFormattedFluidAmount().equals("0")) {
                if (this.lockedFluid != null) {
                    fluidAmount = "0";
                }
            } else {
                fluidAmount = tankWidget.getFormattedFluidAmount();
            }

            if (!fluidAmount.isEmpty()) {
                list.add(new TextComponentString(fluidAmount));
            }

        };
    }

    private void uiCycleFilter() {
        this.cycleFilter(true);
        this.writeCustomData(6500,  (buf) -> buf.writeByte(this.subFilterIndex));
    }

    //#fix# does detected need to be saved or just refreshed on load? does ticks-alive need to be saved to prevent every one ticking at once?
    //update system based on chunk and global time instead of ticks alive?
    //should detection area be changeable and saved?
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("FluidInventory", this.fluidTank.writeToNBT(new NBTTagCompound()));
        data.setBoolean("locked", this.locked);
        if (this.locked && this.lockedFluid != null) {
            data.setTag("lockedFluid", this.lockedFluid.writeToNBT(new NBTTagCompound()));
        }
        data.setByte("subFilterIndex", this.subFilterIndex);
        data.setBoolean("validStockNearby", this.validStockNearby);
        data.setBoolean("pulling", this.pulling);
        return data;
    }

    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.fluidTank.readFromNBT(data.getCompoundTag("FluidInventory"));
        this.locked = data.getBoolean("locked");
        this.lockedFluid = this.locked ? FluidStack.loadFluidStackFromNBT(data.getCompoundTag("lockedFluid")) : null;

        this.setSubFilterIndex(data.getByte("subFilterIndex"));
        this.validStockNearby = data.getBoolean("validStockNearby");
        this.pulling = data.getBoolean("pulling");
    }

    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        if (!playerIn.getHeldItem(hand).hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            return super.onRightClick(playerIn, hand, facing, hitResult);
        } else {
            return this.getWorld().isRemote || !playerIn.isSneaking() && FluidUtil.interactWithFluidHandler(playerIn, hand, this.fluidTank);
        }
    }

    private boolean isLocked() {
        return this.locked;
    }

    private void setLocked(boolean locked) {
        if (this.locked != locked) {
            this.locked = locked;
            if (!this.getWorld().isRemote) {
                this.markDirty();
            }

            if (locked && this.fluidTank.getFluid() != null) {
                this.lockedFluid = this.fluidTank.getFluid().copy();
                this.lockedFluid.amount = 1;
            } else {
                this.lockedFluid = null;
            }
        }
    }

    public Vec3d getInteractionArea() {
        return this.detectionArea;
    }

    public boolean setFilterIndex(byte index) {
        for(byte i = 0; i < subClassMap.length; i++) {
            if(subClassMap[i] == index) {
                this.setSubFilterIndex(i);
                return true;
            }
        }
        this.setSubFilterIndex((byte)0);
        return false;
    }

    public void setSubFilterIndex(byte index) {
        this.subFilterIndex = index;
    }

    public void cycleFilter(boolean up) {
        this.subFilterIndex = StockHelperFunctions.CycleFilter(this.subFilterIndex, up, (byte)subClassMap.length);
        this.writeCustomData(6500, (buf) -> buf.writeByte(this.subFilterIndex));
    }

    public void cycleFilterUp() {
        this.cycleFilter(true);
    }

    public byte getFilterIndex() {
        return subClassMap[this.subFilterIndex];
    }


    public Class<?> getFilter() { return StockHelperFunctions.ClassMap[this.getFilterIndex()]; }

    public void SetTransferState(boolean state) {
        this.pulling = state;
        this.writeCustomData(6502, (buf) -> buf.writeBoolean(this.pulling));
    }

    public MetaTileEntity GetMetaTileEntity() {
        return this;
    }

    protected boolean canMachineConnectRedstone(EnumFacing side) {
        return true;
    }

    public boolean isOpaqueCube() {
        return false;
    }

    public int getLightOpacity() {
        return 0;
    }

    // Most of the locking logic taken from Quantum Tanks, can potentially be abstracted

    private class LockableFluidTank extends FluidTank implements IFilteredFluidContainer, IFilter<FluidStack> {
        public LockableFluidTank(int capacity) {
            super(capacity);
        }

        public int fillInternal(FluidStack resource, boolean doFill) {
            int accepted = super.fillInternal(resource, doFill);
            if (accepted == 0 && !resource.isFluidEqual(this.getFluid())) {
                return 0;
            } else {
                if (doFill && MetaTileEntityStockFluidExchanger.this.locked && MetaTileEntityStockFluidExchanger.this.lockedFluid == null) {
                    MetaTileEntityStockFluidExchanger.this.lockedFluid = resource.copy();
                    MetaTileEntityStockFluidExchanger.this.lockedFluid.amount = 1;
                }

                return accepted;
            }
        }

        public boolean canFillFluidType(FluidStack fluid) {
            return this.test(fluid);
        }

        public IFilter<FluidStack> getFilter() {
            return this;
        }

        public boolean test(@Nonnull FluidStack fluidStack) {
            return !MetaTileEntityStockFluidExchanger.this.locked || MetaTileEntityStockFluidExchanger.this.lockedFluid == null || fluidStack.isFluidEqual(MetaTileEntityStockFluidExchanger.this.lockedFluid);
        }

        public int getPriority() {
            return MetaTileEntityStockFluidExchanger.this.locked && MetaTileEntityStockFluidExchanger.this.lockedFluid != null ? IFilter.whitelistPriority(1) : IFilter.noPriority();
        }
    }

}
