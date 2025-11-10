package supersymmetry.common.item.armor;

import static net.minecraft.inventory.EntityEquipmentSlot.*;
import static supersymmetry.api.util.SuSyUtility.susyId;
import static supersymmetry.common.event.DimensionBreathabilityHandler.ABSORB_ALL;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ISpecialArmor;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.damagesources.DamageSources;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import supersymmetry.api.items.IGeoMetaArmor;
import supersymmetry.client.renderer.handler.GeoMetaArmorRenderer;
import supersymmetry.common.event.DimensionBreathabilityHandler;
import supersymmetry.common.item.SuSyArmorItem;

public class AdvancedBreathingApparatus extends BreathingApparatus implements IGeoMetaArmor {

    private static final double DEFAULT_ABSORPTION = 0;
    private final double hoursOfLife;
    protected final String name;
    private final int tier;
    private final double relativeAbsorption;
    // We don't really use animations, but this can't be null
    // Luckily this isn't instanced for every itemStack so we're fine here
    private AnimationFactory factory;

    public AdvancedBreathingApparatus(EntityEquipmentSlot slot, double hoursOfLife, String name, int tier,
                                      double relativeAbsorption) {
        super(slot);
        this.hoursOfLife = hoursOfLife;
        this.name = name;
        this.tier = tier;
        this.relativeAbsorption = relativeAbsorption;
    }

    @Override
    public boolean mayBreatheWith(ItemStack stack, EntityPlayer player) {
        return player.dimension == DimensionBreathabilityHandler.BENEATH_ID ||
                player.dimension == DimensionBreathabilityHandler.NETHER_ID;
    }

    @Override
    public double getDamageAbsorbed(ItemStack stack, EntityPlayer player) {
        this.handleDamage(stack, player);

        ItemStack chest = player.getItemStackFromSlot(CHEST);
        if (chest.getItem() instanceof SuSyArmorItem item) {
            if (item.getItem(chest).getArmorLogic() instanceof AdvancedBreathingApparatus tank && tank.tier == tier) {
                tank.handleDamage(chest, player);

                int piecesCount = 0;
                ItemStack leggings = player.getItemStackFromSlot(LEGS);
                if (leggings.getItem() instanceof SuSyArmorItem item2) {
                    if (item2.getItem(leggings).getArmorLogic() instanceof AdvancedBreathingApparatus legLogic) {
                        legLogic.handleDamage(leggings, player);
                        piecesCount++;
                    }
                }

                ItemStack boots = player.getItemStackFromSlot(FEET);
                if (boots.getItem() instanceof SuSyArmorItem item2) {
                    if (item2.getItem(boots).getArmorLogic() instanceof AdvancedBreathingApparatus bootLogic) {
                        bootLogic.handleDamage(boots, player);
                        piecesCount++;
                    }
                }

                if (tank.getOxygen(chest) <= 0) {
                    return DEFAULT_ABSORPTION;
                } else {
                    tank.changeOxygen(chest, -1.);
                }
                switch (piecesCount) {
                    case 0:
                        return 0.5;
                    case 1:
                        return 1;
                    case 2:
                        return ABSORB_ALL;
                }
            }
        }
        return DEFAULT_ABSORPTION;
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

    private void handleDamage(ItemStack stack, EntityPlayer player) {
        if (hoursOfLife == 0 || player.dimension == DimensionBreathabilityHandler.BENEATH_ID) {
            return; // No damage
        }
        double amount = (1. / (60. * 60. * hoursOfLife));
        changeDamage(stack, amount); // It's actually ticked every overall second, not just every tick.
        if (getDamage(stack) >= 1) {
            player.renderBrokenItemStack(stack);
            stack.shrink(1);
            player.setItemStackToSlot(HEAD, ItemStack.EMPTY);
        }
    }

    @Override
    public double getDurabilityForDisplay(ItemStack itemStack) {
        if (SLOT == CHEST && getMaxOxygen(itemStack) != -1) {
            return getOxygen(itemStack) / getMaxOxygen(itemStack);
        } else {
            if (hoursOfLife > 0) {
                return 1 - getDamage(itemStack);
            }
        }
        return 1;
    }

    @Override
    public float getHeatResistance() {
        return 0.25F;
    }

    @Override
    public ISpecialArmor.ArmorProperties getProperties(EntityLivingBase player, @NotNull ItemStack armor,
                                                       DamageSource source,
                                                       double damage, EntityEquipmentSlot equipmentSlot) {
        ISpecialArmor.ArmorProperties prop = new ISpecialArmor.ArmorProperties(0, 0.0, 0);
        if (source.isUnblockable())
            return prop;

        if (source == DamageSources.getHeatDamage())
            return new ISpecialArmor.ArmorProperties(0, 0.25, 5);
        if (source == DamageSources.getFrostDamage())
            return new ISpecialArmor.ArmorProperties(0, 0.20, 2);
        if (source == DamageSource.IN_FIRE)
            return new ISpecialArmor.ArmorProperties(0, 0.10, 2);
        if (source == DamageSource.ON_FIRE)
            return new ISpecialArmor.ArmorProperties(0, 0.0750, 2);
        if (source == DamageSource.LAVA)
            return new ISpecialArmor.ArmorProperties(0, 0.0375, 2);

        prop.Armor = getAbsorption(armor) * relativeAbsorption * 20;
        return prop;
    }

    protected float getAbsorption(ItemStack itemStack) {
        return getAbsorption(getEquipmentSlot(itemStack));
    }

    protected float getAbsorption(EntityEquipmentSlot slot) {
        return switch (slot) {
            case HEAD, FEET -> 0.15F;
            case CHEST -> 0.4F;
            case LEGS -> 0.3F;
            default -> 0.0F;
        };
    }

    public void addInformation(ItemStack stack, List<String> strings) {
        if (hoursOfLife > 0) {
            double lifetime = 60 * 60 * hoursOfLife;
            int secondsRemaining = (int) (lifetime - getDamage(stack) * lifetime);
            strings.add(I18n.format("supersymmetry.seconds_left", secondsRemaining));
        } else {
            strings.add(I18n.format("supersymmetry.unlimited"));
        }

        int armor = (int) Math.round(20.0F * this.getAbsorption(this.SLOT) * this.relativeAbsorption);
        if (armor > 0)
            strings.add(I18n.format("attribute.modifier.plus.0", armor, I18n.format("attribute.name.generic.armor")));
    }

    @Override
    public int getArmorDisplay(EntityPlayer player, ItemStack armor, int slot) {
        return (int) Math.round(20.0F * this.getAbsorption(armor) * relativeAbsorption);
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return textureRL().toString();
    }

    @Nullable
    @SideOnly(Side.CLIENT)
    @Override
    public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack,
                                    EntityEquipmentSlot armorSlot, ModelBiped defaultModel) {
        return GeoMetaArmorRenderer.INSTANCE
                .setCurrentItem(entityLiving, itemStack, armorSlot)
                .applyEntityStats(defaultModel)
                .applySlot(armorSlot);
    }

    @Override
    public List<ResourceLocation> getTextureLocations() {
        return Collections.emptyList();
    }

    @Override
    public void registerControllers(AnimationData data) {
        /* Do nothing */
    }

    @Override
    public AnimationFactory getFactory() {
        if (this.factory == null) {
            this.factory = new AnimationFactory(this);
        }
        return this.factory;
    }

    @Override
    public String getGeoName() {
        return name + "_armor";
    }

    // No animation needed
    @Override
    public ResourceLocation animationRL() {
        return susyId("animations/dummy.animation.json");
    }
}
