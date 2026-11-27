package supersymmetry.common.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import gregtech.api.network.IClientExecutor;
import gregtech.api.network.IPacket;
import supersymmetry.client.renderer.gun.GunClientHandler;

public class SPacketGunRecoil implements IPacket, IClientExecutor {

    private float recoil;
    private float horizontalModifier;
    private boolean fired;

    public SPacketGunRecoil() {}

    public SPacketGunRecoil(float recoil, float horizontalModifier) {
        this(recoil, horizontalModifier, false);
    }

    public SPacketGunRecoil(float recoil, float horizontalModifier, boolean fired) {
        this.recoil = recoil;
        this.horizontalModifier = horizontalModifier;
        this.fired = fired;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void executeClient(NetHandlerPlayClient handler) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player != null) {
            player.rotationPitch = Math.clamp(player.rotationPitch - recoil, -90.0f, 90.0f);
            if (horizontalModifier > 0) {
                player.rotationYaw += recoil * 0.4f * (player.getRNG().nextFloat() - 0.5f);
            }
            if (fired) {
                GunClientHandler.onGunFired();
            }
        }
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeFloat(recoil);
        buf.writeFloat(horizontalModifier);
        buf.writeBoolean(fired);
    }

    @Override
    public void decode(PacketBuffer buf) {
        recoil = buf.readFloat();
        horizontalModifier = buf.readFloat();
        fired = buf.readBoolean();
    }
}
