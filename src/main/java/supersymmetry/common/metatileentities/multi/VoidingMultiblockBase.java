package supersymmetry.common.metatileentities.multi;

import static gregtech.api.capability.GregtechDataCodes.WORKING_ENABLED;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.fluids.FluidState;
import gregtech.api.fluids.GTFluid;
import gregtech.api.fluids.attribute.AttributedFluid;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialFlags;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;

public abstract class VoidingMultiblockBase extends MultiblockWithDisplayBase implements IControllable {

    // Update this value based on your needs
    // For instance, if you want your glorified trashcan to be tiered
    public int rateBonus = 1;
    // Amount of ticks between voiding
    public final int voidingFrequency = 10;
    public boolean active = false;
    protected boolean workingEnabled = true;

    public Object2BooleanOpenHashMap<Fluid> fluidCache = new Object2BooleanOpenHashMap<>();

    public VoidingMultiblockBase(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    protected void updateFormedValid() {
        if (this.getWorld().isRemote) return;
        if (getOffsetTimer() % voidingFrequency == 0) {
            this.active = false;
            if (!this.workingEnabled) return;
            for (IFluidTank tank : getAbilities(MultiblockAbility.IMPORT_FLUIDS)) {
                FluidStack fs = tank.getFluid();
                if (fs != null) {
                    Fluid fluid = fs.getFluid();
                    boolean voidable = fluidCache.computeIfAbsent(fluid, this::canVoid);
                    // TODO: Cache this?
                    if (voidable) {
                        tank.drain(this.getActualVoidingRate(), true);
                        this.active = true;
                    }
                }
            }
        }
    }

    public boolean canVoid(Fluid fluid) {
        if (fluid instanceof AttributedFluid attributedFluid) {
            FluidState state = attributedFluid.getState();
            if (fluid instanceof GTFluid.GTMaterialFluid gtFluid) {
                Material mat = gtFluid.getMaterial();
                return canVoidState(state) && (!incinerate() ^ mat.hasFlag(MaterialFlags.FLAMMABLE));
            }
            return canVoidState(state);
        }
        return false;
    }

    public boolean canVoidState(FluidState state) {
        return false;
    }

    private int getActualVoidingRate() {
        return rateBonus * getBaseVoidingRate();
    }

    public int getBaseVoidingRate() {
        return 1000;
    }

    // Whether to void flammable fluids or not
    public boolean incinerate() {
        return false;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(),
                this.isActive(), true);
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.IS_WORKING) {
            this.active = this.lastActive;
        } else if (dataId == WORKING_ENABLED) {
            this.workingEnabled = buf.readBoolean();
            scheduleRenderUpdate();
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(active);
        buf.writeBoolean(workingEnabled);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.active = buf.readBoolean();
        this.workingEnabled = buf.readBoolean();
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.active = false;
        this.writeCustomData(GregtechDataCodes.IS_WORKING, (buf -> buf.writeBoolean(false)));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setBoolean("workingEnabled", this.workingEnabled);
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.workingEnabled = data.getBoolean("workingEnabled");
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return false;
    }

    @Override
    public boolean getIsWeatherOrTerrainResistant() {
        return true;
    }

    @Override
    public boolean isWorkingEnabled() {
        return workingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean enabled) {
        this.workingEnabled = enabled;
        this.writeCustomData(WORKING_ENABLED, buf -> buf.writeBoolean(enabled));
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE)
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        return super.getCapability(capability, side);
    }
}
