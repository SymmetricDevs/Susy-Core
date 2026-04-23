package supersymmetry.common.item.behavior;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import supersymmetry.Supersymmetry;
import supersymmetry.common.faction.FactionHateManager;
import supersymmetry.common.item.SuSyMetaItems;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID)
public class FactionRadioBehaviour {

    private static final String TAG_ROOT = "susy";
    private static final String TAG_FACTION = "faction";

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        EntityPlayer player = event.getEntityPlayer();
        ItemStack stack = event.getItemStack();

        if (stack.isEmpty()) return;

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
