package supersymmetry.common.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import gregtech.api.network.IClientExecutor;
import gregtech.api.network.IPacket;
import supersymmetry.client.renderer.gun.GunClientHandler;

/**
 * Starts or stops a named gun animation on the client. The server only needs the clip name; the client looks up its
 * length and bone keyframes in the held gun's animation file.
 */
public class SPacketGunAnimation implements IPacket, IClientExecutor {

    private String animation = "";
    private boolean start;
    private int magazineSize;

    public SPacketGunAnimation() {}

    public SPacketGunAnimation(String animation, boolean start) {
        this(animation, start, 0);
    }

    /**
     * @param magazineSize capacity of the magazine the reload is inserting, or zero when the clip has no switch
     */
    public SPacketGunAnimation(String animation, boolean start, int magazineSize) {
        this.animation = animation;
        this.start = start;
        this.magazineSize = magazineSize;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void executeClient(NetHandlerPlayClient handler) {
        if (Minecraft.getMinecraft().player == null) {
            return;
        }
        if (start) {
            GunClientHandler.playAnimation(animation, magazineSize);
        } else {
            GunClientHandler.stopAnimation(animation);
        }
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeInt(animation.length());
        buf.writeString(animation);
        buf.writeBoolean(start);
        buf.writeInt(magazineSize);
    }

    @Override
    public void decode(PacketBuffer buf) {
        animation = buf.readString(buf.readInt());
        start = buf.readBoolean();
        magazineSize = buf.readInt();
    }
}
