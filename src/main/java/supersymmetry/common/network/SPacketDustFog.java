package supersymmetry.common.network;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import gregtech.api.network.IClientExecutor;
import gregtech.api.network.IPacket;
import supersymmetry.client.renderer.handler.DustFogRenderer;

@ParametersAreNonnullByDefault
public class SPacketDustFog implements IPacket, IClientExecutor {

    private float fogStrength;

    @SuppressWarnings("unused")
    public SPacketDustFog() {
        // Required
    }

    public SPacketDustFog(float fogStrength) {
        this.fogStrength = fogStrength;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void executeClient(NetHandlerPlayClient handler) {
        Minecraft mc = Minecraft.getMinecraft();

        if (mc.player == null || mc.world == null)
            return;

        DustFogRenderer.applyPacket();
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeFloat(fogStrength);
    }

    @Override
    public void decode(PacketBuffer buf) {
        fogStrength = buf.readFloat();
    }
}
