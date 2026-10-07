package supersymmetry.common.item.behavior;

import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

import gregtech.api.items.metaitem.stats.IItemBehaviour;
import supersymmetry.common.faction.FactionHateManager;

public class FactionRadioBehaviour implements IItemBehaviour {

    private static final String TAG_ROOT = "susy";
    private static final String TAG_FACTION = "faction";

    public static final FactionRadioBehaviour INSTANCE = new FactionRadioBehaviour();

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        ActionResult<ItemStack> result = ActionResult.newResult(EnumActionResult.SUCCESS, stack);

        if (player.world.isRemote)
            return result;

        // Read faction from item NBT
        NBTTagCompound tag = stack.getSubCompound(TAG_ROOT);
        if (tag == null) {
            player.sendStatusMessage(new TextComponentTranslation("chat.susy.radio.no_tag"), true);
            return result;
        }

        String faction = tag.getString(TAG_FACTION);
        if (faction.isEmpty()) {
            player.sendStatusMessage(new TextComponentTranslation("chat.susy.radio.no_faction"), true);
            return result;
        }

        // Get hate value (SERVER SIDE SAFE)
        int hate = FactionHateManager.getHate(player, faction);

        // Send to player (action bar)
        player.sendStatusMessage(new TextComponentTranslation("chat.susy.radio.get_hate", hate), true);

        return result;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @NotNull List<String> lines) {
        NBTTagCompound tag = stack.getSubCompound(TAG_ROOT);

        if (tag == null || tag.getString(TAG_FACTION).isEmpty()) {
            lines.add(I18n.format("item.susy.faction_radio.blank"));
            return;
        }

        String faction = tag.getString(TAG_FACTION);
        lines.add(I18n.format("item.susy.faction_radio.faction", faction));
    }
}
