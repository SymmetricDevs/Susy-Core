package supersymmetry.common.item.behavior;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.text.TextComponentString;

import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

import supersymmetry.common.item.SuSyMetaItems;
import supersymmetry.common.util.FactionHelper;
import supersymmetry.Supersymmetry;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID)
public class EntityTaggerHandler {

    private static final String TAG_ROOT = "susy";
    private static final String TAG_FACTION = "faction";

    //right click on entity to add to faction (sets nbt tag)
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        EntityPlayer player = event.getEntityPlayer();
        ItemStack stack = event.getItemStack();
        Entity target = event.getTarget();

        if (stack.isEmpty()) return;

        if (SuSyMetaItems.isMetaItem(stack) != SuSyMetaItems.ENTITY_TAGGER.metaValue)
            return;

        if (!player.world.isRemote) {
            NBTTagCompound entityTag = target.getEntityData();

            // shift right click to clear faction
            if (player.isSneaking()) {
                entityTag.removeTag(TAG_ROOT + "." + TAG_FACTION);
                player.sendMessage(new TextComponentString("Faction cleared"));
            } else {
                NBTTagCompound itemTag = stack.getOrCreateSubCompound(TAG_ROOT);

                String faction = itemTag.getString(TAG_FACTION);

                if (faction.isEmpty()) {
                    faction = FactionHelper.FACTIONS[0];
                    itemTag.setString(TAG_FACTION, faction);
                }

                entityTag.setString(TAG_ROOT + "." + TAG_FACTION, faction);

                player.sendMessage(new TextComponentString("Set faction: " + faction));
            }
        }

        event.setCancellationResult(EnumActionResult.SUCCESS);
        event.setCanceled(true);
    }

    //chaing selected faction
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        EntityPlayer player = event.getEntityPlayer();
        ItemStack stack = event.getItemStack();

        if (stack.isEmpty()) return;

        if (SuSyMetaItems.isMetaItem(stack) != SuSyMetaItems.ENTITY_TAGGER.metaValue)
            return;

        if (!player.isSneaking()) return;

        if (!player.world.isRemote) {
            NBTTagCompound tag = stack.getOrCreateSubCompound(TAG_ROOT);

            String current = tag.getString(TAG_FACTION);
            String next = FactionHelper.getNextFaction(current);

            tag.setString(TAG_FACTION, next);

            player.sendMessage(new TextComponentString("Faction set to: " + next));
        }

        event.setCancellationResult(EnumActionResult.SUCCESS);
        event.setCanceled(true);
    }

    //glow for easier attaching
    @SubscribeEvent
    public static void onPlayerTick(net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent event) {
        if (event.phase != net.minecraftforge.fml.common.gameevent.TickEvent.Phase.END) return;

        EntityPlayer player = event.player;

        if (player.world.isRemote) return; // SERVER ONLY

        ItemStack stack = player.getHeldItemMainhand();

        double radius = 32.0;

        List<EntityLivingBase> entities = player.world.getEntitiesWithinAABB(
                EntityLivingBase.class,
                player.getEntityBoundingBox().grow(radius)
        );

        if (stack.isEmpty() ||
                SuSyMetaItems.isMetaItem(stack) != SuSyMetaItems.ENTITY_TAGGER.metaValue) {

            for (EntityLivingBase entity : entities) {
                entity.setGlowing(false);
            }
            return;
        }

        NBTTagCompound tag = stack.getSubCompound("susy");
        if (tag == null) return;

        String selectedFaction = tag.getString("faction");
        if (selectedFaction.isEmpty()) return;

        for (EntityLivingBase entity : entities) {
            String faction = entity.getEntityData().getString("susy.faction");

            if (selectedFaction.equals(faction)) {
                entity.setGlowing(true);
            } else {
                entity.setGlowing(false);
            }
        }
    }
}
