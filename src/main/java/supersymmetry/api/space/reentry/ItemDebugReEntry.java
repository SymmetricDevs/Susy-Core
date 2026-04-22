package supersymmetry.api.space.reentry;

import java.util.Collections;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ItemDebugReEntry extends Item {

    public ItemDebugReEntry() {
        setRegistryName("susy", "debug_reentry");
        setMaxStackSize(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (!world.isRemote && player instanceof EntityPlayerMP) {
            ReEntryLaunchHelper.beginReEntry(
                    ReEntryDimensions.EARTH_REENTRY_DIM_ID,
                    Collections.singletonList((EntityPlayerMP) player));
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }
}
