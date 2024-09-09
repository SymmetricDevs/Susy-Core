package supersymmetry.mixins.gregtech;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.metatileentity.NeighborCacheTileEntityBase;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import supersymmetry.api.blocks.IForcedStates;
import supersymmetry.common.blocks.BlockSheetedFrame;

@Mixin(value = TileEntityPipeBase.class, remap = false)
public abstract class TileEntityPipeBaseMixin<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>, NodeDataType> extends NeighborCacheTileEntityBase implements IPipeTile<PipeType, NodeDataType>, IForcedStates {
    int rotationState = 0;

    @Shadow
    private TileEntityPipeBase<PipeType, NodeDataType> tickingPipe;

    public void setForcedState(int val) {
        rotationState = val;
        if (world != null && world.isRemote) {
            this.writeCustomData(GregtechDataCodes.UPDATE_FRAME_MATERIAL, (buf) -> buf.writeVarInt(val));
        }
    }

    public int getForcedState() {
        return rotationState;
    }

    @Inject(method = "setConnection(Lnet/minecraft/util/EnumFacing;ZZ)V", at = @At("HEAD"), cancellable = true)
    public void onSetConnection(EnumFacing side, boolean connected, boolean fromNeighbor, CallbackInfo callbackInfo) {
        int rotationOrdinal = ((IForcedStates) this).getForcedState() - 1;
        if (this.getWorld().isRemote || rotationOrdinal < 0) return;

        // cancel if connection is attempted on non axis sides for rotations that are not omnidirectional
        if (rotationOrdinal != BlockSheetedFrame.FrameEnumAxis.NONE.ordinal() && rotationOrdinal != side.getAxis().ordinal() && connected) {
            callbackInfo.cancel();
        }
    }

    // (DIIIZ)Lcom/mypackage/ThingType;
    @Inject(method = "transferDataFrom(Lgregtech/api/pipenet/tile/IPipeTile;)V", at = @At("TAIL"))
    public void onTransferDataFrom(IPipeTile<PipeType, NodeDataType> tileEntity, CallbackInfo callbackInfo) {
        setForcedState(((IForcedStates) tileEntity).getForcedState()); // this method should only ever be called with TE instance of this mixin's target
    }

    @Inject(method = "writeToNBT(Lnet/minecraft/nbt/NBTTagCompound;)Lnet/minecraft/nbt/NBTTagCompound;", at = @At("TAIL"))
    public void onWriteToNBT(@NotNull NBTTagCompound compound, CallbackInfoReturnable<NBTTagCompound> callbackInfoR) {
        compound.setInteger("rotationState", getForcedState());
    }

    @Inject(method = "readFromNBT(Lnet/minecraft/nbt/NBTTagCompound;)V", at = @At("HEAD"))
    public void onReadFromNBT(@NotNull NBTTagCompound compound, CallbackInfo callbackInfo) {
        // actual read from is only called when tickingPipe is null
        if (tickingPipe == null) {
            setForcedState(compound.getInteger("rotationState"));
        }
    }

    @Inject(method = "writeInitialSyncData(Lnet/minecraft/network/PacketBuffer;)V", at = @At("HEAD"))
    public void onWriteInitialSyncData(PacketBuffer buf, CallbackInfo callbackInfo) {
        buf.writeInt(getForcedState());
    }

    @Inject(method = "receiveInitialSyncData(Lnet/minecraft/network/PacketBuffer;)V", at = @At("HEAD"))
    public void onReceiveInitialSyncData(PacketBuffer buf, CallbackInfo callbackInfo) {
        // actual receive only called when tickingPipe is null
        if (tickingPipe == null) {
            setForcedState(buf.readInt());
        }
    }

    @Inject(method = "receiveCustomData(ILnet/minecraft/network/PacketBuffer;)V", at = @At("HEAD"))
    public void onReceiveCustomData(int discriminator, PacketBuffer buf, CallbackInfo callbackInfo) {
        if (tickingPipe == null && discriminator == BlockSheetedFrame.UPDATE_ROTATION_STATE) {
            rotationState = buf.readInt();

            this.scheduleChunkForRenderUpdate();
        }
    }
}
