package supersymmetry.common.network;

import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

import gregtech.api.network.IPacket;
import gregtech.api.network.IServerExecutor;
import supersymmetry.common.entities.EntityAbstractRocket;

public class CPacketRocketLaunch implements IPacket, IServerExecutor {

    public int rocketID;

    public CPacketRocketLaunch() {}

    public CPacketRocketLaunch(EntityAbstractRocket rocket) {
        this.rocketID = rocket.getEntityId();
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeInt(this.rocketID);
    }

    @Override
    public void decode(PacketBuffer buf) {
        rocketID = buf.readInt();
    }

    @Override
    public void executeServer(NetHandlerPlayServer handler) {
        EntityAbstractRocket rocket = (EntityAbstractRocket) handler.player.getEntityWorld().getEntityByID(rocketID);
        if (rocket != null && rocket.isEntityAlive()) {
            rocket.startCountdown(200);
        }
    }
}
