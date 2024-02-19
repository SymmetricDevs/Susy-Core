package supersymmetry.common.metatileentities.single.railinterfaces;
/*
// TODO: Adapt to MetaTileEntityStockInteractor
import cam72cam.immersiverailroading.entity.EntityRollingStock;
import cam72cam.immersiverailroading.entity.Freight;
import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.widgets.CycleButtonWidget;
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
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.SusyLog;
import supersymmetry.api.stockinteraction.StockHelperFunctions;
import supersymmetry.client.renderer.textures.SusyTextures;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

public class MetaTileEntityStockItemExchanger extends MetaTileEntityStockInteractor
{
    public boolean pulling;

    public final int inventoryArrayHeight = 5;
    private final int inventoryArrayWidth = 5;
    private ItemStackHandler itemTank;

    //locomotive, tank #fix# redo sub class map system

    public MetaTileEntityStockItemExchanger(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityStockItemExchanger(this.metaTileEntityId);
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
        this.itemTank = (new ItemStackHandler(this.inventoryArrayHeight * this.inventoryArrayWidth));
        this.itemInventory = this.itemTank;
    }

    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(this.validStockNearby);
        buf.writeBoolean(this.pulling);

        for(int i = 0; i < this.inventoryArrayHeight * this.inventoryArrayWidth; i++) {
            ItemStack stack = this.itemTank.getStackInSlot(i);
            NBTTagCompound tagCompound = new NBTTagCompound();
            stack.writeToNBT(tagCompound);
            buf.writeCompoundTag(tagCompound);
        }
    }

    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.validStockNearby = buf.readBoolean();
        this.pulling = buf.readBoolean();
        this.scheduleRenderUpdate();

        for(int i = 0; i < this.inventoryArrayHeight * this.inventoryArrayWidth; i++) {
            try {
                ItemStack stack = new ItemStack(buf.readCompoundTag());
                this.itemTank.insertItem(i, stack, false);
            } catch (IOException e) {
                SusyLog.logger.warn("Could not deserialize stock item exchanger inventory at " + getPos());
            }
        }
    }

    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);

        if (dataId == 6501) {
            this.pulling = buf.readBoolean();
        }

        this.scheduleRenderUpdate();
    }

    public void update() {
        super.update();

        if(this.getWorld().isRemote)
            return;

        if(this.getOffsetTimer() % 20 == 0)
        {
            List<EntityRollingStock> stocks = StockHelperFunctions.GetStockInArea(this.subClassMap[this.subFilterIndex], this.getFrontFacing(), this, this.getWorld());
            boolean newValidNearby = stocks.size() > 0;
            if(newValidNearby != this.validStockNearby || this.ticksAlive == 0)
            {
                //#fix# if buffer is sent to server, then this should be run twice? test.
                this.validStockNearby = newValidNearby;
                this.writeCustomData(0b10, (buf) -> buf.writeBoolean(newValidNearby));
            }

            if(!validStockNearby || !this.isBlockRedstonePowered())
                return;

            Freight invStock = (Freight)stocks.get(0);
            cam72cam.immersiverailroading.inventory.FilteredStackHandler umodFilteredHandler = invStock.cargoItems;
            ItemStackHandler stockStackHandler = umodFilteredHandler.internal;

            if(pulling) {
                this.TransferAll(stockStackHandler, this.itemTank);
            }
            else {
                this.TransferAll(this.itemTank, stockStackHandler);
            }
        }
    }

    public void TransferAll(ItemStackHandler from, ItemStackHandler to) {
        for(int i = 0; i < from.getSlots(); i++) {
            if(!from.getStackInSlot(i).isEmpty())
            {
                for(int j = 0; j < to.getSlots();) {
                    if(to.getStackInSlot(j).isEmpty() || ItemHandlerHelper.canItemStacksStack(from.getStackInSlot(i), to.getStackInSlot(j))) {
                        from.setStackInSlot(i, to.insertItem(j, from.getStackInSlot(i), false));
                        j++;
                        if(from.getStackInSlot(i).isEmpty())
                            j = to.getSlots() + 1;
                    }
                }
            }
        }
    }

    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing wrenchSide, CuboidRayTraceResult hitResult) {
        return super.onScrewdriverClick(playerIn, hand, wrenchSide, hitResult);
    }

    public void onNeighborChanged() {
        if(this.getWorld().isRemote)
            return;
        this.updateInputRedstoneSignals();
        this.active = this.isBlockRedstonePowered();
        this.scheduleRenderUpdate();
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
            case 0b00:
                SusyTextures.STOCK_ITEM_EXCHANGER_PUSHING_OFF.renderOrientedState(renderState, translation, pipeline, Cuboid6.full, this.getFrontFacing(), true, true);
                break;
            case 0b01:
                SusyTextures.STOCK_ITEM_EXCHANGER_PUSHING_ON.renderOrientedState(renderState, translation, pipeline, Cuboid6.full, this.getFrontFacing(), true, true);
                break;
            case 0b10:
                SusyTextures.STOCK_ITEM_EXCHANGER_PULLING_OFF.renderOrientedState(renderState, translation, pipeline, Cuboid6.full, this.getFrontFacing(), true, true);
                break;
            case 0b11:
                SusyTextures.STOCK_ITEM_EXCHANGER_PULLING_ON.renderOrientedState(renderState, translation, pipeline, Cuboid6.full, this.getFrontFacing(), true, true);
                break;
        }
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("susy.stock_interfaces.item_exchanger.description"));
        tooltip.add(I18n.format("susy.stock_interfaces.screwdriver_cycle"));
        tooltip.add(I18n.format("susy.stock_interfaces.wrench_toggle"));
    }

    //#fix# what does this do
    public boolean showToolUsages() {
        return false;
    }

    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176, Math.max(166, 18 + 18 * this.inventoryArrayHeight + 94));


        for(int j = 0; j < inventoryArrayHeight; j++) {
            for(int i = 0; i < inventoryArrayWidth; i++) {
                builder = builder.slot(this.itemTank, j * inventoryArrayWidth + i, 8 + i * 18, 18 + j * 18, true, true, new IGuiTexture[]{GuiTextures.SLOT});
            }
        }

        CycleButtonWidget filterIndexButton = new CycleButtonWidget(9 + 8 + 5 * 18, 18, 60, 18, subClassNameMap, () -> this.subFilterIndex, (x) -> this.uiCycleFilter());
        CycleButtonWidget cycleStateButton = new CycleButtonWidget(9 + 8 + 5 * 18, 36, 60, 18, new String[]{"pulling", "pushing"}, () -> this.pulling ? 0 : 1, (x) -> this.SetTransferState(x == 0));

        builder.widget(filterIndexButton);
        builder.widget(cycleStateButton);

        return builder.label(6, 6, I18n.format(getMetaFullName())).bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 8, 18 + 18 * inventoryArrayHeight + 12).build(getHolder(), entityPlayer);
    }

    private void uiCycleFilter() {
        this.cycleFilter(true);
    }

    //#fix# does detected need to be saved or just refreshed on load? does ticks-alive need to be saved to prevent every one ticking at once?
    //update system based on chunk and global time instead of ticks alive?
    //should detection area be changeable and saved?
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("validStockNearby", this.validStockNearby);
        data.setBoolean("pulling", this.pulling);
        return data;
    }

    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.validStockNearby = data.getBoolean("validStockNearby");
        this.pulling = data.getBoolean("pulling");
    }

    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        return super.onRightClick(playerIn, hand, facing, hitResult);
    }

    //#fix# what does this do
    protected boolean shouldSerializeInventories() {
        return false;
    }

    public Vec3d getInteractionArea() {
        return this.detectionArea;
    }

    public void cycleFilter(boolean up) {
        this.subFilterIndex = StockHelperFunctions.CycleFilter(this.subFilterIndex, up, (byte)this.subClassMap.length);
        this.writeCustomData(0b1, (buf) -> buf.writeByte(this.subFilterIndex));
    }

    public void cycleFilterUp() {
        this.cycleFilter(true);
    }

    public byte getFilterIndex() {
        return this.subFilterIndex;
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

    public Class getFilter() { return StockHelperFunctions.ClassMap[this.getFilterIndex()]; }

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
*/