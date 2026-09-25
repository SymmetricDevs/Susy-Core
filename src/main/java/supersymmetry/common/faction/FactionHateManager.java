package supersymmetry.common.faction;

import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.Universe;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.List;

public class FactionHateManager {

    private static final String TAG_ROOT = "susy";
    private static final String TAG_HATE = "hate";

    private static final String FORGE_DATA = "ForgeData";

    public static int getHate(EntityPlayer player, String faction) {
        NBTTagCompound root = player.getEntityData().getCompoundTag(TAG_ROOT);
        NBTTagCompound hate = root.getCompoundTag(TAG_HATE);
        return hate.getInteger(faction);
    }

    public static void addHate(EntityPlayer player, String faction, int amount) {
        List<ForgePlayer> teamMembers = getAllTeamMembers(player);
        int maxHate = getHate(player, faction);
        for (ForgePlayer member : teamMembers) {
            int memberHate = getHateFromForgePlayer(member, faction);
            if (memberHate > maxHate) {
                maxHate = memberHate;
            }
        }
        int next = maxHate + amount;
        int baseline = FactionBaselineRegistry.getBaseline((EntityPlayerMP) player);
        for (ForgePlayer member : teamMembers) {
            if (member.isOnline()) {
                int memberBaseline = FactionBaselineRegistry.getBaseline(member.getPlayer());
                if (memberBaseline > baseline) {
                    baseline = memberBaseline;
                }
            }
        }
        if (next < baseline) next = baseline;
        next = Math.max(0, next);
        writeHate(player, faction, next);
        for (ForgePlayer member : teamMembers) {
            writeHateToForgePlayer(member, faction, next);
        }
    }

    public static void setHate(EntityPlayer player, String faction, int amount) {
        writeHate(player, faction, amount);
    }

    private static int getHateFromForgePlayer(ForgePlayer fp, String faction) {
        if (fp.isOnline()) {
            return getHate(fp.getPlayer(), faction);
        }
        NBTTagCompound playerNBT = fp.getPlayerNBT();
        NBTTagCompound forgeData = playerNBT.getCompoundTag(FORGE_DATA);
        NBTTagCompound root = forgeData.getCompoundTag(TAG_ROOT);
        NBTTagCompound hate = root.getCompoundTag(TAG_HATE);
        return hate.getInteger(faction);
    }

    private static void writeHateToForgePlayer(ForgePlayer fp, String faction, int amount) {
        if (fp.isOnline()) {
            writeHate(fp.getPlayer(), faction, amount);
            return;
        }
        NBTTagCompound playerNBT = fp.getPlayerNBT();
        NBTTagCompound forgeData = playerNBT.getCompoundTag(FORGE_DATA);
        NBTTagCompound root = forgeData.getCompoundTag(TAG_ROOT);
        NBTTagCompound hate = root.getCompoundTag(TAG_HATE);
        hate.setInteger(faction, amount);
        root.setTag(TAG_HATE, hate);
        forgeData.setTag(TAG_ROOT, root);
        playerNBT.setTag(FORGE_DATA, forgeData);
        fp.setPlayerNBT(playerNBT);
    }

    private static List<ForgePlayer> getAllTeamMembers(EntityPlayer player) {
        List<ForgePlayer> result = new ArrayList<>();
        try {
            Universe universe = Universe.get();
            if (universe == null) return result;
            ForgePlayer self = universe.getPlayer(player.getGameProfile().getId());
            if (self == null) return result;
            ForgeTeam team = self.team;
            if (team == null || !team.isValid()) return result;
            for (ForgePlayer member : team.getMembers()) {
                if (!member.getId().equals(player.getGameProfile().getId())) {
                    result.add(member);
                }
            }
        } catch (Exception ignored) {}
        return result;
    }

    private static void writeHate(EntityPlayer player, String faction, int amount) {
        NBTTagCompound root = player.getEntityData().getCompoundTag(TAG_ROOT);
        NBTTagCompound hate = root.getCompoundTag(TAG_HATE);
        hate.setInteger(faction, amount);
        root.setTag(TAG_HATE, hate);
        player.getEntityData().setTag(TAG_ROOT, root);
    }
}
