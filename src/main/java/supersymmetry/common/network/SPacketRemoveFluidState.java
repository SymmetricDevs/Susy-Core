package supersymmetry.common.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import gregtech.api.network.IClientExecutor;
import gregtech.api.network.IPacket;

public class SPacketRemoveFluidState implements IPacket, IClientExecutor {

    private BlockPos blockPosition;

    public SPacketRemoveFluidState() {}

    public SPacketRemoveFluidState(BlockPos pos) {
        this.blockPosition = pos;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void executeClient(NetHandlerPlayClient netHandlerPlayClient) {
        World world = Minecraft.getMinecraft().world;
        world.setBlockToAir(blockPosition);
    }

    @Override
    public void encode(PacketBuffer packetBuffer) {
        packetBuffer.writeBlockPos(blockPosition);
    }

    @Override
    public void decode(PacketBuffer packetBuffer) {
        this.blockPosition = packetBuffer.readBlockPos();
    }
}
