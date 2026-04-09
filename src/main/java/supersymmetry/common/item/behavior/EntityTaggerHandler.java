package supersymmetry.common.item.behavior;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import supersymmetry.Supersymmetry;
import supersymmetry.common.item.SuSyMetaItems;
import supersymmetry.common.util.FactionHelper;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID)
public class EntityTaggerHandler {

    private static final String TAG_ROOT = "susy";
    private static final String TAG_FACTION = "faction";
    private static final String TAG_HATE = "hate";
    private static final double radius = 32;

    // right click on entity to add to faction (sets nbt tag)
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
            NBTTagCompound susyTag = entityTag.getCompoundTag(TAG_ROOT);

            // shift right click to clear faction
            if (player.isSneaking()) {
                susyTag.removeTag(TAG_FACTION);
                entityTag.setTag(TAG_ROOT, susyTag);

                player.sendMessage(new TextComponentString("Faction cleared"));
            } else {
                NBTTagCompound itemTag = stack.getOrCreateSubCompound(TAG_ROOT);

                String faction = itemTag.getString(TAG_FACTION);

                if (faction.isEmpty()) {
                    faction = FactionHelper.FACTIONS[0];
                    itemTag.setString(TAG_FACTION, faction);
                }

                susyTag.setString(TAG_FACTION, faction);
                entityTag.setTag(TAG_ROOT, susyTag);

                player.sendMessage(new TextComponentString("Set faction: " + faction));
            }
        }

        event.setCancellationResult(EnumActionResult.SUCCESS);
        event.setCanceled(true);
    }

    // chaing selected faction
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

    @SubscribeEvent
    public static void onAttackEntity(net.minecraftforge.event.entity.player.AttackEntityEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        ItemStack stack = player.getHeldItemMainhand();
        Entity target = event.getTarget();

        if (stack.isEmpty()) return;

        if (SuSyMetaItems.isMetaItem(stack) != SuSyMetaItems.ENTITY_TAGGER.metaValue)
            return;

        if (!(target instanceof EntityLivingBase)) return;

        if (!player.world.isRemote) {

            NBTTagCompound entityTag = target.getEntityData();

            // Get or create susy compound
            NBTTagCompound susyTag = entityTag.getCompoundTag(TAG_ROOT);

            // Get current hate
            int currentHate = susyTag.getInteger(TAG_HATE);

            // Modify hate
            int amount = player.isSneaking() ? -1 : 1;
            int newHate = currentHate + amount;

            susyTag.setInteger(TAG_HATE, newHate);
            entityTag.setTag(TAG_ROOT, susyTag);

            player.sendMessage(new TextComponentString(
                    "Mob hate value: " + newHate));
        }

        // Prevent damage
        event.setCanceled(true);
    }

    // glow for easier attaching, dev only, might make a config to unsubscribe this so it doesn't spam the ticks
    @SubscribeEvent
    public static void onPlayerTick(net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent event) {
        if (event.phase != net.minecraftforge.fml.common.gameevent.TickEvent.Phase.END) return;

        EntityPlayer player = event.player;

        if (player.world.isRemote) return; // SERVER ONLY

        // Only run every 5 ticks to not cook the server
        if (player.ticksExisted % 5 != 0) return;

        ItemStack stack = player.getHeldItemMainhand();

        // If not holding correct item exit
        if (stack.isEmpty() ||
                SuSyMetaItems.isMetaItem(stack) != SuSyMetaItems.ENTITY_TAGGER.metaValue) {

            List<EntityLivingBase> entities = player.world.getEntitiesWithinAABB(
                    EntityLivingBase.class,
                    player.getEntityBoundingBox().grow(radius));

            for (EntityLivingBase entity : entities) {
                if (entity.isGlowing()) {
                    entity.setGlowing(false);
                }
            }
            return;
        }

        NBTTagCompound tag = stack.getSubCompound(TAG_ROOT);
        if (tag == null) return;

        String selectedFaction = tag.getString(TAG_FACTION);
        if (selectedFaction.isEmpty()) return;

        List<EntityLivingBase> entities = player.world.getEntitiesWithinAABB(
                EntityLivingBase.class,
                player.getEntityBoundingBox().grow(radius));

        for (EntityLivingBase entity : entities) {
            NBTTagCompound susy = entity.getEntityData().getCompoundTag(TAG_ROOT);
            String faction = susy.getString(TAG_FACTION);

            entity.setGlowing(selectedFaction.equals(faction));
        }
    }
}
