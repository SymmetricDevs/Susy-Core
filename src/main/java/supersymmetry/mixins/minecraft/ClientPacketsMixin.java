package supersymmetry.mixins.minecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.inventory.Container;
import net.minecraft.network.play.server.*;
import net.minecraft.util.text.TextComponentString;

import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import supersymmetry.client.network.PacketRules;

@Mixin(NetHandlerPlayClient.class)
public class ClientPacketsMixin {

    @Shadow
    private Minecraft client;
    @Shadow
    private WorldClient world;
    @Unique private long susy$lastWarning;

    @Unique private boolean susy$player() {
        return client.player != null && client.player.connection == (Object) this;
    }

    @Unique private boolean susy$world() {
        return susy$player() && world != null && client.world == world;
    }

    @Unique private void susy$recover(int window, int slot, int size) {
        LogManager.getLogger("SuSyCompat").warn("Rejected out-of-range container update: window={}, slot={}, size={}",
                window, slot, size);
        if (client.player.openContainer.windowId == window) client.player.closeScreen();
        long now = System.nanoTime();
        if (now - susy$lastWarning > 5_000_000_000L) {
            susy$lastWarning = now;
            client.player.sendMessage(new TextComponentString("Container out of sync; reopen it to refresh."));
        }
    }

    @Inject(method = "handleSetSlot",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/network/PacketThreadUtil;checkThreadAndEnqueue(Lnet/minecraft/network/Packet;Lnet/minecraft/network/INetHandler;Lnet/minecraft/util/IThreadListener;)V",
                     shift = At.Shift.AFTER),
            cancellable = true)
    private void slot(SPacketSetSlot packet, CallbackInfo ci) {
        if (!susy$world()) {
            ci.cancel();
            return;
        }
        int window = packet.getWindowId(), slot = packet.getSlot();
        if (window == -1) return;
        if (window == -2) {
            if (PacketRules.invalidSlot(slot, client.player.inventory.getSizeInventory())) {
                susy$recover(window, slot, client.player.inventory.getSizeInventory());
                ci.cancel();
            }
            return;
        }
        Container inventory = client.player.inventoryContainer, open = client.player.openContainer;
        boolean hotbar = window == 0 && slot >= 36 && slot < 45;
        boolean creative = client.currentScreen instanceof GuiContainerCreative &&
                ((GuiContainerCreative) client.currentScreen).getSelectedTabIndex() !=
                        CreativeTabs.INVENTORY.getIndex();
        if (!hotbar && (window != open.windowId || window == 0 && creative)) return;
        Container target = hotbar ? inventory : open;
        if (!PacketRules.invalidSlot(slot, target.inventorySlots.size())) return;
        if (window == 0 && !PacketRules.invalidSlot(slot, inventory.inventorySlots.size())) {
            inventory.putStackInSlot(slot, packet.getStack());
            client.getTutorial().handleSetSlot(packet.getStack());
        } else susy$recover(window, slot, target.inventorySlots.size());
        ci.cancel();
    }

    @Inject(method = "handleWindowItems",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/network/PacketThreadUtil;checkThreadAndEnqueue(Lnet/minecraft/network/Packet;Lnet/minecraft/network/INetHandler;Lnet/minecraft/util/IThreadListener;)V",
                     shift = At.Shift.AFTER),
            cancellable = true)
    private void contents(SPacketWindowItems packet, CallbackInfo ci) {
        if (!susy$world()) {
            ci.cancel();
            return;
        }
        int id = packet.getWindowId();
        Container target = id == 0 ? client.player.inventoryContainer : client.player.openContainer;
        if (id != target.windowId) return;
        if (packet.getItemStacks().size() > target.inventorySlots.size()) {
            susy$recover(id, packet.getItemStacks().size() - 1, target.inventorySlots.size());
            ci.cancel();
        }
    }

    @Inject(method = { "handleBlockChange", "handleTimeUpdate", "handleEntityProperties" },
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/network/PacketThreadUtil;checkThreadAndEnqueue(Lnet/minecraft/network/Packet;Lnet/minecraft/network/INetHandler;Lnet/minecraft/util/IThreadListener;)V",
                     shift = At.Shift.AFTER),
            cancellable = true)
    private void worldPacket(CallbackInfo ci) {
        if (!susy$world()) ci.cancel();
    }

    @Inject(method = "handleChangeGameState",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/network/PacketThreadUtil;checkThreadAndEnqueue(Lnet/minecraft/network/Packet;Lnet/minecraft/network/INetHandler;Lnet/minecraft/util/IThreadListener;)V",
                     shift = At.Shift.AFTER),
            cancellable = true)
    private void state(SPacketChangeGameState packet, CallbackInfo ci) {
        if (!susy$player() || PacketRules.needsWorld(packet.getGameState()) && !susy$world()) ci.cancel();
    }
}
