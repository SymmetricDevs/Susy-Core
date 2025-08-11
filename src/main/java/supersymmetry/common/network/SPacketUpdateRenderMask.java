package supersymmetry.common.network;

import gregtech.api.network.IClientExecutor;
import gregtech.api.network.IPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;
import supersymmetry.api.util.RenderMaskManager;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collection;

@ParametersAreNonnullByDefault
public class SPacketUpdateRenderMask implements IPacket, IClientExecutor {

    private boolean disable;
    private int dimId;
    private BlockPos controllerPos;
    @Nullable
    private Collection<BlockPos> poses;

    @SuppressWarnings("unused")
    public SPacketUpdateRenderMask() {
        /* Needed for client side */
    }

    public SPacketUpdateRenderMask(BlockPos controllerPos, @Nullable Collection<BlockPos> poses, int dimId) {
        this.disable = poses != null;
        this.dimId = dimId;
        this.controllerPos = controllerPos;
        this.poses = poses;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void executeClient(NetHandlerPlayClient handler) {
        boolean updateRendering = Minecraft.getMinecraft().world.provider.getDimension() == dimId;
        if (disable) {
            RenderMaskManager.addDisableModel(controllerPos, poses, updateRendering);
        } else {
            RenderMaskManager.removeDisableModel(controllerPos, updateRendering);
        }
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeBoolean(disable);
        buf.writeInt(dimId);
        buf.writeBlockPos(controllerPos);
        if (disable && poses != null) {
            buf.writeInt(poses.size());
            poses.forEach(buf::writeBlockPos);
        }
    }

    @Override
    public void decode(PacketBuffer buf) {
        this.disable = buf.readBoolean();
        this.dimId = buf.readInt();
        this.controllerPos = buf.readBlockPos();
        if (disable) {
            int size = buf.readInt();
            this.poses = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                poses.add(buf.readBlockPos());
            }
        }
    }
}
