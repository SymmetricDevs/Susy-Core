package supersymmetry.common.item.behavior;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import gregtech.api.items.metaitem.stats.IItemBehaviour;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import supersymmetry.Supersymmetry;
import supersymmetry.common.item.SuSyMetaItems;

import java.util.List;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID)
public class IntelChipBehaviour implements IItemBehaviour {

    public static final IntelChipBehaviour INSTANCE = new IntelChipBehaviour();

    private static final String TAG_ROOT = "susy";
    private static final String TAG_FACTION = "faction";
    private static final String TAG_KILLS = "kills";
    private static final int MAX_KILLS = 50;

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @NotNull List<String> lines) {

        NBTTagCompound tag = stack.getSubCompound(TAG_ROOT);

        if (tag == null || tag.getString(TAG_FACTION).isEmpty()) {
            lines.add(I18n.format("item.susy.intel_chip.blank"));
            return;
        }

        String faction = tag.getString(TAG_FACTION);
        int kills = tag.getInteger(TAG_KILLS);
        lines.add(I18n.format("item.susy.intel_chip.faction", faction));
        lines.add(I18n.format("item.susy.intel_chip.kills", kills, MAX_KILLS));
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        System.out.println("enters tag giver");
        if (event.getEntity().world.isRemote) return;

        EntityLivingBase dead = (EntityLivingBase) event.getEntity();

        Entity source = event.getSource().getTrueSource();
        if (!(source instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) source;

        NBTTagCompound entityTag = dead.getEntityData();
        if (!entityTag.hasKey(TAG_ROOT)) return;

        String deadFaction = entityTag.getCompoundTag(TAG_ROOT).getString(TAG_FACTION);
        if (deadFaction.isEmpty()) return;

        int boundSlot = -1;
        int blankSlot = -1;

        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            if (!isIntelChip(stack)) continue;

            NBTTagCompound chipTag = stack.getOrCreateSubCompound(TAG_ROOT);
            String chipFaction = chipTag.getString(TAG_FACTION);

            if (chipFaction.isEmpty() && blankSlot == -1) {
                blankSlot = i;
            } else if (chipFaction.equals(deadFaction)) {
                boundSlot = i;
                break;
            }
        }

        int targetSlot = (boundSlot != -1) ? boundSlot : blankSlot;
        if (targetSlot == -1) return;

        ItemStack chip = player.inventory.getStackInSlot(targetSlot);
        NBTTagCompound chipTag = chip.getOrCreateSubCompound(TAG_ROOT);

        if (chipTag.getString(TAG_FACTION).isEmpty()) {
            chipTag.setString(TAG_FACTION, deadFaction);
            chipTag.setInteger(TAG_KILLS, 1);
        } else {
            int kills = chipTag.getInteger(TAG_KILLS) + 1;

            if (kills >= MAX_KILLS) {
                ItemStack fullChip = SuSyMetaItems.INTEL_CHIP_FULL.getStackForm();
                NBTTagCompound fullTag = fullChip.getOrCreateSubCompound(TAG_ROOT);
                fullTag.setString(TAG_FACTION, deadFaction);
                player.inventory.setInventorySlotContents(targetSlot, fullChip);
            } else {
                chipTag.setInteger(TAG_KILLS, kills);
            }
        }
    }

    private static boolean isIntelChip(ItemStack stack) {
        return SuSyMetaItems.isMetaItem(stack) == SuSyMetaItems.INTEL_CHIP.metaValue;
    }
}
