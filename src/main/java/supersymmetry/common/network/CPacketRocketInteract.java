package supersymmetry.common.network;

import gregtech.api.network.IPacket;
import gregtech.api.network.IServerExecutor;
import net.minecraft.entity.Entity;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * A variation on CPacketUseEntity for rockets which skips the distance check.
 */
public class CPacketRocketInteract implements IPacket, IServerExecutor {
    private int entityId;
    private Vec3d hitVec;
    private EnumHand hand;

    public CPacketRocketInteract() {
    }

    @SideOnly(Side.CLIENT)
    public CPacketRocketInteract(Entity entityIn, EnumHand handIn, Vec3d hitVecIn) {
        this.entityId = entityIn.getEntityId();
        this.hand = handIn;
        this.hitVec = hitVecIn;
    }

    @Override
    public void executeServer(NetHandlerPlayServer handler) {
        Entity entity = handler.player.getEntityWorld().getEntityByID(this.entityId);
        if (entity == null) {
            return;
        }
        if (handler.player.getDistanceSq(entity) < 1600) { // Much more reasonable :trollface:
            if (net.minecraftforge.common.ForgeHooks.onInteractEntityAt(handler.player, entity, hitVec, hand) != null)
                return;
            entity.applyPlayerInteraction(handler.player, hitVec, hand);
        }
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeVarInt(this.entityId);

        buf.writeFloat((float) this.hitVec.x);
        buf.writeFloat((float) this.hitVec.y);
        buf.writeFloat((float) this.hitVec.z);

        buf.writeEnumValue(this.hand);
    }

    @Override
    public void decode(PacketBuffer buf) {
        this.entityId = buf.readVarInt();

        this.hitVec = new Vec3d(buf.readFloat(), buf.readFloat(), buf.readFloat());

        this.hand = buf.readEnumValue(EnumHand.class);
    }

    @Nullable
    public Entity getEntityFromWorld(World worldIn) {
        return worldIn.getEntityByID(this.entityId);
    }
}
