package supersymmetry.common.item.behavior;

import gregtech.api.items.metaitem.stats.IItemBehaviour;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import supersymmetry.Supersymmetry;
import supersymmetry.common.faction.FactionHateManager;
import supersymmetry.common.item.SuSyMetaItems;

import java.util.List;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID)
public class FactionRadioBehaviour implements IItemBehaviour {

    private static final String TAG_ROOT = "susy";
    private static final String TAG_FACTION = "faction";

    public static final FactionRadioBehaviour INSTANCE = new FactionRadioBehaviour();

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @NotNull List<String> lines) {

        NBTTagCompound tag = stack.getSubCompound(TAG_ROOT);

        if (tag == null || tag.getString(TAG_FACTION).isEmpty()) {
            lines.add(I18n.format("item.susy.faction_radio.blank"));
            return;
        }

        String faction = tag.getString(TAG_FACTION);

        lines.add(I18n.format("item.susy.faction_radio.faction", faction));
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        EntityPlayer player = event.getEntityPlayer();
        ItemStack stack = event.getItemStack();

        if (stack.isEmpty()) return;

        if (player.world.isRemote) return;

        // Only our faction radio item
        if (SuSyMetaItems.isMetaItem(stack) != SuSyMetaItems.FACTION_RADIO.metaValue)
            return;

        // Read faction from item NBT
        NBTTagCompound tag = stack.getSubCompound(TAG_ROOT);

        if (tag == null) {
            player.sendStatusMessage(
                    new TextComponentTranslation("chat.susy.radio.no_tag"),
                    true);
            event.setCanceled(true);
            return;
        }

        String faction = tag.getString(TAG_FACTION);

        if (faction.isEmpty()) {
            player.sendStatusMessage(
                    new TextComponentTranslation("chat.susy.radio.no_faction"),
                    true);
            event.setCanceled(true);
            return;
        }

        // Get hate value (SERVER SIDE SAFE)
        int hate = FactionHateManager.getHate(player, faction);

        // Send to player (action bar)
        player.sendStatusMessage(
                new TextComponentTranslation("chat.susy.radio.get_hate", hate),
                true);

        event.setCanceled(true);

        event.setCancellationResult(EnumActionResult.SUCCESS);
        event.setCanceled(true);
    }
}
