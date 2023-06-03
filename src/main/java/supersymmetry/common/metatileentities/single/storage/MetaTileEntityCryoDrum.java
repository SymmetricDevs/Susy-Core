package supersymmetry.common.metatileentities.single.storage;

import codechicken.lib.colour.ColourRGBA;
import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.impl.FilteredFluidHandler;
import gregtech.api.capability.impl.ThermalFluidHandlerItemStack;
import gregtech.api.fluids.MaterialFluid;
import gregtech.api.fluids.fluidType.FluidType;
import gregtech.api.fluids.fluidType.FluidTypes;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.unification.material.Material;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import supersymmetry.client.renderer.textures.SusyTextures;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

public class MetaTileEntityCryoDrum extends MetaTileEntity {

    private final int tankSize;
    private final Material material;
    private FilteredFluidHandler fluidTank;
    private boolean isAutoOutput = false;

    private final boolean isAcidProof;
    private final boolean isPlasmaProof;

    int maxFluidTemp;

    public MetaTileEntityCryoDrum(ResourceLocation metaTileEntityId, Material material, int tankSize, int maxFluidTemp, boolean isAcidProof, boolean isPlasmaProof) {
        super(metaTileEntityId);
        this.material = material;
        this.tankSize = tankSize;
        this.maxFluidTemp = maxFluidTemp;
        this.isAcidProof = isAcidProof;
        this.isPlasmaProof = isPlasmaProof;
        this.initializeInventory();
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityCryoDrum(this.metaTileEntityId, this.material, this.tankSize, this.maxFluidTemp, this.isAcidProof, this.isPlasmaProof);
    }

    public int getLightOpacity() {
        return 1;
    }

    public int getActualComparatorValue() {
        FluidTank fluidTank = this.fluidTank;
        int fluidAmount = fluidTank.getFluidAmount();
        int maxCapacity = fluidTank.getCapacity();
        float f = (float)fluidAmount / ((float)maxCapacity * 1.0F);
        return MathHelper.floor(f * 14.0F) + (fluidAmount > 0 ? 1 : 0);
    }

    public boolean isOpaqueCube() {
        return false;
    }

    public String getHarvestTool() {
        return "wrench";
    }

    public boolean hasFrontFacing() {
        return true;
    }

    protected void initializeInventory() {
        super.initializeInventory();
        this.fluidTank = (new FilteredFluidHandler(this.tankSize)).setFillPredicate((stack) -> {
            if (stack != null && stack.getFluid() != null) {
                Fluid fluid = stack.getFluid();
                FluidType fluidType;

                //I want to use materials for these drums that do not have fluid pipe properties, so these cases are handled with bools directly
                //Additionally, if something is a cryo drum its assumed that it is gas and cryo proof
                if (fluid.getTemperature() > maxFluidTemp) return false;
                else {
                    if (fluid instanceof MaterialFluid) {
                        fluidType = ((MaterialFluid)fluid).getFluidType();
                        if (fluidType == FluidTypes.ACID && !this.isAcidProof) return false;
                        if (fluidType == FluidTypes.PLASMA && !this.isPlasmaProof) return false;
                    }

                    return true;
                }

            } else {
                return false;
            }
        });

        this.fluidInventory = this.fluidTank;
    }

    public void initFromItemStackData(NBTTagCompound itemStack) {
        super.initFromItemStackData(itemStack);
        if (itemStack.hasKey("Fluid", 10)) {
            FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(itemStack.getCompoundTag("Fluid"));
            this.fluidTank.setFluid(fluidStack);
        }

    }

    public void writeItemStackData(NBTTagCompound itemStack) {
        super.writeItemStackData(itemStack);
        FluidStack fluidStack = this.fluidTank.getFluid();
        if (fluidStack != null && fluidStack.amount > 0) {
            NBTTagCompound tagCompound = new NBTTagCompound();
            fluidStack.writeToNBT(tagCompound);
            itemStack.setTag("Fluid", tagCompound);
        }

    }

    public ICapabilityProvider initItemStackCapabilities(ItemStack itemStack) {
        return new ThermalFluidHandlerItemStack(itemStack, this.tankSize, this.maxFluidTemp, true, this.isAcidProof, true, this.isPlasmaProof);
    }

    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        FluidStack fluidStack = this.fluidTank.getFluid();
        buf.writeBoolean(fluidStack != null);
        if (fluidStack != null) {
            NBTTagCompound tagCompound = new NBTTagCompound();
            fluidStack.writeToNBT(tagCompound);
            buf.writeCompoundTag(tagCompound);
        }

