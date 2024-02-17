package supersymmetry.common.metatileentities.single.storage;

import codechicken.lib.colour.ColourRGBA;
import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.IPropertyFluidFilter;
import gregtech.api.capability.impl.FilteredFluidHandler;
import gregtech.api.capability.impl.GTFluidHandlerItemStack;
import gregtech.api.capability.impl.PropertyFluidFilter;
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
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

import static gregtech.api.capability.GregtechDataCodes.UPDATE_AUTO_OUTPUT;

public class MetaTileEntityCryoDrum extends MetaTileEntity {

    private final int tankSize;
    private FilteredFluidHandler fluidTank;
    private boolean isAutoOutput = false;

    private final int maxFluidTemp;
    private final boolean isAcidProof;
    private final boolean isPlasmaProof;

    private final int color;

    public MetaTileEntityCryoDrum(ResourceLocation metaTileEntityId, int tankSize, int maxFluidTemp, boolean isAcidProof,
                                  boolean isPlasmaProof, int color) {
        super(metaTileEntityId);
        this.tankSize = tankSize;
        this.maxFluidTemp = maxFluidTemp;
        this.isAcidProof = isAcidProof;
        this.isPlasmaProof = isPlasmaProof;
        this.color = color;
        this.initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityCryoDrum(this.metaTileEntityId, this.tankSize, this.maxFluidTemp, this.isAcidProof,
                this.isPlasmaProof, this.color);
    }

