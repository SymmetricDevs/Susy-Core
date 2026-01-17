package supersymmetry.common.item.armor;

import static net.minecraft.inventory.EntityEquipmentSlot.CHEST;
import static net.minecraft.inventory.EntityEquipmentSlot.HEAD;
import static supersymmetry.api.util.SuSyUtility.susyId;
import static supersymmetry.common.event.DimensionBreathabilityHandler.ABSORB_ALL;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.EnchantmentDurability;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.Nullable;

import gregtech.api.items.metaitem.stats.IItemDurabilityManager;
import supersymmetry.api.items.IBreathingArmorLogic;
import supersymmetry.client.renderer.handler.ITextureRegistrar;
import supersymmetry.client.renderer.handler.SimpleBreathingApparatusModel;
import supersymmetry.common.event.DimensionBreathabilityHandler;
import supersymmetry.common.item.SuSyArmorItem;

public class BreathingApparatus implements IBreathingArmorLogic, IItemDurabilityManager, ITextureRegistrar {

    @SideOnly(Side.CLIENT)
    protected ModelBiped model;

    protected final EntityEquipmentSlot SLOT;

    private static final double DEFAULT_ABSORPTION = 0;

    protected int maxDurability;

    public BreathingApparatus(EntityEquipmentSlot slot, int maxDurability) {
        SLOT = slot;
        this.maxDurability = maxDurability;
    }

    @Override
    public EntityEquipmentSlot getEquipmentSlot(ItemStack itemStack) {
        return SLOT;
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return switch (SLOT) {
            case HEAD -> "susy:textures/armor/gas_mask.png";
            case CHEST -> "susy:textures/armor/gas_tank.png";
            default -> null;
        };
    }

    @SideOnly(Side.CLIENT)
    @Override
    public @Nullable ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack,
                                              EntityEquipmentSlot armorSlot, ModelBiped defaultModel) {
        if (model == null)
            model = new SimpleBreathingApparatusModel("gas", armorSlot);
        return model;
    }

    @Override
    public void damageArmor(EntityLivingBase entity, ItemStack itemStack, DamageSource source, int damage,
                            EntityEquipmentSlot equipmentSlot) {
        itemStack.attemptDamageItem(damage, entity.getRNG(), null);
        if (damage > 0) {
            int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, itemStack);
            int j = 0;

            for (int k = 0; i > 0 && k < damage; ++k) {
                if (EnchantmentDurability.negateDamage(itemStack, i, entity.getRNG())) {
                    ++j;
                }
            }

            damage -= j;

            if (damage <= 0) {
                return;
            }
        }

        changeDurability(itemStack, -damage);
    }

    @Override
    public boolean mayBreatheWith(ItemStack stack, EntityPlayer player) {
        if (player.dimension != DimensionBreathabilityHandler.BENEATH_ID) {
            return false;
        }
        ItemStack chest = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (chest.getItem() instanceof SuSyArmorItem item) {
            if (item.getItem(chest).getArmorLogic() instanceof BreathingApparatus tank) {
                return tank.getOxygen(chest) > 0;
            }
        }
        return false;
    }

    @Override
    public double getDamageAbsorbed(ItemStack stack, EntityPlayer player) {
        if (!DimensionBreathabilityHandler.isInHazardousEnvironment(player)) {
            return ABSORB_ALL;
        }

        ItemStack chest = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (chest.getItem() instanceof SuSyArmorItem item) {
            if (item.getItem(chest).getArmorLogic() instanceof BreathingApparatus tank) {
                tank.changeOxygen(chest, -1);
                return ABSORB_ALL;
            }
        }
        return DEFAULT_ABSORPTION;
    }

    @Override
    public void addInformation(ItemStack stack, List<String> tooltips) {
        if (getEquipmentSlot(stack) == CHEST) {
            int oxygen = (int) getOxygen(stack);
            int maxOxygen = (int) getMaxOxygen(stack);
            tooltips.add(I18n.format("supersymmetry.oxygen", oxygen, maxOxygen));
            tooltips.add(I18n.format("item.durability", getDurability(stack), maxDurability));
        }
    }

    @Override
    public double getDurabilityForDisplay(ItemStack itemStack) {
        if (SLOT == EntityEquipmentSlot.CHEST) {
            return (getOxygen(itemStack) / getMaxOxygen(itemStack));
        } else {
            return 1;
        }
    }

    double getOxygen(ItemStack stack) {
        if (stack.getTagCompound() == null) {
            return 1; // only nomex doesnt have it, everything else should be fine ish..
        }
        if (!stack.getTagCompound().hasKey("oxygen")) {
            stack.getTagCompound().setDouble("oxygen", getMaxOxygen(stack));
        }
        return stack.getTagCompound().getDouble("oxygen");
    }

    double getMaxOxygen(ItemStack stack) {
        if (stack.getTagCompound() == null) {
            stack.setTagCompound(new NBTTagCompound());
        }
        if (!stack.getTagCompound().hasKey("maxOxygen")) {
            stack.getTagCompound().setDouble("maxOxygen", 1200);
        }
        return stack.getTagCompound().getDouble("maxOxygen");
    }

    void changeOxygen(ItemStack stack, double oxygenChange) {
        if (!stack.hasTagCompound()) {
            return;
        } // only nomex doesnt have it
        NBTTagCompound compound = stack.getTagCompound();
        compound.setDouble("oxygen", getOxygen(stack) + oxygenChange);
        stack.setTagCompound(compound);
    }

    int getDurability(ItemStack stack) {
        if (stack.getTagCompound() == null) {
            return 0;
        }
        if (!stack.getTagCompound().hasKey("durability")) {
            stack.getTagCompound().setInteger("durability", maxDurability);
        }
        return stack.getTagCompound().getInteger("durability");
    }

    void changeDurability(ItemStack stack, int durabilityChange) {
        if (!stack.hasTagCompound()) {
            return;
        } // only nomex doesnt have it
        NBTTagCompound compound = stack.getTagCompound();
        compound.setInteger("durability", getDurability(stack) + durabilityChange);
        stack.setTagCompound(compound);
    }

    @Override
    public void onArmorTick(World world, EntityPlayer player, ItemStack itemStack) {
        if (player.getItemStackFromSlot(HEAD) != itemStack) return; // doing that because it would tick all 4 pieces and
        // subtract 4s/s otherwise, no goog
        if (player.isInsideOfMaterial(Material.WATER)) {
            var chest = player.getItemStackFromSlot(CHEST);
            if (chest.getItem() instanceof SuSyArmorItem item) {
                if (item.getItem(chest).getArmorLogic() instanceof BreathingApparatus tank) {
                    if (tank.getOxygen(chest) > 0) {
                        player.setAir(300);
                        if (!DimensionBreathabilityHandler.isInHazardousEnvironment(player)) {
                            changeOxygen(player.getItemStackFromSlot(CHEST), (-1f) / 20);
                            // assuming that if its hazardous the player is already breathing with the suit, so no extra
                            // air is
                            // needed
                        }
                    }
                }
            }
        }
    }

    @Override
    public List<ResourceLocation> getTextureLocations() {
        List<ResourceLocation> models = new ArrayList<>();
        switch (SLOT) {
            case HEAD -> models.add(susyId("armor/gas_mask"));
            case CHEST -> models.add(susyId("armor/gas_tank"));
        }
        return models;
    }
}
