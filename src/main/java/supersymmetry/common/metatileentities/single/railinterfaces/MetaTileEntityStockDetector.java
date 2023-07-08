package supersymmetry.common.metatileentities.single.railinterfaces;

import cam72cam.immersiverailroading.entity.EntityRollingStock;
import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.texture.TextureUtils;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.TooltipHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.stockinteraction.IStockInteractor;
import supersymmetry.api.stockinteraction.StockHelperFunctions;
import supersymmetry.client.renderer.textures.SusyTextures;

import javax.annotation.Nullable;
import java.util.List;

//#fix# make sure data is initialized correctly (on initial sync?, in constructor? find out.)
public class MetaTileEntityStockDetector extends MetaTileEntity implements IStockInteractor
{
    public int ticksAlive;

    public byte filterIndex;
    public boolean detecting;

    public final Vec3d detectionArea = new Vec3d(5, 0, 5);

    private final int PacketIDAll = 0x616C6C;
    private final int PacketIDDetect = 0x646574;
    private final int PackIDFilterIndex = 0x696E64;

    public MetaTileEntityStockDetector(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityStockDetector(this.metaTileEntityId);
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

    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeByte(this.filterIndex);
        buf.writeBoolean(this.detecting);
    }

    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.filterIndex = buf.readByte();
        this.setDetecting(buf.readBoolean());
        this.scheduleRenderUpdate();
    }

    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == PacketIDAll)
        {
            this.filterIndex = buf.readByte();
            this.setDetecting(buf.readBoolean());
            this.scheduleRenderUpdate();
        }
        else if (dataId == PackIDFilterIndex)
        {
            this.filterIndex = buf.readByte();
            this.scheduleRenderUpdate();
        }
        else if (dataId == PacketIDDetect)
        {
            this.setDetecting(buf.readBoolean());
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
            boolean newDetected = stocks.size() > 0;
            if(newDetected != this.detecting || this.ticksAlive == 0)
            {
                this.setDetecting(newDetected);
                this.writeCustomData(PacketIDDetect, (buf) -> buf.writeBoolean(newDetected));
            }
        }

        this.ticksAlive++;
    }

    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing wrenchSide, CuboidRayTraceResult hitResult) {
        if (!playerIn.isSneaking()) {
            if (this.getWorld().isRemote) {
                this.scheduleRenderUpdate();
            } else {
                this.cycleFilterUp();
                playerIn.sendMessage(new TextComponentTranslation("Filter set to " + (this.filterIndex == 0 ? "none" : StockHelperFunctions.ClassNameMap[filterIndex])));
            }
            return true;
        }
        return super.onScrewdriverClick(playerIn, hand, wrenchSide, hitResult);
    }

    public boolean needsSneakToRotate() {
        return true;
    }

    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        byte state = 0b00;
        state |= (this.detecting ? 0b01 : 0b00);
        state |= (this.usingFilter() ? 0b10 : 0b00);

        switch (state) {
            case 0b00 ->
                    SusyTextures.STOCK_DETECTOR_NEITHER.renderOrientedState(renderState, translation, pipeline, Cuboid6.full, this.getFrontFacing(), true, true);
            case 0b01 ->
                    SusyTextures.STOCK_DETECTOR_DETECTING.renderOrientedState(renderState, translation, pipeline, Cuboid6.full, this.getFrontFacing(), true, true);
            case 0b10 ->
                    SusyTextures.STOCK_DETECTOR_FILTER.renderOrientedState(renderState, translation, pipeline, Cuboid6.full, this.getFrontFacing(), true, true);
            case 0b11 ->
                    SusyTextures.STOCK_DETECTOR_BOTH.renderOrientedState(renderState, translation, pipeline, Cuboid6.full, this.getFrontFacing(), true, true);
        }
    }

    //#fix# figure out how to add translations like with I18n instead of just english
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip, boolean advanced) {
        if (TooltipHelper.isShiftDown()) {
            tooltip.add("Screwdriver to cycle filter");
        } else {
            tooltip.add(I18n.format("gregtech.tooltip.tool_hold_shift"));
        }
    }

    public void setDetecting(boolean detected) {
        this.detecting = detected;
        this.UpdateRedstoneSignal();
    }

    public void UpdateRedstoneSignal() {
        this.setOutputRedstoneSignal(EnumFacing.byIndex(0), this.detecting ? 15 : 0);
        this.setOutputRedstoneSignal(EnumFacing.byIndex(1), this.detecting ? 15 : 0);
        this.setOutputRedstoneSignal(EnumFacing.byIndex(2), this.detecting ? 15 : 0);
        this.setOutputRedstoneSignal(EnumFacing.byIndex(3), this.detecting ? 15 : 0);
        this.setOutputRedstoneSignal(EnumFacing.byIndex(4), this.detecting ? 15 : 0);
        this.setOutputRedstoneSignal(EnumFacing.byIndex(5), this.detecting ? 15 : 0);
    }

    //#fix# what does this do
    public boolean showToolUsages() {
        return false;
    }

    //#fix# does the block need UI? how does modular UI work? do research.
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 384, 192)
                .label(10, 5, getMetaFullName());

        return builder.build(getHolder(), entityPlayer);
    }

    //#fix# does detected need to be saved or just refreshed on load? does ticks-alive need to be saved to prevent every one ticking at once?
    //update system based on chunk and global time instead of ticks alive?
    //should detection area be changeable and saved?
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("detected", this.detecting);
        data.setByte("filterIndex", filterIndex);
        return data;
    }

    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.detecting = data.getBoolean("detected");
        this.filterIndex = data.getByte("filterIndex");
    }

    //#fix# what does this do
    protected boolean shouldSerializeInventories() {
        return false;
    }

    public Vec3d getInteractionArea() {
        return this.detectionArea;
    }

    public void cycleFilter(boolean up) {
        this.filterIndex = StockHelperFunctions.CycleFilter(this.filterIndex, up);
        this.writeCustomData(PackIDFilterIndex, (buf) -> buf.writeByte(this.filterIndex));
    }

    public void cycleFilterUp() {
        this.cycleFilter(true);
    }

    public byte getFilterIndex() {
        return this.filterIndex;
    }

    public Class getFilter() { return StockHelperFunctions.ClassMap[this.getFilterIndex()]; }

    public boolean usingFilter() {
        return this.filterIndex != 0;
    }

    public MetaTileEntity GetMetaTileEntity() {
        return this;
    }

    protected boolean canMachineConnectRedstone(EnumFacing side) {
        return true;
    }
}
