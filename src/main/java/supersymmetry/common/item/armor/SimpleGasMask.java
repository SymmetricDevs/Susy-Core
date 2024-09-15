package supersymmetry.common.item.armor;

import gregtech.api.items.armor.ArmorMetaItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;

import gregtech.api.items.metaitem.stats.IItemDurabilityManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import supersymmetry.api.items.IBreathingArmorLogic;
import supersymmetry.common.event.DimensionBreathabilityHandler;

public class SimpleGasMask implements IBreathingArmorLogic, IItemDurabilityManager {
    @Override
    public EntityEquipmentSlot getEquipmentSlot(ItemStack itemStack) {
        return EntityEquipmentSlot.HEAD;
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return "gregtech:textures/armor/simple_gas_mask.png";
    }

    @Override
    public void addToolComponents(ArmorMetaItem.ArmorMetaValueItem mvi) {
        mvi.addComponents(new IItemBehaviour() {

            @Override
            public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
                return onRightClick(world, player, hand);
            }
        });
    }

    public ActionResult<ItemStack> onRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (player.getHeldItem(hand).getItem() instanceof ArmorMetaItem) {
            ItemStack armor = player.getHeldItem(hand);
            if (armor.getItem() instanceof ArmorMetaItem &&
                    player.inventory.armorInventory.get(EntityEquipmentSlot.HEAD.getIndex()).isEmpty() &&
                    !player.isSneaking()) {
                player.inventory.armorInventory.set(EntityEquipmentSlot.HEAD.getIndex(), armor.copy());
                player.setHeldItem(hand, ItemStack.EMPTY);
                player.playSound(new SoundEvent(new ResourceLocation("item.armor.equip_generic")), 1.0F, 1.0F);
                return ActionResult.newResult(EnumActionResult.SUCCESS, armor);
            }
        }

        return ActionResult.newResult(EnumActionResult.PASS, player.getHeldItem(hand));
    }



    @Override
    public double getDurabilityForDisplay(ItemStack itemStack) {
        return 1 - getDamage(itemStack);
    }

    @Override
    public boolean canBreakWithDamage(ItemStack stack) {
        return getDamage(stack) >= 1;
    }

    @Override
    public boolean mayBreatheWith(ItemStack stack, EntityPlayer player) {
        return getDamage(stack) < 1;
    }


    @Override
    public boolean isValidArmor(ItemStack itemStack, Entity entity, EntityEquipmentSlot equipmentSlot) {
        return true;
    }

    @Override
    public double tryTick(ItemStack stack, EntityPlayer player) {
        if (DimensionBreathabilityHandler.isInHazardousEnvironment(player)) {
            changeDamage(stack, 1. / (60. * 20.)); // It's actually ticked every overall second, not just every tick.
        }
        if (getDamage(stack) >= 1) {
            player.renderBrokenItemStack(stack);
            stack.shrink(1);
            player.setItemStackToSlot(EntityEquipmentSlot.HEAD, ItemStack.EMPTY);
        }
        return 0;
    }

    private double getDamage(ItemStack stack) {
        if (stack.getTagCompound() == null) {
            stack.setTagCompound(new NBTTagCompound());
        }
        if (!stack.getTagCompound().hasKey("damage")) {
            stack.getTagCompound().setDouble("damage", 0);
        }
        return stack.getTagCompound().getDouble("damage");
    }

    private void changeDamage(ItemStack stack, double damageChange) {
        NBTTagCompound compound = stack.getTagCompound();
        compound.setDouble("damage", getDamage(stack) + damageChange);
        stack.setTagCompound(compound);
    }




}
