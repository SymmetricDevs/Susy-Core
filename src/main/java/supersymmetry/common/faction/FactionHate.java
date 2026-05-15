package supersymmetry.common.faction;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import supersymmetry.Supersymmetry;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID)
public class FactionHate {

    private static final String TAG_ROOT = "susy";
    private static final String TAG_FACTION = "faction";
    private static final String TAG_HATE = "hate";

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().world.isRemote) return;

        EntityLivingBase dead = (EntityLivingBase) event.getEntity();

        // Get killer
        Entity source = event.getSource().getTrueSource();
        if (!(source instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) source;

        NBTTagCompound entityTag = dead.getEntityData();
        if (!entityTag.hasKey(TAG_ROOT)) return;

        NBTTagCompound susy = entityTag.getCompoundTag(TAG_ROOT);

        String faction = susy.getString(TAG_FACTION);
        if (faction.isEmpty()) return;

        int hateValue = susy.getInteger(TAG_HATE);

        // Apply to player
        FactionHateManager.addHate(player, faction, hateValue);
    }
}