        buf.writeBoolean(this.isAutoOutput);
    }

    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        FluidStack fluidStack = null;
        if (buf.readBoolean()) {
            try {
                NBTTagCompound tagCompound = buf.readCompoundTag();
                fluidStack = FluidStack.loadFluidStackFromNBT(tagCompound);
            } catch (IOException var4) {
            }
        }

        this.fluidTank.setFluid(fluidStack);
        this.isAutoOutput = buf.readBoolean();
    }

    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == 560) {
            this.isAutoOutput = buf.readBoolean();
            this.scheduleRenderUpdate();
        }

    }

    public void update() {
        super.update();
        if (!this.getWorld().isRemote && this.isAutoOutput && this.getOffsetTimer() % 5L == 0L) {
            this.pushFluidsIntoNearbyHandlers(new EnumFacing[]{EnumFacing.DOWN});
        }

    }

    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        if (!playerIn.getHeldItem(hand).hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, (EnumFacing)null)) {
            return false;
        } else {
            return this.getWorld().isRemote || !playerIn.isSneaking() && FluidUtil.interactWithFluidHandler(playerIn, hand, this.fluidTank);
        }
    }

    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing wrenchSide, CuboidRayTraceResult hitResult) {
        if (!playerIn.isSneaking()) {
            if (this.getWorld().isRemote) {
                this.scheduleRenderUpdate();
                return true;
            } else {
                playerIn.sendMessage(new TextComponentTranslation("gregtech.machine.drum." + (this.isAutoOutput ? "disable" : "enable") + "_output", new Object[0]));
                this.toggleOutput();
                return true;
            }
        } else {
            return super.onScrewdriverClick(playerIn, hand, wrenchSide, hitResult);
        }
    }

    private void toggleOutput() {
        this.isAutoOutput = !this.isAutoOutput;
        if (!this.getWorld().isRemote) {
            this.notifyBlockUpdate();
            this.writeCustomData(560, (buf) -> {
                buf.writeBoolean(this.isAutoOutput);
            });
            this.markDirty();
        }

    }

    //needed to change this as well, but wasn't worth digging enough to have it not just use brass's color
    @Override @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        int color = ColourRGBA.multiply(GTUtility.convertRGBtoOpaqueRGBA_CL(this.material.getMaterialRGB()), GTUtility.convertRGBtoOpaqueRGBA_CL(this.getPaintingColorForRendering()));
        color = GTUtility.convertOpaqueRGBA_CLtoRGB(color);
        return Pair.of(SusyTextures.BRASS_DRUM.getParticleTexture(), color);
    }

    //I wanted a custom colored texture
    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        SusyTextures.BRASS_DRUM.render(renderState, translation, pipeline, this.getFrontFacing());
        SusyTextures.BRASS_DRUM_OVERLAY.renderOrientedState(renderState, translation, pipeline, this.getFrontFacing(), false, false);

        if (this.isAutoOutput) {
            Textures.STEAM_VENT_OVERLAY.renderSided(EnumFacing.DOWN, renderState, translation, pipeline);
        }

    }

    public int getDefaultPaintingColor() {
        return 16777215;
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.universal.tooltip.fluid_storage_capacity", this.tankSize));
        if (TooltipHelper.isShiftDown()) {

            //always gas and cryo proof, so no condition needs to be checked
            tooltip.add(I18n.format("gregtech.fluid_pipe.max_temperature", this.maxFluidTemp));
            tooltip.add(I18n.format("gregtech.fluid_pipe.gas_proof"));

            if (this.isAcidProof)
                tooltip.add(I18n.format("gregtech.fluid_pipe.acid_proof"));

            tooltip.add(I18n.format("gregtech.fluid_pipe.cryo_proof"));

            if (this.isPlasmaProof)
                tooltip.add(I18n.format("gregtech.fluid_pipe.plasma_proof"));

            tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
            tooltip.add(I18n.format("gregtech.tool_action.screwdriver.auto_output_down"));
            tooltip.add(I18n.format("gregtech.tool_action.crowbar"));
        } else {
            tooltip.add(I18n.format("gregtech.tooltip.tool_fluid_hold_shift"));
        }

        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound != null && tagCompound.hasKey("Fluid", 10)) {
            FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(tagCompound.getCompoundTag("Fluid"));
            if (fluidStack == null) {
                return;
            }

            tooltip.add(I18n.format("gregtech.machine.fluid_tank.fluid", fluidStack.amount, I18n.format(fluidStack.getUnlocalizedName())));
        }

    }

    public boolean showToolUsages() {
        return false;
    }

    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("FluidInventory", ((FluidTank)this.fluidInventory).writeToNBT(new NBTTagCompound()));
        data.setBoolean("AutoOutput", this.isAutoOutput);
        return data;
    }

    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        ((FluidTank)this.fluidInventory).readFromNBT(data.getCompoundTag("FluidInventory"));
        this.isAutoOutput = data.getBoolean("AutoOutput");
    }

    protected boolean shouldSerializeInventories() {
        return false;
    }
}

