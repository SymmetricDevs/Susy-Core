package supersymmetry.common.item.behavior;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

import gregtech.api.items.metaitem.stats.IItemBehaviour;
import supersymmetry.common.event.MobHordeWorldData;
import supersymmetry.common.item.SuSyMetaItems;

public class SkywatchChipBehaviour implements IItemBehaviour {

    public static final SkywatchChipBehaviour INSTANCE = new SkywatchChipBehaviour();

    public static final String TAG = "skywatch";
    public static final String TAG_ALERT = "alert";
    public static final String TAG_X = "raidX";
    public static final String TAG_Z = "raidZ";
    public static final String TAG_ARROW_UNTIL = "arrowUntil";

    public static final double RANGE = 150.0D;
    private static final int CHECK_INTERVAL = 20;
    public static final int ARROW_DURATION = 100;

    @Override
    public void onUpdate(ItemStack stack, Entity entity) {
        World world = entity.world;
        if (world.isRemote || !(entity instanceof EntityPlayer)) return;
        if ((world.getTotalWorldTime() + entity.getEntityId()) % CHECK_INTERVAL != 0) return;

        double[] raid = MobHordeWorldData.get(world)
                .findNearestPodRaid(entity.dimension, entity.posX, entity.posZ, RANGE);

        if (raid != null) {
            NBTTagCompound tag = stack.getOrCreateSubCompound(TAG);
            if (!tag.getBoolean(TAG_ALERT)) {
                tag.setBoolean(TAG_ALERT, true);
                world.playSound(null, entity.posX, entity.posY, entity.posZ,
                        SoundEvents.BLOCK_NOTE_PLING, SoundCategory.PLAYERS, 1.0F, 0.5F);
            }
            if (tag.getDouble(TAG_X) != raid[0] || tag.getDouble(TAG_Z) != raid[1]) {
                tag.setDouble(TAG_X, raid[0]);
                tag.setDouble(TAG_Z, raid[1]);
            }
        } else if (isAlert(stack)) {
            stack.removeSubCompound(TAG);
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!isAlert(stack)) return new ActionResult<>(EnumActionResult.PASS, stack);
        if (!world.isRemote) {
            stack.getOrCreateSubCompound(TAG).setLong(TAG_ARROW_UNTIL,
                    world.getTotalWorldTime() + ARROW_DURATION);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    public static boolean isAlert(ItemStack stack) {
        if (stack.isEmpty() || SuSyMetaItems.isMetaItem(stack) != SuSyMetaItems.SKYWATCH_CHIP.metaValue) {
            return false;
        }
        NBTTagCompound tag = stack.getSubCompound(TAG);
        return tag != null && tag.getBoolean(TAG_ALERT);
    }
}
