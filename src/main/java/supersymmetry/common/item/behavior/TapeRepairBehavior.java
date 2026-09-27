package supersymmetry.common.item.behavior;

import static net.minecraft.inventory.EntityEquipmentSlot.HEAD;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import gregtech.api.items.metaitem.stats.IItemBehaviour;
import supersymmetry.common.item.SuSyArmorItem;
import supersymmetry.common.item.armor.SpaceSuit;

public class TapeRepairBehavior implements IItemBehaviour {

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (!player.isSneaking()) {
            return ActionResult.newResult(EnumActionResult.PASS, player.getHeldItem(hand));
        }

        ItemStack held = player.getHeldItem(hand);
        ItemStack helmet = player.getItemStackFromSlot(HEAD);
        if (helmet.getItem() instanceof SuSyArmorItem item) {
            if (item.getItem(helmet).getArmorLogic() instanceof SpaceSuit suit) {
                if (suit.getTapedHoles(helmet) < suit.getPunctures(helmet)) {
                    if (!world.isRemote) {
                        suit.tapeHole(helmet);
                        held.shrink(1);
                    }
                    return ActionResult.newResult(EnumActionResult.SUCCESS, held);
                }
            }
        }
        return ActionResult.newResult(EnumActionResult.PASS, held);
    }
}
