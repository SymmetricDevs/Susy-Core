package supersymmetry.network;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MissingModsMessage implements IMessage {

    private List<String> missingModNames;

    public MissingModsMessage() {}

    public MissingModsMessage(List<String> missingModNames) {
        this.missingModNames = missingModNames;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(missingModNames.size());
        for (String name : missingModNames) {
            byte[] bytes = name.getBytes(StandardCharsets.UTF_8);
            buf.writeInt(bytes.length);
            buf.writeBytes(bytes);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int count = buf.readInt();
        missingModNames = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int len = buf.readInt();
            byte[] bytes = new byte[len];
            buf.readBytes(bytes);
            missingModNames.add(new String(bytes, StandardCharsets.UTF_8));
        }
    }

    public static class Handler implements IMessageHandler<MissingModsMessage, IMessage> {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(MissingModsMessage message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() ->
                    Minecraft.getMinecraft().displayGuiScreen(new GuiMissingMods(message.missingModNames))
            );
            return null;
        }
    }
}
