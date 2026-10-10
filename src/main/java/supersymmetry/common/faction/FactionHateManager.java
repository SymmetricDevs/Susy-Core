package supersymmetry.common.faction;

import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.Universe;
import org.jspecify.annotations.Nullable;

public class FactionHateManager {

    private static final String TAG_ROOT = "susy";
    private static final String TAG_HATE = "hate";
    private static final String FORGE_DATA = "ForgeData";

    @Nullable
    public static ForgeTeam getTeam(EntityPlayer player) {
        Universe universe = Universe.get();
        if (universe == null) return null;
        ForgePlayer fp = universe.getPlayer(player.getGameProfile().getId());
        if (fp == null) return null;
        ForgeTeam team = fp.team;
        return (team != null && team.isValid()) ? team : null;
    }

    public static void setHate(EntityPlayer player, String faction, int amount) {
        ForgeTeam team = getTeam(player);
        if (team != null) {
            TeamHateData.get(team).setHate(faction, Math.max(0, amount));
        } else {
            writePersonalHate(player, faction, Math.max(0, amount));
        }
    }

    public static int getEffectiveBaseline(EntityPlayer player) {
        int online = 0;
        if (player instanceof EntityPlayerMP) {
            online = FactionBaselineRegistry.getBaseline((EntityPlayerMP) player);
        }
        ForgeTeam team = getTeam(player);
        if (team == null) return online;

        for (EntityPlayerMP member : team.getOnlineMembers()) {
            online = Math.max(online, FactionBaselineRegistry.getBaseline(member));
        }
        TeamHateData data = TeamHateData.get(team);
        data.raiseBaseline(online);
        return data.getBaseline();
    }

    public static int getStoredHate(EntityPlayer player, String faction) {
        ForgeTeam team = getTeam(player);
        if (team != null) return TeamHateData.get(team).getHate(faction);
        return getPersonalHate(player, faction);
    }

    public static int getHate(EntityPlayer player, String faction) {
        return Math.max(getStoredHate(player, faction), getEffectiveBaseline(player));
    }

    public static void addHate(EntityPlayer player, String faction, int amount) {
        int next = Math.max(getEffectiveBaseline(player), getHate(player, faction) + amount);
        setHate(player, faction, next);
    }

    public static int getPersonalHate(EntityPlayer player, String faction) {
        return player.getEntityData().getCompoundTag(TAG_ROOT)
                .getCompoundTag(TAG_HATE).getInteger(faction);
    }

    public static NBTTagCompound getPersonalHateTag(ForgePlayer fp) {
        if (fp.isOnline()) {
            return fp.getPlayer().getEntityData().getCompoundTag(TAG_ROOT).getCompoundTag(TAG_HATE);
        }
        return fp.getPlayerNBT().getCompoundTag(FORGE_DATA)
                .getCompoundTag(TAG_ROOT).getCompoundTag(TAG_HATE);
    }

    private static void writePersonalHate(EntityPlayer player, String faction, int amount) {
        NBTTagCompound root = player.getEntityData().getCompoundTag(TAG_ROOT);
        NBTTagCompound hate = root.getCompoundTag(TAG_HATE);
        hate.setInteger(faction, amount);
        root.setTag(TAG_HATE, hate);
        player.getEntityData().setTag(TAG_ROOT, root);
    }

    static void writePersonalHate(ForgePlayer fp, Map<String, Integer> values) {
        if (fp.isOnline()) {
            values.forEach((f, v) -> writePersonalHate(fp.getPlayer(), f, v));
            return;
        }
        NBTTagCompound playerNBT = fp.getPlayerNBT();
        NBTTagCompound forgeData = playerNBT.getCompoundTag(FORGE_DATA);
        NBTTagCompound root = forgeData.getCompoundTag(TAG_ROOT);
        NBTTagCompound hate = root.getCompoundTag(TAG_HATE);
        values.forEach(hate::setInteger);
        root.setTag(TAG_HATE, hate);
        forgeData.setTag(TAG_ROOT, root);
        playerNBT.setTag(FORGE_DATA, forgeData);
        fp.setPlayerNBT(playerNBT);
    }
}
