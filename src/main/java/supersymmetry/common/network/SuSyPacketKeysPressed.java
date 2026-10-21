package supersymmetry.common.network;

import java.util.List;

import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

import org.apache.commons.lang3.tuple.Pair;

import gregtech.api.network.IPacket;
import gregtech.api.network.IServerExecutor;
import supersymmetry.api.input.SuSyKeyBind;

public class SuSyPacketKeysPressed implements IPacket, IServerExecutor {

    private Object updateKeys;

    @SuppressWarnings("unused")
    public SuSyPacketKeysPressed() {}

    public SuSyPacketKeysPressed(List<SuSyKeyBind> updateKeys) {
        this.updateKeys = updateKeys;
    }

    @Override
    public void encode(PacketBuffer buf) {
        List<SuSyKeyBind> updateKeys = (List<SuSyKeyBind>) this.updateKeys;
        buf.writeVarInt(updateKeys.size());
        for (SuSyKeyBind keyBind : updateKeys) {
            buf.writeVarInt(keyBind.ordinal());
            buf.writeBoolean(keyBind.isPressed());
            buf.writeBoolean(keyBind.isKeyDown());
        }
    }

    @Override
    public void decode(PacketBuffer buf) {
        this.updateKeys = new Pair[SuSyKeyBind.VALUES.length];
        Pair<Boolean, Boolean>[] updateKeys = (Pair<Boolean, Boolean>[]) this.updateKeys;
        int size = buf.readVarInt();
        for (int i = 0; i < size; i++) {
            updateKeys[buf.readVarInt()] = Pair.of(buf.readBoolean(), buf.readBoolean());
        }
    }

    @Override
    public void executeServer(NetHandlerPlayServer handler) {
        SuSyKeyBind[] keybinds = SuSyKeyBind.VALUES;
        Pair<Boolean, Boolean>[] updateKeys = (Pair<Boolean, Boolean>[]) this.updateKeys;
        for (int i = 0; i < updateKeys.length; i++) {
            Pair<Boolean, Boolean> pair = updateKeys[i];
            if (pair != null) {
                keybinds[i].update(pair.getLeft(), pair.getRight(), handler.player);
            }
        }
    }
}
