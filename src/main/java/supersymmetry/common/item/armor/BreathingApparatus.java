package supersymmetry.common.item.armor;

import gregtech.api.items.metaitem.stats.IItemDurabilityManager;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;
import supersymmetry.api.items.IBreathingArmorLogic;
import supersymmetry.client.renderer.handler.BreathingApparatusModel;
import supersymmetry.client.renderer.handler.ITextureRegistrar;
import supersymmetry.client.renderer.handler.SimpleBreathingApparatusModel;
import supersymmetry.common.event.DimensionBreathabilityHandler;
import supersymmetry.common.item.SuSyArmorItem;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.inventory.EntityEquipmentSlot.CHEST;
import static supersymmetry.api.util.SuSyUtility.susyId;

public class BreathingApparatus implements IBreathingArmorLogic, IItemDurabilityManager, ITextureRegistrar {
    @SideOnly(Side.CLIENT)
    protected ModelBiped model;

    protected final EntityEquipmentSlot SLOT;

    public BreathingApparatus(EntityEquipmentSlot slot) {
        SLOT = slot;
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
    public @Nullable ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot, ModelBiped defaultModel) {
        if (model == null)
            model = new SimpleBreathingApparatusModel("gas", armorSlot);
        return model;
    }
    @Override
    public boolean mayBreatheWith(ItemStack stack, EntityPlayer player) {
        if (player.dimension != DimensionBreathabilityHandler.BENEATH_ID) {
            return false;
        }
        ItemStack chest = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (chest.getItem() instanceof SuSyArmorItem item) {
            if (item.getItem(chest).getArmorLogic() instanceof BreathingApparatus tank) {
                return tank.getOxygen(stack) > 0;
            }
        }
        return false;
    }

    @Override
    public double tryTick(ItemStack stack, EntityPlayer player) {
        if (!DimensionBreathabilityHandler.isInHazardousEnvironment(player)) {
            return 0;
        }

        ItemStack chest = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (chest.getItem() instanceof SuSyArmorItem item) {
            if (item.getItem(chest).getArmorLogic() instanceof BreathingApparatus tank) {
                tank.changeOxygen(stack, -1);
                return 0;
            }
        }
        return 0.5;
    }

    @Override
    public void addInformation(ItemStack stack, List<String> tooltips) {
        if (getEquipmentSlot(stack) == CHEST) {
            int oxygen = (int) getOxygen(stack);
            int maxOxygen = (int) getMaxOxygen(stack);
            tooltips.add(I18n.format("supersymmetry.oxygen", oxygen, maxOxygen));
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
            stack.setTagCompound(new NBTTagCompound());
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
        NBTTagCompound compound = stack.getTagCompound();
        compound.setDouble("oxygen", getOxygen(stack) + oxygenChange);
        stack.setTagCompound(compound);
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
