package supersymmetry.common.faction;

import com.feed_the_beast.ftblib.events.team.ForgeTeamPlayerJoinedEvent;
import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import supersymmetry.Supersymmetry;

import java.util.List;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID)
public class FactionHate {

    private static final String TAG_ROOT = "susy";
    private static final String TAG_FACTION = "faction";
    private static final String TAG_HATE = "hate";
    private static final String FORGE_DATA = "ForgeData";

    // how far we'll look for someone to credit an environmental/trap kill to
    private static final double MAX_ENVIRONMENTAL_KILL_RADIUS = 400.0D;

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().world.isRemote)
            return;

        EntityLivingBase dead = (EntityLivingBase) event.getEntity();

        NBTTagCompound entityTag = dead.getEntityData();
        if (!entityTag.hasKey(TAG_ROOT))
            return;

        NBTTagCompound susy = entityTag.getCompoundTag(TAG_ROOT);

        String faction = susy.getString(TAG_FACTION);
        if (faction.isEmpty())
            return;

        int hateValue = susy.getInteger(TAG_HATE);

        // Get killer, if any
        Entity source = event.getSource().getTrueSource();

        EntityPlayer player;
        if (source instanceof EntityPlayer) {
            player = (EntityPlayer) source;
        } else {
            // environmental death, give credit to nearest player
            // works both as anti-cheese for suffocation traps while looting and makes it so your own
            // defenses (such as turrets/razor wire) work towards hate reduction
            player = findNearestPlayer(dead, MAX_ENVIRONMENTAL_KILL_RADIUS);
            if (player == null)
                return;
        }

        // Apply to player
        FactionHateManager.addHate(player, faction, hateValue);
    }

    private static EntityPlayer findNearestPlayer(EntityLivingBase dead, double maxRadius) {
        List<EntityPlayer> players = dead.world.playerEntities;

        EntityPlayer nearest = null;
        double nearestDistSq = maxRadius * maxRadius;

        for (EntityPlayer candidate : players) {
            double distSq = dead.getDistanceSq(candidate);
            if (distSq <= nearestDistSq) {
                nearest = candidate;
                nearestDistSq = distSq;
            }
        }

        return nearest;
    }

    // making sure the hate stays after you die
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getEntity().world.isRemote)
            return;

        EntityPlayer original = event.getOriginal();
        EntityPlayer clone = (EntityPlayer) event.getEntity();

        NBTTagCompound originalData = original.getEntityData();
        if (!originalData.hasKey(TAG_ROOT))
            return;

        NBTTagCompound susyData = originalData.getCompoundTag(TAG_ROOT);
        if (!susyData.hasKey(TAG_HATE))
            return;

        NBTTagCompound cloneData = clone.getEntityData();
        cloneData.setTag(TAG_ROOT, susyData.copy());
    }

    @SubscribeEvent
    public static void onTeamJoin(ForgeTeamPlayerJoinedEvent event) {
        ForgePlayer joining = event.getPlayer();

        if (!joining.isOnline())
            return;

        ForgeTeam team = joining.team;
        if (team == null || !team.isValid())
            return;

        NBTTagCompound maxHates = new NBTTagCompound();
        for (ForgePlayer member : team.getMembers()) {
            if (member.getId().equals(joining.getId()))
                continue;

            NBTTagCompound memberHate = getMemberHateNBT(member);
            for (String faction : memberHate.getKeySet()) {
                int memberValue = memberHate.getInteger(faction);
                int currentMax = maxHates.getInteger(faction);
                if (memberValue > currentMax) {
                    maxHates.setInteger(faction, memberValue);
                }
            }
        }

        NBTTagCompound joiningHate = getMemberHateNBT(joining);
        for (String faction : joiningHate.getKeySet()) {
            int joiningValue = joiningHate.getInteger(faction);
            int currentMax = maxHates.getInteger(faction);
            if (joiningValue > currentMax) {
                maxHates.setInteger(faction, joiningValue);
            }
        }

        if (maxHates.getSize() == 0)
            return;

        EntityPlayerMP joiningPlayer = joining.getPlayer();
        for (String faction : maxHates.getKeySet()) {
            int trueMax = maxHates.getInteger(faction);
            FactionHateManager.setHate(joiningPlayer, faction, trueMax);
            for (ForgePlayer member : team.getMembers()) {
                if (member.getId().equals(joining.getId()))
                    continue;
                FactionHateManager.writeHateToForgePlayer(member, faction, trueMax);
            }
        }
    }

    private static NBTTagCompound getMemberHateNBT(ForgePlayer member) {
        if (member.isOnline()) {
            NBTTagCompound root = member.getPlayer().getEntityData().getCompoundTag(TAG_ROOT);
            return root.getCompoundTag(TAG_HATE);
        }
        NBTTagCompound playerNBT = member.getPlayerNBT();
        NBTTagCompound forgeData = playerNBT.getCompoundTag(FORGE_DATA);
        NBTTagCompound root = forgeData.getCompoundTag(TAG_HATE);
        return root.getCompoundTag(TAG_HATE);
    }
}