    @Override
    public int getLightOpacity() {
        return 1;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean hasFrontFacing() {
        return false;
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        this.fluidTank = new FilteredFluidHandler(this.tankSize)
                .setFilter(new PropertyFluidFilter(maxFluidTemp, true, isAcidProof, true, isPlasmaProof));
        this.fluidInventory = this.fluidTank;
    }

    @Override
    public void initFromItemStackData(NBTTagCompound itemStack) {
        super.initFromItemStackData(itemStack);
        if (itemStack.hasKey(FluidHandlerItemStack.FLUID_NBT_KEY, Constants.NBT.TAG_COMPOUND)) {
            FluidStack fluidStack = FluidStack
                    .loadFluidStackFromNBT(itemStack.getCompoundTag(FluidHandlerItemStack.FLUID_NBT_KEY));
            fluidTank.setFluid(fluidStack);
        }
    }

    @Override
    public void writeItemStackData(NBTTagCompound itemStack) {
        super.writeItemStackData(itemStack);
        FluidStack fluidStack = fluidTank.getFluid();
        if (fluidStack != null && fluidStack.amount > 0) {
            NBTTagCompound tagCompound = new NBTTagCompound();
            fluidStack.writeToNBT(tagCompound);
            itemStack.setTag(FluidHandlerItemStack.FLUID_NBT_KEY, tagCompound);
        }
    }

    @Override
    public ICapabilityProvider initItemStackCapabilities(ItemStack itemStack) {
        return new GTFluidHandlerItemStack(itemStack, tankSize).setFilter(this.fluidTank.getFilter());
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        FluidStack fluidStack = fluidTank.getFluid();
        buf.writeBoolean(fluidStack != null);
        if (fluidStack != null) {
            NBTTagCompound tagCompound = new NBTTagCompound();
            fluidStack.writeToNBT(tagCompound);
            buf.writeCompoundTag(tagCompound);
        }
        buf.writeBoolean(isAutoOutput);
    }

    @Override
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        FluidStack fluidStack = null;
        if (buf.readBoolean()) {
            try {
                NBTTagCompound tagCompound = buf.readCompoundTag();
                fluidStack = FluidStack.loadFluidStackFromNBT(tagCompound);
            } catch (IOException ignored) {
            }
        }
        fluidTank.setFluid(fluidStack);
        isAutoOutput = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == UPDATE_AUTO_OUTPUT) {
            this.isAutoOutput = buf.readBoolean();
            scheduleRenderUpdate();
        }
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            if (isAutoOutput && getOffsetTimer() % 5 == 0) {
                pushFluidsIntoNearbyHandlers(EnumFacing.DOWN);
            }
        }
    }

    @Override
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                CuboidRayTraceResult hitResult) {
        if (playerIn.getHeldItem(hand).hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            return getWorld().isRemote ||
                    (!playerIn.isSneaking() && FluidUtil.interactWithFluidHandler(playerIn, hand, fluidTank));
        }
        return super.onRightClick(playerIn, hand, facing, hitResult);
    }

    @Override
    public boolean onScrewdriverClick(@NotNull EntityPlayer playerIn, EnumHand hand, EnumFacing wrenchSide,
                                      CuboidRayTraceResult hitResult) {
        if (!playerIn.isSneaking()) {
            if (getWorld().isRemote) {
                scheduleRenderUpdate();
                return true;
            }
            playerIn.sendStatusMessage(new TextComponentTranslation(
                    "gregtech.machine.drum." + (isAutoOutput ? "disable" : "enable") + "_output"), true);
            toggleOutput();
            return true;
        }
        return super.onScrewdriverClick(playerIn, hand, wrenchSide, hitResult);
    }

    private void toggleOutput() {
        isAutoOutput = !isAutoOutput;
        if (!getWorld().isRemote) {
            notifyBlockUpdate();
            writeCustomData(UPDATE_AUTO_OUTPUT, buf -> buf.writeBoolean(isAutoOutput));
            markDirty();
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        int particleColor = ColourRGBA.multiply(
                GTUtility.convertRGBtoOpaqueRGBA_CL(this.color),
                GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()));
        particleColor = GTUtility.convertOpaqueRGBA_CLtoRGB(particleColor);
        return Pair.of(Textures.DRUM.getParticleTexture(), particleColor);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        ColourMultiplier multiplier = new ColourMultiplier(
                ColourRGBA.multiply(GTUtility.convertRGBtoOpaqueRGBA_CL(this.color),
                        GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering())));
        Textures.DRUM.render(renderState, translation, ArrayUtils.add(pipeline, multiplier), getFrontFacing());
        Textures.DRUM_OVERLAY.render(renderState, translation, pipeline);

        if (isAutoOutput) {
            Textures.STEAM_VENT_OVERLAY.renderSided(EnumFacing.DOWN, renderState, translation, pipeline);
        }
    }

    @Override
    public int getDefaultPaintingColor() {
        return 0xFFFFFF;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.universal.tooltip.fluid_storage_capacity", tankSize));

        // FluidPipeProperties#appendTooltips
        if (TooltipHelper.isShiftDown()) {
            tooltip.add(I18n.format("gregtech.fluid_pipe.max_temperature", maxFluidTemp));
            tooltip.add(I18n.format("gregtech.fluid_pipe.gas_proof"));
            if (isPlasmaProof) tooltip.add(I18n.format("gregtech.fluid_pipe.plasma_proof"));
            tooltip.add(I18n.format("gregtech.fluid_pipe.cryo_proof"));
            if (fluidTank.getFilter() instanceof IPropertyFluidFilter filter) {
                filter.getContainedAttributes().forEach(a -> a.appendContainerTooltips(tooltip));
            }
        } else if (fluidTank.getFilter() instanceof IPropertyFluidFilter filter && !filter.getContainedAttributes().isEmpty()) {
            tooltip.add(I18n.format("gregtech.tooltip.tool_fluid_hold_shift"));
        }

        if (TooltipHelper.isShiftDown()) {
            tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
            tooltip.add(I18n.format("gregtech.tool_action.screwdriver.auto_output_down"));
            tooltip.add(I18n.format("gregtech.tool_action.crowbar"));
        }

        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound != null && tagCompound.hasKey("Fluid", Constants.NBT.TAG_COMPOUND)) {
            FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(tagCompound.getCompoundTag("Fluid"));
            if (fluidStack == null) return;
            tooltip.add(I18n.format("gregtech.machine.fluid_tank.fluid", fluidStack.amount,
                    I18n.format(fluidStack.getUnlocalizedName())));
        }
    }

    // Override this so that we can control the "Hold SHIFT" tooltip manually
    @Override
    public boolean showToolUsages() {
        return false;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("FluidInventory", ((FluidTank) fluidInventory).writeToNBT(new NBTTagCompound()));
        data.setBoolean("AutoOutput", isAutoOutput);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        ((FluidTank) this.fluidInventory).readFromNBT(data.getCompoundTag("FluidInventory"));
        isAutoOutput = data.getBoolean("AutoOutput");
    }

    @Override
    protected boolean shouldSerializeInventories() {
        return false;
    }
}

