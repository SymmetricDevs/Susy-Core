package supersymmetry.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import io.netty.buffer.ByteBuf;
import supersymmetry.common.item.ItemToolbelt;

/**
 * Carries a toolbelt slot selection between client and server.
 * <p>
 * Ported from GTCEu-Reloaded 43140b63f (LGPL-3.0), using this project's network channel
 * instead of GT's. {@code index} is the container slot holding the belt, so the receiving
 * side can find the same stack the sender meant.
 */
public class PacketToolbeltSelectionChange implements IMessage {

    private int slot;
    private int index;

    public PacketToolbeltSelectionChange() {}

    public PacketToolbeltSelectionChange(int slot, int index) {
        this.slot = slot;
        this.index = index;
    }

    public static void toClient(int slot, int index, EntityPlayerMP player) {
        if (player == null) return;
        SuSyNetwork.CHANNEL.sendTo(new PacketToolbeltSelectionChange(slot, index), player);
    }

    public static void toServer(int slot, int index) {
        SuSyNetwork.CHANNEL.sendToServer(new PacketToolbeltSelectionChange(slot, index));
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.slot = buf.readByte();
        this.index = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(slot);
        buf.writeInt(index);
    }

    public static class Handler implements IMessageHandler<PacketToolbeltSelectionChange, IMessage> {

        @Override
        public IMessage onMessage(PacketToolbeltSelectionChange message, MessageContext ctx) {
            if (ctx.side == Side.CLIENT) {
                handleClient(message);
            } else {
                handleServer(message, ctx);
            }
            return null;
        }

        private void handleClient(PacketToolbeltSelectionChange message) {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            if (player == null || player.openContainer == null) return;
            if (message.index < 0 || message.index >= player.openContainer.inventorySlots.size()) return;
            ItemStack stack = player.openContainer.getSlot(message.index).getStack();
            if (stack.getItem() instanceof ItemToolbelt belt) belt.setSelectedSlot(stack, message.slot);
        }

        private void handleServer(PacketToolbeltSelectionChange message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                ItemStack stack = player.getHeldItemMainhand();
                if (stack.getItem() instanceof ItemToolbelt belt) belt.setSelectedSlot(stack, message.slot);
            });
        }
    }
}
