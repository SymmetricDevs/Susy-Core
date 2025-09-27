package supersymmetry.api.items;

import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor;

import org.jetbrains.annotations.NotNull;

import gregtech.api.items.armor.ArmorMetaItem;
import gregtech.api.items.armor.ISpecialArmorLogic;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.common.items.behaviors.TooltipBehavior;

public interface IBreathingArmorLogic extends ISpecialArmorLogic {

    boolean mayBreatheWith(ItemStack stack, EntityPlayer player);

    double getDamageAbsorbed(ItemStack stack, EntityPlayer player);

    default ISpecialArmor.ArmorProperties getProperties(EntityLivingBase player, @NotNull ItemStack armor,
                                                        DamageSource source, double damage,
                                                        EntityEquipmentSlot equipmentSlot) {
        return new ISpecialArmor.ArmorProperties(0, 0, (int) player.getMaxHealth());
    }

    default int getArmorDisplay(EntityPlayer player, @NotNull ItemStack armor, int slot) {
        return 0;
    }

    void addInformation(ItemStack stack, List<String> tooltips);

    @Override
    default void addToolComponents(ArmorMetaItem.ArmorMetaValueItem metaValueItem) {
        metaValueItem.addComponents(new TooltipBehavior((ignored) -> {}) {

            @Override
            public void addInformation(ItemStack itemStack, @NotNull List<String> lines) {
                IBreathingArmorLogic.this.addInformation(itemStack, lines);
            }
        }, new IItemBehaviour() {

            @Override
            public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
                return onRightClick(world, player, hand);
            }
        });
    }

    default ActionResult<ItemStack> onRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (player.getHeldItem(hand).getItem() instanceof ArmorMetaItem) {
            ItemStack armor = player.getHeldItem(hand);
            if (armor.getItem() instanceof ArmorMetaItem &&
                    player.inventory.armorInventory.get(getEquipmentSlot(armor).getIndex()).isEmpty() &&
                    !player.isSneaking()) {
                player.inventory.armorInventory.set(getEquipmentSlot(armor).getIndex(), armor.copy());
                player.setHeldItem(hand, ItemStack.EMPTY);
                player.playSound(new SoundEvent(new ResourceLocation("item.armor.equip_generic")), 1.0F, 1.0F);
                return ActionResult.newResult(EnumActionResult.SUCCESS, armor);
            }
        }

        return ActionResult.newResult(EnumActionResult.PASS, player.getHeldItem(hand));
    }
}
