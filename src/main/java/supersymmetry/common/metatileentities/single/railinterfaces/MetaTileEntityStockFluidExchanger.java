package supersymmetry.common.metatileentities.single.railinterfaces;

import cam72cam.immersiverailroading.entity.EntityRollingStock;
import cam72cam.immersiverailroading.entity.FreightTank;
import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.impl.FilteredFluidHandler;
import gregtech.api.capability.impl.ThermalFluidHandlerItemStack;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.CycleButtonWidget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.TankWidget;
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
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.stockinteraction.IStockInteractor;
import supersymmetry.api.stockinteraction.StockHelperFunctions;
import supersymmetry.client.renderer.textures.SusyTextures;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

//#fix# can hold plasma acid gas and lava, maybe change that
public class MetaTileEntityStockFluidExchanger extends MetaTileEntity implements IStockInteractor
{
    public int ticksAlive;

    public boolean validStockNearby;
    public boolean pulling;

    private boolean active; //purely for client side rendering, server checks if block is powered #fix# ask about update order (placing redstone wire borks it)

    public final Vec3d detectionArea = new Vec3d(5, 0, 5);

    private final int tankSize = 16000;
    private FilteredFluidHandler fluidTank;

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

    public int getLightOpacity() {
        return 1;
    }

    //#fix# should have comparitor interaction maybe
    public int getActualComparatorValue() {
        return 1;
    }

    public boolean isOpaqueCube() {
        return true;
    }

    //#fix# pickaxe not it maybe
    public String getHarvestTool() {
        return "wrench";
    }

    public boolean hasFrontFacing() {
        return true;
    }

    protected void initializeInventory() {
        super.initializeInventory();
        this.fluidTank = (new FilteredFluidHandler(this.tankSize)).setFillPredicate((stack) -> stack != null && stack.getFluid() != null);
        this.fluidInventory = this.fluidTank;
    }

    public ICapabilityProvider initItemStackCapabilities(ItemStack itemStack) {
        return new ThermalFluidHandlerItemStack(itemStack, this.tankSize, 0x7fffffff, true, true, true, true);
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
            }
        }

        this.fluidTank.setFluid(fluidStack);
    }

    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);

        if((dataId & 0b1) > 0) {
            this.setSubFilterIndex(buf.readByte());
        }
        if ((dataId & 0b10) > 0) {
            this.validStockNearby = buf.readBoolean();
        }
        if ((dataId & 0b100) > 0) {
            this.pulling = buf.readBoolean();
        }
        if ((dataId & 0b1000) > 0) {
            this.active = buf.readBoolean();
        }
        this.scheduleRenderUpdate();
    }

    public void update() {
        super.update();

        if(this.getWorld().isRemote)
            return;

        if(this.ticksAlive % 20 == 0)
        {
            this.onNeighborChanged();

            List<EntityRollingStock> stocks = StockHelperFunctions.GetStockInArea(getFilterIndex(), this.getFrontFacing(), this, this.getWorld());
            boolean newValidNearby = stocks.size() > 0;
            if(newValidNearby != this.validStockNearby || this.ticksAlive == 0)
            {
                //#fix# if buffer is sent to server, then this should be run twice? test.
                this.validStockNearby = newValidNearby;
                this.writeCustomData(0b10, (buf) -> buf.writeBoolean(newValidNearby));
            }

            if(!validStockNearby || !this.isBlockRedstonePowered())
                return;

            FreightTank tankStock = (FreightTank)stocks.get(0);
            cam72cam.mod.fluid.FluidTank umodStockTank = tankStock.theTank;
            FluidTank actualStockTank = umodStockTank.internal;
            FluidStack actualFluidStack = actualStockTank.getFluid();

            if(pulling && actualFluidStack != null && actualFluidStack.getFluid() != null)
            {
                if(fluidTank.getFluid() == null || actualFluidStack.getFluid() == fluidTank.getFluid().getFluid())
                {
                    int space = fluidTank.getCapacity() - fluidTank.getFluidAmount();
                    FluidStack taken = actualStockTank.drain(space, true);
                    fluidTank.fill(taken, true);
                }
            }
            else if (!pulling && fluidTank.getFluid() != null && fluidTank.getFluid().getFluid() != null)
            {
                if(actualFluidStack == null || actualFluidStack.getFluid() == fluidTank.getFluid().getFluid())
                {
                    int space = actualStockTank.getCapacity() - actualStockTank.getFluidAmount();
                    FluidStack given = fluidTank.drain(space, true);
                    actualStockTank.fill(given, true);
                }
            }
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
        this.writeCustomData(0b1000, (buf) -> buf.writeBoolean(this.active));
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
        int w = 160;
        int h = 144;
        int buffer = 16;
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        int fontHeight = fontRenderer.FONT_HEIGHT;
        int tankWidth = 64;
        int tankY = fontHeight + buffer + buffer / 2;

        LabelWidget header = new LabelWidget(buffer, buffer / 2, I18n.format(getMetaFullName()));

        TankWidget tankWidget = new TankWidget(this.fluidTank, buffer, tankY, tankWidth, h - buffer - tankY).setBackgroundTexture(GuiTextures.BACKGROUND);

        CycleButtonWidget filterIndexButton = new CycleButtonWidget(buffer + tankWidth + 8, fontHeight + buffer + buffer / 2, w - (2 * buffer + tankWidth), 24, subClassNameMap, () -> this.subFilterIndex, (x) -> this.uiCycleFilter());

        CycleButtonWidget transferStateButton = new CycleButtonWidget(buffer + tankWidth + 8, fontHeight + buffer + buffer + 24 + buffer / 2, w - (2 * buffer + tankWidth), 24, new String[]{"pulling", "pushing"}, () -> this.pulling ? 0 : 1, (x) -> this.SetTransferState(x == 0));


        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, w, h)
                .widget(header)
                .widget(tankWidget)
                .widget(filterIndexButton)
                .widget(transferStateButton)
                ;

        return builder.build(getHolder(), entityPlayer);
    }

    private void uiCycleFilter() {
        this.cycleFilter(true);
        this.writeCustomData(0b1,  (buf) -> buf.writeByte(this.subFilterIndex));
    }

    //#fix# does detected need to be saved or just refreshed on load? does ticks-alive need to be saved to prevent every one ticking at once?
    //update system based on chunk and global time instead of ticks alive?
    //should detection area be changeable and saved?
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setByte("subFilterIndex", this.subFilterIndex);
        data.setBoolean("validStockNearby", this.validStockNearby);
        data.setBoolean("pulling", this.pulling);
        return data;
    }

    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
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

    //#fix# what does this do
    protected boolean shouldSerializeInventories() {
        return false;
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
        this.writeCustomData(0b1, (buf) -> buf.writeByte(this.subFilterIndex));
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
        this.writeCustomData(0b100, (buf) -> buf.writeBoolean(this.pulling));
    }

    public MetaTileEntity GetMetaTileEntity() {
        return this;
    }

    protected boolean canMachineConnectRedstone(EnumFacing side) {
        return true;
    }
}
