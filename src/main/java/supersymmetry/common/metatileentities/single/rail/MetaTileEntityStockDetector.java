package supersymmetry.common.metatileentities.single.rail;

import codechicken.lib.colour.ColourRGBA;
import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import supersymmetry.api.stockinteraction.IStockInteractor;
import supersymmetry.client.renderer.textures.SusyTextures;

import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityStockDetector extends MetaTileEntity implements IStockInteractor
{
    public int ticksAlive;
    public boolean detected;

    private String filterFullName;
    private boolean usingFilter;
    private Vec3d detectionArea;

    public MetaTileEntityStockDetector(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        //this.detected = false;
        //this.ticksAlive = 0;
        //this.filterFullName = "";
        //this.usingFilter = false;
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityStockDetector(this.metaTileEntityId);
    }
    
    public int getLightOpacity() {
        return 1;
    }


    public int getActualComparatorValue() {
        return 1;
    }

    public boolean isOpaqueCube() {
        return true;
    }

    public String getHarvestTool() {
        return "pickaxe";
    }

    public boolean hasFrontFacing() {
        return true;
    }

    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeString(this.filterFullName == null ? "" : this.filterFullName);
        buf.writeByte(ToByte());
    }

    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.filterFullName = buf.readString(32767);

        FromByte(buf.readByte());
    }

    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == 123)
        {
            this.filterFullName = buf.readString(32767);
            byte stats = buf.readByte();
            UsingFilterFromByte(stats);
            this.scheduleRenderUpdate();
        }
        else if (dataId == 124)
        {
            byte stats = buf.readByte();
            UsingFilterFromByte(stats);
            this.scheduleRenderUpdate();
        }
        else if (dataId == 125)
        {
            byte stats = buf.readByte();
            DetectingFromByte(stats);
            this.scheduleRenderUpdate();
        }

    }

    public void update() {
        super.update();

        if(this.getWorld().isRemote)
            return;
    }


    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        if (!playerIn.getHeldItem(hand).hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, (EnumFacing)null)) {
            return false;
        } else {
            return false;
            //return this.getWorld().isRemote || !playerIn.isSneaking() && FluidUtil.interactWithFluidHandler(playerIn, hand, this.fluidTank);
        }
    }

    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing wrenchSide, CuboidRayTraceResult hitResult) {
        if (!playerIn.isSneaking()) {
            if (this.getWorld().isRemote) {
                this.scheduleRenderUpdate();
                return true;
            } else {
                this.toggleFilterUse(playerIn);
                return true;
            }
        } else {
            return super.onScrewdriverClick(playerIn, hand, wrenchSide, hitResult);
        }
    }

    private void toggleFilterUse(EntityPlayer playerIn) {
        if(!this.usingFilter && (this.filterFullName == null || this.filterFullName == ""))
        {
            playerIn.sendMessage(new TextComponentTranslation("could not set to use filter, no filter set"));
            //return;
        }

        this.usingFilter = !this.usingFilter;
        playerIn.sendMessage(new TextComponentTranslation("set filter use to " + this.usingFilter));

        //this.markDirty();
        if (!this.getWorld().isRemote) {
            //this.notifyBlockUpdate();
            this.writeCustomData(124, (buf) -> {
                buf.writeByte(ToByte());
            });
            markDirty();
        }

    }

    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        int color = ColourRGBA.multiply(GTUtility.convertRGBtoOpaqueRGBA_CL(0xFFFFFF), GTUtility.convertRGBtoOpaqueRGBA_CL(this.getPaintingColorForRendering()));
        color = GTUtility.convertOpaqueRGBA_CLtoRGB(color);
        return Pair.of(Textures.DRUM.getParticleTexture(), color);
    }

    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        ColourMultiplier multiplier;

        multiplier = new ColourMultiplier(ColourRGBA.multiply(GTUtility.convertRGBtoOpaqueRGBA_CL(0xFFFFFF), GTUtility.convertRGBtoOpaqueRGBA_CL(this.getPaintingColorForRendering())));
        //Textures.DRUM.render(renderState, translation, (IVertexOperation[])ArrayUtils.add(pipeline, multiplier), this.getFrontFacing());
        //SusyTextures.STOCK_DETECTOR.render(renderState, translation, pipeline, Cuboid6.full);//, getFrontFacing());
        //Textures.DRUM_OVERLAY.render(renderState, translation, pipeline);
        //Textures.MIXER_OVERLAY.renderOrientedState(renderState, translation, pipeline, Cuboid6.full, getFrontFacing(), true, true);
        //SusyTextures.STOCK_DETECTOR.renderSided();

        byte state = ToByte();

        switch(state)
        {
            case 0b00:
                SusyTextures.STOCK_DETECTOR.renderOrientedState(renderState, translation, pipeline, Cuboid6.full, getFrontFacing(), true, true);
                break;
            case 0b01:
                SusyTextures.STOCK_DETECTOR_DETECTING.renderOrientedState(renderState, translation, pipeline, Cuboid6.full, getFrontFacing(), true, true);
                break;
            case 0b10:
                SusyTextures.STOCK_DETECTOR_FILTER.renderOrientedState(renderState, translation, pipeline, Cuboid6.full, getFrontFacing(), true, true);
                break;
            case 0b11:
                SusyTextures.STOCK_DETECTOR_BOTH.renderOrientedState(renderState, translation, pipeline, Cuboid6.full, getFrontFacing(), true, true);
                break;
        }
    }

    public int getDefaultPaintingColor() {
        return 0xFFFFFF;
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        if (TooltipHelper.isShiftDown()) {
            tooltip.add(I18n.format("gregtech.fluid_pipe.max_temperature", new Object[]{340}));
            tooltip.add(I18n.format("gregtech.fluid_pipe.not_gas_proof", new Object[0]));


            tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers", new Object[0]));
            tooltip.add(I18n.format("gregtech.tool_action.screwdriver.auto_output_down", new Object[0]));
            tooltip.add(I18n.format("gregtech.tool_action.crowbar", new Object[0]));
        } else {
            tooltip.add(I18n.format("gregtech.tooltip.tool_fluid_hold_shift", new Object[0]));
        }

        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound != null && tagCompound.hasKey("Fluid", 10)) {
            FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(tagCompound.getCompoundTag("Fluid"));
            if (fluidStack == null) {
                return;
            }

            tooltip.add(I18n.format("gregtech.machine.fluid_tank.fluid", new Object[]{fluidStack.amount, I18n.format(fluidStack.getUnlocalizedName(), new Object[0])}));
        }

    }

    public byte ToByte()
    {
        byte state = 0b00;
        state |= (this.detected ? 0b01 : 0b00);
        state |= (this.usingFilter ? 0b10 : 0b00);

        return state;
    }

    public void FromByte(byte stats)
    {
        UsingFilterFromByte(stats);
        DetectingFromByte(stats);
    }

    public void UsingFilterFromByte(byte stats)
    {
        this.usingFilter = (stats & 0b10) > 0;
    }

    public void DetectingFromByte(byte stats)
    {
        this.detected = (stats & 0b01) > 0;
    }

    public boolean showToolUsages() {
        return false;
    }

    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 384, 192)
                .label(10, 5, getMetaFullName());


        return builder.build(getHolder(), entityPlayer);
    }

    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("detected", this.detected);
        data.setString("filterFullName", this.filterFullName == null ? "" : this.filterFullName);
        data.setBoolean("usingFilter", this.usingFilter);
        return data;
    }

    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.detected = data.getBoolean("detected");
        this.filterFullName = data.getString("filterFullName");
        this.usingFilter = data.getBoolean("usingFilter");
    }

    protected boolean shouldSerializeInventories() {
        return false;
    }

    public void SetInteractionArea(Vec3d area) {
        this.detectionArea = area;
    }

    public Vec3d GetInteractionArea() {
        return this.detectionArea;
    }

    public void SetFilterClass(String clazz) {
        this.filterFullName = clazz;
    }

    public String GetFilterClass() {
        return this.filterFullName;
    }

    public void SetUsingFilter(boolean usingFilter) {
        this.usingFilter = usingFilter;
    }

    public boolean GetUsingFilter() {
        return this.usingFilter;
    }

    public MetaTileEntity GetMetaTileEntity() {
        return this;
    }
}
