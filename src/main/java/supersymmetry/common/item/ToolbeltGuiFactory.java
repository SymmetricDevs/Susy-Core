package supersymmetry.common.item;

import java.util.Objects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumHand;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.AbstractUIFactory;
import com.cleanroommc.modularui.factory.GuiManager;
import com.cleanroommc.modularui.factory.HandGuiData;

/**
 * Opens an MUI2 panel for an item held in hand.
 * <p>
 * Ported from GTCEu-Reloaded 43140b63f (LGPL-3.0), reduced to the toolbelt.
 */
public class ToolbeltGuiFactory extends AbstractUIFactory<HandGuiData> {

    public static final ToolbeltGuiFactory INSTANCE = new ToolbeltGuiFactory();

    private ToolbeltGuiFactory() {
        super("susy:toolbelt");
    }

    /** Must run on both sides before any open packet is sent, like GTGuis.registerFactories(). */
    public static void register() {
        GuiManager.registerFactory(INSTANCE);
    }

    public static void open(EntityPlayer player, EnumHand hand) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(hand);
        GuiManager.open(INSTANCE, new HandGuiData(player, hand), (EntityPlayerMP) player);
    }

    @Override
    public @NotNull IGuiHolder<HandGuiData> getGuiHolder(HandGuiData data) {
        if (data.getUsedItemStack().getItem() instanceof ItemToolbelt toolbelt) return toolbelt;
        throw new IllegalArgumentException("Found item is not a toolbelt!");
    }

    @Override
    public void writeGuiData(HandGuiData guiData, PacketBuffer buffer) {
        buffer.writeByte(guiData.getHand().ordinal());
    }

    @Override
    public @NotNull HandGuiData readGuiData(EntityPlayer player, PacketBuffer buffer) {
        return new HandGuiData(player, EnumHand.values()[buffer.readByte()]);
    }
}
