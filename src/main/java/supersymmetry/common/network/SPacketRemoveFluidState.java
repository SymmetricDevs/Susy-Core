package supersymmetry.common.network;

import git.jbredwards.fluidlogged_api.api.util.FluidState;
import git.jbredwards.fluidlogged_api.api.util.FluidloggedUtils;
import gregtech.api.network.IClientExecutor;
import gregtech.api.network.IPacket;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SPacketRemoveFluidState implements IPacket, IClientExecutor {
    private BlockPos blockPosition;

    public SPacketRemoveFluidState() {
    }

    public SPacketRemoveFluidState(BlockPos pos) {
        this.blockPosition = pos;
    }

    @Override
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
