package supersymmetry.common.metatileentities.single.railinterfaces;

import cam72cam.immersiverailroading.entity.EntityRollingStock;
import cam72cam.immersiverailroading.entity.Freight;
import cam72cam.immersiverailroading.entity.FreightTank;
import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.client.utils.TooltipHelper;
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
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import scala.swing.Slider;
import supersymmetry.api.stockinteraction.IStockInteractor;
import supersymmetry.api.stockinteraction.StockHelperFunctions;
import supersymmetry.client.renderer.textures.SusyTextures;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class MetaTileEntityStockReader extends MetaTileEntity implements IStockInteractor {

    public int ticksAlive;

    public byte filterIndex;        //s (not actually needed for rendering)
    public byte signal;             //s
    public boolean readingItems;    //s

    public float stackFillNeeded;
    public int slotUseNeeded;
    public float fluidPercentageNeeded;
    public boolean proportional;

    private float[] thresholds = { 0.5f, 0.6667f, 0.75f, 0.9f };

    public final Vec3d detectionArea = new Vec3d(5, 0, 5);

    public MetaTileEntityStockReader(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityStockReader(this.metaTileEntityId);
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

    public void writeDetectToBuffer(PacketBuffer buf) {
        buf.writeFloat(this.stackFillNeeded);
        buf.writeInt(this.slotUseNeeded);
        buf.writeFloat(this.fluidPercentageNeeded);
        buf.writeBoolean(this.proportional);
    }

    public void readDetectFromBuffer(PacketBuffer buf) {
        this.stackFillNeeded = buf.readFloat();
        this.slotUseNeeded = buf.readInt();
        this.fluidPercentageNeeded = buf.readFloat();
        this.proportional = buf.readBoolean();
    }

    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeByte(this.filterIndex);
        buf.writeByte(this.signal);
        buf.writeBoolean(this.readingItems);

        this.writeDetectToBuffer(buf);
    }

    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.filterIndex = buf.readByte();
        this.signal = buf.readByte();
        this.readingItems = buf.readBoolean();

        this.readDetectFromBuffer(buf);

        this.UpdateRedstoneSignal();
        this.scheduleRenderUpdate();
    }

    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if((dataId & 0b1) > 0) {
            this.filterIndex = buf.readByte();
        }
        if((dataId & 0b10) > 0) {
            this.signal = buf.readByte();
            this.UpdateRedstoneSignal();
            this.scheduleRenderUpdate();
        }
        if((dataId & 0b100) > 0) {
            this.readingItems = buf.readBoolean();
            this.UpdateRedstoneSignal();
            this.scheduleRenderUpdate();
        }
        if((dataId & 0b1000) > 0) {
            this.readDetectFromBuffer(buf);
            this.UpdateRedstoneSignal();
            this.scheduleRenderUpdate();
        }
    }

    public void update() {
        super.update();

        if(this.getWorld().isRemote)
            return;

        if(this.ticksAlive % 20 == 0)
        {
            List<EntityRollingStock> stocks = StockHelperFunctions.GetStockInArea(this.filterIndex, this.getFrontFacing(), this, this.getWorld());

            if(stocks.size() > 0) {
                this.signal = calculateOutputSignal(stocks.get(0));
            }
            else {
                this.signal = 0;
            }

            this.UpdateRedstoneSignal();
            this.writeCustomData(0b10, (buf) -> buf.writeByte(this.signal));
        }

        this.ticksAlive++;
    }

    public byte calculateOutputSignal(EntityRollingStock stock) {
        if(this.readingItems && stock instanceof Freight) {
            Freight invStock = (Freight)stock;
            cam72cam.immersiverailroading.inventory.FilteredStackHandler umodFilteredHandler = invStock.cargoItems;
            ItemStackHandler stockStackHandler = umodFilteredHandler.internal;
            float maxItems = 0;
            float curItems = 0;
            int totalSlots = 0;
            int fullSlots = 0;

            for(int i = 0; i < stockStackHandler.getSlots(); i++) {
                ItemStack stack = stockStackHandler.getStackInSlot(i);
                if(stack != ItemStack.EMPTY) {
                    maxItems += stack.getMaxStackSize();
                    curItems += stack.getCount();
                    fullSlots++;
                }
                totalSlots++;
            }

            if(proportional) {
                return (byte)(15 * (curItems / (maxItems + 64 * (totalSlots - fullSlots))));
            }
            boolean itemsHit = (curItems / maxItems) > this.stackFillNeeded;
            boolean slotsHit = fullSlots > this.slotUseNeeded;
            return (byte)((itemsHit && slotsHit) ? 15 : 0);
        }

        if (!this.readingItems && stock instanceof FreightTank) {
            FreightTank tankStock = (FreightTank)stock;
            cam72cam.mod.fluid.FluidTank umodStockTank = tankStock.theTank;
            FluidTank actualStockTank = umodStockTank.internal;
            float fluidFullness = (float)actualStockTank.getFluidAmount() / (float)actualStockTank.getCapacity();
            if(proportional) {
                return (byte)((fluidFullness));
            }
            return (byte) ((fluidFullness > this.fluidPercentageNeeded) ? 15 : 0);
        }

        return 0;
    }

    public boolean needsSneakToRotate() {
        return true;
    }

    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        if(this.readingItems) {
            SusyTextures.STOCK_READER_ITEM.renderOrientedState(renderState, translation, pipeline, Cuboid6.full, this.getFrontFacing(), true, true);
        }
        else {
            SusyTextures.STOCK_READER_FLUID.renderOrientedState(renderState, translation, pipeline, Cuboid6.full, this.getFrontFacing(), true, true);
        }
    }

    //#fix# figure out how to add translations like with I18n instead of just english
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip, boolean advanced) {
        if (TooltipHelper.isShiftDown()) {
            //tooltip.add("Screwdriver to cycle filter, wrench to change detection mode");
        } else {
            tooltip.add("this block outputs redstone signal based on the inventory/tank of the stock in front of it");
            tooltip.add("right click for configuration gui");
            //tooltip.add(I18n.format("gregtech.tooltip.tool_hold_shift"));
        }
    }

    public void UpdateRedstoneSignal() {
        this.setOutputRedstoneSignal(EnumFacing.byIndex(0), signal);
        this.setOutputRedstoneSignal(EnumFacing.byIndex(1), signal);
        this.setOutputRedstoneSignal(EnumFacing.byIndex(2), signal);
        this.setOutputRedstoneSignal(EnumFacing.byIndex(3), signal);
        this.setOutputRedstoneSignal(EnumFacing.byIndex(4), signal);
        this.setOutputRedstoneSignal(EnumFacing.byIndex(5), signal);
    }

    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);

        data.setByte("filterIndex", this.filterIndex);
        data.setByte("signal", this.signal);
        data.setBoolean("readingItems", this.readingItems);

        data.setFloat("stackFillNeeded", this.stackFillNeeded);
        data.setInteger("slotUseNeeded", this.slotUseNeeded);
        data.setFloat("fluidPercentageNeeded", this.fluidPercentageNeeded);
        data.setBoolean("proportional", this.proportional);

        return data;
    }

    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);

        this.filterIndex = data.getByte("filterIndex");
        this.signal = data.getByte("signal");
        this.readingItems = data.getBoolean("readingItems");

        this.stackFillNeeded = data.getFloat("stackFillNeeded");
        this.slotUseNeeded = data.getInteger("slotUseNeeded");
        this.fluidPercentageNeeded = data.getFloat("fluidPercentageNeeded");
        this.proportional = data.getBoolean("proportional");
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {

        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        int strh = fontRenderer.FONT_HEIGHT;

        int buffer = 8;

        List<Widget> itemOnlyWidgets = new ArrayList<Widget>();
        List<Widget> fluidOnlyWidgets = new ArrayList<Widget>();
        List<Widget> proportionalOnlyWidgets = new ArrayList<Widget>();

        int row1y = buffer;
        int row1MinX = fontRenderer.getStringWidth(getMetaFullName()) + buffer * 4;

        int row2y = row1y + strh + buffer;
        int row2height = 25;
        int r2b1w = fontRenderer.getStringWidth("proportional ( )" + 4);
        int r2b2w = fontRenderer.getStringWidth("locomotive") + 4;
        int r2b3w = fontRenderer.getStringWidth("fluid") + 4;
        int row2MinX = buffer + r2b1w + buffer + r2b2w + buffer + r2b3w + buffer;

        int row3y = row2y + row2height + buffer;
        int row3height = 25;
        int row4y = row3y + row3height + buffer;
        int row4height = 25;
        int row3MinX = fontRenderer.getStringWidth("fluid fullness 1234") + 4;


        int trueMinX = Integer.max(row1MinX, row2MinX);
        trueMinX = Integer.max(trueMinX, row3MinX);
        trueMinX += buffer;

        int h = row4y + row4height + buffer;
        int w = trueMinX;

        int leftoverR1 = w - row1MinX;
        int leftoverR2 = w - row2MinX;
        int leftoverR3 = w - row3MinX;

        final Consumer<Integer> SetAllVis = (x) -> {
            this.visAll(proportionalOnlyWidgets, this.proportional);
            this.visAll(itemOnlyWidgets, !this.proportional && this.readingItems);
            this.visAll(fluidOnlyWidgets, !this.proportional && !this.readingItems);
        };

        //row 1
        LabelWidget header = new LabelWidget(w / 2, row1y, "Stock reader"); //getMetaFullName()) #fix# add translations
        header.setXCentered(true);

        //row 2
        CycleButtonWidget propCycleButtom = new CycleButtonWidget(buffer, row2y, r2b1w + (leftoverR2 / 3), row2height, new String[]{ "proportional: (F)", "proportional: (T)"}, () -> this.proportional ? 1 : 0,
            (x) -> {
                this.proportional = (x == 1);
                SetAllVis.accept(0);
            });

        CycleButtonWidget filterIndexButton = new CycleButtonWidget(buffer + r2b1w + buffer +  + (leftoverR2 / 3), row2y, r2b2w + (leftoverR2 / 3), row2height, StockHelperFunctions.ClassNameMap, () -> this.filterIndex, (x) -> this.uiCycleFilter());

        CycleButtonWidget typeToggleButton = new CycleButtonWidget(buffer + r2b1w + buffer + r2b2w + buffer +  + (2 * leftoverR2 / 3), row2y, r2b3w + (leftoverR2 / 3), row2height, new String[]{"items", "fluid"}, () -> this.readingItems ? 0 : 1,
            (x) -> {
                this.readingItems = (x == 0);
                SetAllVis.accept(0);
            });

        //item only (rows 3 and 4)
        SliderWidget fullnessSlider = new SliderWidget("fullness required: ", buffer, row3y, w - buffer - buffer, row3height, 0, 1, this.stackFillNeeded, (x) -> this.stackFillNeeded = x);
        SliderWidget slotNeedSlider = new SliderWidget("slots required: ", buffer, row4y, w - buffer - buffer, row4height, 0, 99, this.slotUseNeeded, (x) -> this.slotUseNeeded = (int)x);
        itemOnlyWidgets.add(fullnessSlider);
        itemOnlyWidgets.add(slotNeedSlider);

        //fluid only (row 3)
        SliderWidget fluidFullnessSlider = new SliderWidget("fluid fullness required: ", buffer, row3y, w - buffer - buffer, row3height, 0, 1, this.fluidPercentageNeeded, (x) -> this.fluidPercentageNeeded = x);
        fluidOnlyWidgets.add(fluidFullnessSlider);

        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, w, h)
                .widget(header)
                .widget(propCycleButtom)
                .widget(filterIndexButton)
                .widget(typeToggleButton)

                .widget(fullnessSlider)
                .widget(slotNeedSlider)

                .widget(fluidFullnessSlider)
                ;

        ModularUI modd =  builder.build(getHolder(), entityPlayer);
        SetAllVis.accept(0);
        modd.holder.markAsDirty();

        return modd;
    }

    private void uiCycleFilter() {
        this.cycleFilter(true);
        this.writeCustomData(0b1,  (buf) -> buf.writeByte(this.filterIndex));
    }

    private void uiCycleItemReading() {
        this.readingItems = !this.readingItems;
        this.writeCustomData(0b100, (buf) -> buf.writeBoolean(this.readingItems));
    }

    private void uiCycleProportional() {
        this.proportional = !this.proportional;
        this.writeCustomData(0b1000,  (buf) -> this.writeDetectToBuffer(buf));

    }

    private void visAll(List<Widget> widgets, boolean vis) {
        for(Widget w : widgets) {
            w.setVisible(vis);
        }
    }

    public Vec3d getInteractionArea() {
        return this.detectionArea;
    }

    public void cycleFilter(boolean up) {
        this.filterIndex = StockHelperFunctions.CycleFilter(this.filterIndex, up);
        this.writeCustomData(0b1, (buf) -> buf.writeByte(this.filterIndex));
    }

    public void cycleFilterUp() {
        this.cycleFilter(true);
    }

    public byte getFilterIndex() {
        return this.filterIndex;
    }

    public Class getFilter() { return StockHelperFunctions.ClassMap[this.getFilterIndex()]; }

    public MetaTileEntity GetMetaTileEntity() {
        return this;
    }

    protected boolean canMachineConnectRedstone(EnumFacing side) {
        return true;
    }
}
