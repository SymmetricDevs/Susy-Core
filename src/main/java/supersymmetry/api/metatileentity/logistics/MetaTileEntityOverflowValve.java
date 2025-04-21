package supersymmetry.api.metatileentity.logistics;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.IControllable;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.RenderUtil;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/// An overflow valve has three special faces:
/// **input**, **output** (at the *exact opposite* side of the input face) and **overflow** face.
/// When receiving inputs from the input face, it will first try to output everything to the output face,
/// if it can't, it will try to output to the overflow face when the overflow is active.
///
/// Implements [IDelegator] to avoid recursive capability calls
public class MetaTileEntityOverflowValve extends MetaTileEntity implements IDelegator, IControllable {

    /// Decides if this is a fluid or item overflow valve
    private final boolean isFluidValve;
    /// Whether the overflow valve is active or not
    private boolean overflowActive = true;
    /// The side the overflow valve is facing
    private EnumFacing overflowFacing;

    public MetaTileEntityOverflowValve(ResourceLocation metaTileEntityId, boolean isFluidValve) {
        super(metaTileEntityId);
        this.isFluidValve = isFluidValve;
        if (this.isFluidValve) {
            this.fluidInventory = new OverflowFluidHandler();
        } else {
            this.itemInventory = new OverflowItemHandler();
        }
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityOverflowValve(this.metaTileEntityId, this.isFluidValve);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (this.isFluidValve && capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            if (side == getFrontFacing() || side == getOverflowFacing()) {
                return DefaultCapabilities.getCapability(capability);
            } else if (side == getFrontFacing().getOpposite()) {
                return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(this.fluidInventory);
            }
        } else if (!this.isFluidValve && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (side == getFrontFacing() || side == getOverflowFacing()) {
                return DefaultCapabilities.getCapability(capability);
            } else if (side == getFrontFacing().getOpposite()) {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this.itemInventory);
            }
        }
        return super.getCapability(capability, side);
    }

    @Override
    public boolean isValidFrontFacing(EnumFacing facing) {
        return facing != getFrontFacing()
                && facing != getOverflowFacing()
                && facing != getOverflowFacing().getOpposite();
    }

    @Override
    public void setFrontFacing(EnumFacing frontFacing) {
        super.setFrontFacing(frontFacing);
        /// This extra check is for first-time placement only
        /// In other cases [#isValidFrontFacing(EnumFacing)] will handle this
        if (this.getOverflowFacing() == frontFacing.getOpposite()) {
            this.setOverflowFacing(frontFacing.getAxis() == EnumFacing.Axis.Y ?
                    EnumFacing.NORTH : frontFacing.rotateY()); /// Special treatment for up or downwards
        }
    }

    public EnumFacing getOverflowFacing() {
        if (overflowFacing == null) {
            this.overflowFacing = EnumFacing.NORTH;
        }
        return overflowFacing;
    }

    public void setOverflowFacing(EnumFacing overflowFace) {
        this.overflowFacing = overflowFace;
        if (!this.getWorld().isRemote) {
            this.notifyBlockUpdate();
            this.writeCustomData(GregtechDataCodes.UPDATE_OUTPUT_FACING,
                    buf -> buf.writeByte(this.overflowFacing.getIndex()));
            this.markDirty();
        }
    }

    @Override
    public boolean onWrenchClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        if (!playerIn.isSneaking()) {
            if (this.getOverflowFacing() == facing) {
                return false; /// Do nothing if the overflow valve is already facing the clicked side
            } else if (facing == this.getFrontFacing() || facing == this.getFrontFacing().getOpposite()) {
                return false; /// Do nothing if the clicked side is invalid for overflow valve
            } else {
                if (!this.getWorld().isRemote) {
                    this.setOverflowFacing(facing);
                }
                return true;
            }
        } else {
            /// Rotate the actual front facing when sneaking
            return super.onWrenchClick(playerIn, hand, facing, hitResult);
        }
    }

    public boolean isOverflowActive() {
        return overflowActive;
    }

    public void setOverflowActive(boolean overflowActive) {
        this.overflowActive = overflowActive;
        if (!this.getWorld().isRemote) {
            this.writeCustomData(GregtechDataCodes.UPDATE_AUTO_OUTPUT_ITEMS,
                    buf -> buf.writeBoolean(overflowActive));
            this.markDirty();
        }

    }

    @Nullable
    protected <T> T getCapabilityAt(@NotNull Capability<T> capability, @NotNull EnumFacing side) {
        World world = getWorld();

        BlockPos target = getPos().offset(side);
        if (world.isOutsideBuildHeight(target)) return null;

        TileEntity te = getWorld().getTileEntity(target);
        if (te == null) return null;

        if (te instanceof IGregTechTileEntity gtTe && gtTe.getMetaTileEntity() instanceof IDelegator) return null;

        return te.getCapability(capability, side.getOpposite());
    }

    // TODO
    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        IVertexOperation[] colouredPipeline = ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(this.getPaintingColorForRendering())));
        for (EnumFacing facing : EnumFacing.values()) {
            Textures.renderFace(renderState, translation, colouredPipeline, facing, Cuboid6.full, this.getBaseTexture(), BlockRenderLayer.CUTOUT_MIPPED);
        }
        Textures.FLUID_OUTPUT_OVERLAY.renderSided(getFrontFacing(), renderState, RenderUtil.adjustTrans(translation, getFrontFacing(), 2), pipeline);
        Textures.ITEM_OUTPUT_OVERLAY.renderSided(getOverflowFacing(), renderState, RenderUtil.adjustTrans(translation, getOverflowFacing(), 2), pipeline);
    }

    @SideOnly(Side.CLIENT)
    protected TextureAtlasSprite getBaseTexture() {
        return Textures.PIPE_SIDE;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        addRecursiveWarning(stack, world, tooltip, advanced);
    }

    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(getBaseTexture(), getPaintingColorForRendering());
    }

    @Override
    public int getDefaultPaintingColor() {
        return Materials.Aluminium.getMaterialRGB();
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    @Nullable
    public EnumFacing getDelegatingFacing(EnumFacing facing) {
        return null; /// This is not actually a delegator so just return null for all faces
    }

    @Override
    public boolean needsSneakToRotate() {
        return true;
    }

    // TODO?
    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("OverflowFacing", this.getOverflowFacing().getIndex());
        data.setBoolean("OverflowActive", this.isOverflowActive());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.overflowFacing = EnumFacing.values()[data.getInteger("OverflowFacing")];
        this.overflowActive = data.getBoolean("OverflowActive");
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeByte(this.getOverflowFacing().getIndex());
        buf.writeBoolean(this.overflowActive);
    }

    @Override
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.overflowFacing = EnumFacing.VALUES[buf.readByte()];
        this.overflowActive = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.UPDATE_OUTPUT_FACING) {
            this.overflowFacing = EnumFacing.VALUES[buf.readByte()];
            this.scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.UPDATE_AUTO_OUTPUT_ITEMS) {
            this.overflowActive = buf.readBoolean();
            this.scheduleRenderUpdate();
        }
    }

    @Override
    public boolean isWorkingEnabled() {
        return isOverflowActive();
    }

    @Override
    public void setWorkingEnabled(boolean active) {
        setOverflowActive(active);
    }

    protected class OverflowItemHandler implements IItemHandler {

        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        @NotNull
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        @NotNull
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        @NotNull
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (stack.isEmpty()) return stack;

            stack = insertFirst(stack, simulate);
            if (stack.isEmpty() || !isOverflowActive()) return stack;

            return insertOverflow(stack, simulate);
        }

        protected ItemStack insertFirst(@NotNull ItemStack stack, boolean simulate) {
            IItemHandler handler = getCapabilityAt(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, getFrontFacing());
            return GTTransferUtils.insertItem(handler, stack, simulate);
        }

        protected ItemStack insertOverflow(@NotNull ItemStack stack, boolean simulate) {
            IItemHandler handler = getCapabilityAt(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, getOverflowFacing());
            return GTTransferUtils.insertItem(handler, stack, simulate);
        }
    }

    protected class OverflowFluidHandler extends FluidTank {

        public OverflowFluidHandler() {
            super(Integer.MAX_VALUE);
        }

        @Nullable
        @Override
        public FluidStack drainInternal(int maxDrain, boolean doDrain) {
            return null;
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            FluidStack stack = resource.copy();
            if (stack != null && stack.amount > 0) {
                int filled = fillFirst(stack, doFill);
                if (filled < stack.amount && isOverflowActive()) {
                    stack.amount -= filled;
                    return filled + fillOverflow(stack, doFill);
                }
                return filled;
            }
            return 0;
        }

        protected int fillFirst(FluidStack resource, boolean doFill) {
            IFluidHandler handler = getCapabilityAt(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, getFrontFacing());
            if (handler == null) return 0;
            return handler.fill(resource, doFill);
        }

        protected int fillOverflow(FluidStack resource, boolean doFill) {
            IFluidHandler handler = getCapabilityAt(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, getOverflowFacing());
            if (handler == null) return 0;
            return handler.fill(resource, doFill);
        }
    }
}
