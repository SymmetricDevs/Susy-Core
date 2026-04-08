package supersymmetry.common.faction;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class FactionHateManager {

    private static final String TAG_ROOT = "susy";
    private static final String TAG_HATE = "hate";

    public static int getHate(EntityPlayer player, String faction) {
        NBTTagCompound root = player.getEntityData().getCompoundTag(TAG_ROOT);
        NBTTagCompound hate = root.getCompoundTag(TAG_HATE);

        return hate.getInteger(faction);
    }

    public static void addHate(EntityPlayer player, String faction, int amount) {
        NBTTagCompound root = player.getEntityData().getCompoundTag(TAG_ROOT);
        NBTTagCompound hate = root.getCompoundTag(TAG_HATE);

        int current = hate.getInteger(faction);
        int next = Math.max(0, current + amount);

        hate.setInteger(faction, next);
        root.setTag(TAG_HATE, hate);
        player.getEntityData().setTag(TAG_ROOT, root);
    }

    public static void setHate(EntityPlayer player, String faction, int amount) {
        //debug only, use addHate
        NBTTagCompound root = player.getEntityData().getCompoundTag(TAG_ROOT);
        NBTTagCompound hate = root.getCompoundTag(TAG_HATE);
        hate.setInteger(faction, amount);
        root.setTag(TAG_HATE, hate);
        player.getEntityData().setTag(TAG_ROOT, root);
    }
}
