package supersymmetry.common.network;

import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumHand;

import gregtech.api.network.IPacket;
import gregtech.api.network.IServerExecutor;
import supersymmetry.common.guns.server.GunServerHandler;

public class CPacketGunInput implements IPacket, IServerExecutor {

    private boolean trigger;
    private boolean aim;
    private boolean reload;
    private boolean cycleMode;
    private boolean unload;
    private boolean openAttachments;
    private EnumHand hand = EnumHand.MAIN_HAND;

    public CPacketGunInput() {}

    public CPacketGunInput(boolean trigger, boolean aim, boolean reload, boolean cycleMode, boolean unload,
                           EnumHand hand, boolean openAttachments) {
        this.trigger = trigger;
        this.aim = aim;
        this.reload = reload;
        this.cycleMode = cycleMode;
        this.unload = unload;
        this.hand = hand;
        this.openAttachments = openAttachments;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeBoolean(trigger);
        buf.writeBoolean(aim);
        buf.writeBoolean(reload);
        buf.writeBoolean(cycleMode);
        buf.writeBoolean(unload);
        buf.writeByte(hand.ordinal());
        buf.writeBoolean(openAttachments);
    }

    @Override
    public void decode(PacketBuffer buf) {
        trigger = buf.readBoolean();
        aim = buf.readBoolean();
        reload = buf.readBoolean();
        cycleMode = buf.readBoolean();
        unload = buf.readBoolean();

        int handId = buf.readUnsignedByte();
        EnumHand[] hands = EnumHand.values();
        hand = handId < hands.length ? hands[handId] : EnumHand.MAIN_HAND;
        openAttachments = buf.readBoolean();
    }

    @Override
    public void executeServer(NetHandlerPlayServer handler) {
        GunServerHandler.onInput(handler.player, trigger, aim, reload, cycleMode, unload, openAttachments, hand);
    }
}
