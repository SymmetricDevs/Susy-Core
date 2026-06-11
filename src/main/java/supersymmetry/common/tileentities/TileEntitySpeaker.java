package supersymmetry.common.tileentities;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

import gregtech.api.GregTechAPI;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.SimpleComponent;
import supersymmetry.common.blocks.BlockSpeaker;
import supersymmetry.common.network.SPacketSpeakerAudio;

public class TileEntitySpeaker extends TileEntity implements SimpleComponent {

    public BlockSpeaker.BlockSpeakerType type;

    public TileEntitySpeaker() {
        super();
    }

    public TileEntitySpeaker(BlockSpeaker.BlockSpeakerType type) {
        this();
        this.type = type;
    }

    @Override
    public String getComponentName() {
        return String.format("speaker_%s", type.name().toLowerCase());
    }

    @Callback(doc = "playSound(rate:int,data:string) -- plays a sound through this speaker")
    public Object[] playSound(Context ctx, Arguments args) {
        var datastring = args.checkString(1);
        var data = datastring.getBytes();
        var rate = args.checkInteger(0);
        if (rate < 0 || rate > 41100) {
            throw new IllegalArgumentException("invalid rate");
        }
        if (data.length >= 41100) {
            throw new IllegalArgumentException("too much data");
        }

        var node = ((Environment) this).node();

        GregTechAPI.networkHandler.sendToAllAround(
                new SPacketSpeakerAudio("goog", rate, this.getPos(), data),
                new TargetPoint(
                        this.getWorld().provider.getDimension(),
                        this.getPos().getX(),
                        this.getPos().getY(),
                        this.getPos().getZ(),
                        this.type.getVolume()));

        return new Object[] { "goog" };
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if (nbt.hasKey("type")) {
            type = BlockSpeaker.BlockSpeakerType.valueOf(nbt.getString("type"));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        if (type != null) {
            nbt.setString("type", type.name());
        }
        return nbt;
    }
}
