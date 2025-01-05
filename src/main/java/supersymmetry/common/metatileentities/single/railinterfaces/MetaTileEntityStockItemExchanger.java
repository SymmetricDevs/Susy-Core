package supersymmetry.common.metatileentities.single.railinterfaces;

import cam72cam.immersiverailroading.entity.Freight;
import cam72cam.immersiverailroading.entity.FreightTank;
import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.widgets.CycleButtonWidget;
import gregtech.api.gui.widgets.TabGroup;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTTransferUtils;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.SusyLog;
import supersymmetry.client.renderer.textures.SusyTextures;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MetaTileEntityStockItemExchanger extends MetaTileEntityStockInteractor
{
    public boolean pulling;
    public final int inventoryArrayHeight = 5;
    private final int inventoryArrayWidth = 5;
    private ItemStackHandler itemTank;
    private boolean validStockNearby;

    //locomotive, freight
    public static List<String> subFilter = new ArrayList<>();
    static{
        subFilter.add("locomotive");
        subFilter.add("freight");
    }

    public MetaTileEntityStockItemExchanger(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityStockItemExchanger(this.metaTileEntityId);
    }

    public int getLightOpacity() {
        return 1;
    }

    public boolean isOpaqueCube() {
        return true;
    }

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

        if(this.isWorkingEnabled() && this.getOffsetTimer() % 20 == 0 && this.stocks.size() > 0)
        {
            Freight freightStock = (FreightTank)stocks.get(0);
            cam72cam.mod.item.ItemStackHandler umodStockStackHandler = freightStock.cargoItems;
            ItemStackHandler stockStackHandler = umodStockStackHandler.internal;

            if(pulling) {

                GTTransferUtils.moveInventoryItems(stockStackHandler, this.itemTank);
            }
            else {
                GTTransferUtils.moveInventoryItems(this.itemTank, stockStackHandler);
            }
        }
    }

    public boolean needsSneakToRotate() {
        return true;
    }

    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        byte state = 0b00;
        state |= this.isWorkingEnabled() ? 0b01 : 0b00;
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

        //CycleButtonWidget filterIndexButton = new CycleButtonWidget(9 + 8 + 5 * 18, 18, 60, 18, subClassNameMap, () -> this.subFilterIndex, (x) -> this.uiCycleFilter());
        CycleButtonWidget cycleStateButton = new CycleButtonWidget(9 + 8 + 5 * 18, 36, 60, 18, new String[]{"pulling", "pushing"}, () -> this.pulling ? 0 : 1, (x) -> this.setTransferState(x == 0));

        //builder.widget(filterIndexButton);
        builder.widget(cycleStateButton);

        return builder.label(6, 6, I18n.format(getMetaFullName())).bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 8, 18 + 18 * inventoryArrayHeight + 12).build(getHolder(), entityPlayer);
    }

    @Override
    protected void appendDefaultTab(EntityPlayer entityPlayer, TabGroup tabGroup) {

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

    public void setTransferState(boolean state) {
        this.pulling = state;
        this.writeCustomData(0b100, (buf) -> buf.writeBoolean(this.pulling));
    }

    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        this.setTransferState(!this.pulling);

        if (!this.getWorld().isRemote) {
            String displayName = I18n.format(this.getMetaFullName());
            if (this.pulling) {
                playerIn.sendStatusMessage(new TextComponentTranslation("susy.stock_interfaces.pull", displayName), true);
            } else {
                playerIn.sendStatusMessage(new TextComponentTranslation("susy.stock_interfaces.push", displayName), true);
            }
        }

        return true;
    }
}
