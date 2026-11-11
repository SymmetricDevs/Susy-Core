package supersymmetry.common.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import gregtech.api.network.IClientExecutor;
import gregtech.api.network.IPacket;

public class SPacketGunRecoil implements IPacket, IClientExecutor {

    private float recoil;
    private boolean doHorizontal;

    public SPacketGunRecoil() {}

    public SPacketGunRecoil(float recoil, boolean doHorizontal) {
        this.recoil = recoil;
        this.doHorizontal = doHorizontal;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void executeClient(NetHandlerPlayClient handler) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player != null) {
            player.rotationPitch = Math.clamp(player.rotationPitch - recoil, -90.0f, 90.0f);
            if (doHorizontal) {
                player.rotationYaw += recoil * 0.4f * (player.getRNG().nextFloat() - 0.5f);
            }

        }
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeFloat(recoil);
        buf.writeBoolean(doHorizontal);
    }

    @Override
    public void decode(PacketBuffer buf) {
        recoil = buf.readFloat();
        doHorizontal = buf.readBoolean();
    }
}
