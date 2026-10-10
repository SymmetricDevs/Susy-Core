package supersymmetry.common.faction;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import com.feed_the_beast.ftblib.events.team.ForgeTeamDataEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamDeletedEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamPlayerJoinedEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamPlayerLeftEvent;
import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.ForgeTeam;

import supersymmetry.Supersymmetry;

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

    // migration: HATE system for teams is now handled by storing it in actual ftb team data
    // instead of syncing NBT tags across people in the same team
    // this removes weird desync issues like we've seen in:
    // https://discord.com/channels/881234100504109166/1094752913139707927/1558124632391950336
    @SubscribeEvent
    public static void onTeamData(ForgeTeamDataEvent event) {
        event.register(new TeamHateData(event.getTeam()));
    }

    @SubscribeEvent
    public static void onTeamJoin(ForgeTeamPlayerJoinedEvent event) {
        ForgePlayer joining = event.getPlayer();
        ForgeTeam team = joining.team;
        if (team == null || !team.isValid()) return;

        TeamHateData data = TeamHateData.get(team);
        NBTTagCompound personal = FactionHateManager.getPersonalHateTag(joining);
        for (String faction : personal.getKeySet()) {
            data.setHate(faction, Math.max(data.getHate(faction), personal.getInteger(faction)));
        }
        if (joining.isOnline()) {
            data.raiseBaseline(FactionBaselineRegistry.getBaseline(joining.getPlayer()));
        }
    }

    @SubscribeEvent
    public static void onTeamLeave(ForgeTeamPlayerLeftEvent event) {
        ForgePlayer leaving = event.getPlayer();
        ForgeTeam team = leaving.team;
        if (team == null || !team.isValid()) return;

        TeamHateData data = TeamHateData.get(team);
        if (data == null) return;
        FactionHateManager.writePersonalHate(leaving, data.getAll());
    }

    @SubscribeEvent
    public static void onTeamDeleted(ForgeTeamDeletedEvent event) {
        ForgeTeam team = event.getTeam();
        TeamHateData data = TeamHateData.get(team);
        if (data == null || data.getAll().isEmpty()) return;

        for (ForgePlayer member : team.getMembers()) {
            FactionHateManager.writePersonalHate(member, data.getAll());
        }
    }
}
