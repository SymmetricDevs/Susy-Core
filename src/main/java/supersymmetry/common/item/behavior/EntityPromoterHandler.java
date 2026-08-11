package supersymmetry.common.item.behavior;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
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


//exact logic as tagger
@Mod.EventBusSubscriber(modid = Supersymmetry.MODID)
public class EntityPromoterHandler {

    private static final String TAG_ROOT   = "susy";
    private static final String TAG_LEADER = "leader";

    private static final double GLOW_RADIUS = 32.0;

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        EntityPlayer player = event.getEntityPlayer();
        ItemStack    stack  = event.getItemStack();
        Entity       target = event.getTarget();

        if (stack.isEmpty()) return;
        if (SuSyMetaItems.isMetaItem(stack) != SuSyMetaItems.ENTITY_PROMOTER.metaValue) return;
        if (!(target instanceof EntityLiving)) return;

        if (!player.world.isRemote) {
            NBTTagCompound entityTag = target.getEntityData();
            NBTTagCompound susyTag   = entityTag.getCompoundTag(TAG_ROOT);

            boolean isLeader = susyTag.getBoolean(TAG_LEADER);

            if (isLeader) {
                susyTag.removeTag(TAG_LEADER);
                entityTag.setTag(TAG_ROOT, susyTag);
                player.sendMessage(new TextComponentString("§cLeader tag removed."));
            } else {
                susyTag.setBoolean(TAG_LEADER, true);
                entityTag.setTag(TAG_ROOT, susyTag);
                player.sendMessage(new TextComponentString("§aLeader tag set."));
            }
        }

        event.setCancellationResult(EnumActionResult.SUCCESS);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onPlayerTick(net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent event) {
        if (event.phase != net.minecraftforge.fml.common.gameevent.TickEvent.Phase.END) return;

        EntityPlayer player = event.player;
        if (player.world.isRemote) return;

        boolean enabled = player.getEntityWorld().getGameRules().getBoolean("factionTagger");
        if (!enabled) return;

        if (player.ticksExisted % 5 != 0) return;

        ItemStack stack = player.getHeldItemMainhand();
        boolean holding = !stack.isEmpty() &&
                SuSyMetaItems.isMetaItem(stack) == SuSyMetaItems.ENTITY_PROMOTER.metaValue;

        java.util.List<EntityLivingBase> entities = player.world.getEntitiesWithinAABB(
                EntityLivingBase.class,
                player.getEntityBoundingBox().grow(GLOW_RADIUS));

        for (EntityLivingBase entity : entities) {
            NBTTagCompound susy     = entity.getEntityData().getCompoundTag(TAG_ROOT);
            boolean        isLeader = susy.getBoolean(TAG_LEADER);
            entity.setGlowing(holding && isLeader);
        }
    }
}
