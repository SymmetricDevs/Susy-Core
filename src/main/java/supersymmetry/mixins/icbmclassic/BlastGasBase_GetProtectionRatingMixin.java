package supersymmetry.mixins.icbmclassic;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import icbm.classic.content.blast.gas.BlastGasBase;
import ladysnake.gaspunk.GasPunkConfig;
import ladysnake.gaspunk.item.ItemGasMask;
import supersymmetry.common.faction.EntityNativeGasResistance;

/**
 * ICBM now respects gaspunk config file.
 *
 * reuses pre-existing protection system that was just here all this time apparently
 *
 * Also respects the mob resistance whitelist (SusyGasPunkConfig.mobGasResistance)
 * so whitelisted mobs get their built-in protection rating here too, even with nothing equipped,
 * taking whichever of mask protection or mob resistance is higher.
 */
@Mixin(value = BlastGasBase.class, remap = false)
public abstract class BlastGasBase_GetProtectionRatingMixin {

    @Inject(
            method = "getProtectionRating",
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void susy$injectGasPunkProtectionRating(EntityLivingBase entity,
                                                    CallbackInfoReturnable<Float> cir) {
        float bestProtection = -1.0f; // sentinel: no match found yet

        float mobResistance = susy$getMobResistance(entity);
        if (mobResistance > bestProtection) {
            bestProtection = mobResistance;
        }

        // Fast path: vanilla GasPunk ItemGasMask.
        // uses default hard-coded value
        Item helmet = entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem();
        if (helmet instanceof ItemGasMask) {
            if (0.75f > bestProtection) {
                bestProtection = 0.75f;
            }
            cir.setReturnValue(bestProtection);
            return;
        }

        for (String alt : GasPunkConfig.otherGasMasks) {
            String slotsPart;
            float maskStrength;

            int eqIdx = alt.lastIndexOf('=');
            if (eqIdx >= 0) {
                slotsPart = alt.substring(0, eqIdx);
                try {
                    maskStrength = Float.parseFloat(alt.substring(eqIdx + 1).trim());
                } catch (NumberFormatException e) {
                    continue;
                }
            } else {
                slotsPart = alt;
                maskStrength = 0.5f; // default
            }

            if (maskStrength <= bestProtection) continue; // can't improve on what we have

            String[] suit = slotsPart.split("&");
            boolean matches;

            switch (suit.length) {
                case 1:
                    matches = susy$matchesSlot(suit[0], entity, EntityEquipmentSlot.HEAD);
                    break;
                case 2:
                    matches = susy$matchesSlot(suit[0], entity, EntityEquipmentSlot.HEAD) &&
                            susy$matchesSlot(suit[1], entity, EntityEquipmentSlot.CHEST);
                    break;
                case 3:
                    matches = susy$matchesSlot(suit[0], entity, EntityEquipmentSlot.HEAD) &&
                            susy$matchesSlot(suit[1], entity, EntityEquipmentSlot.CHEST) &&
                            susy$matchesSlot(suit[2], entity, EntityEquipmentSlot.LEGS);
                    break;
                case 4:
                    matches = susy$matchesSlot(suit[0], entity, EntityEquipmentSlot.HEAD) &&
                            susy$matchesSlot(suit[1], entity, EntityEquipmentSlot.CHEST) &&
                            susy$matchesSlot(suit[2], entity, EntityEquipmentSlot.LEGS) &&
                            susy$matchesSlot(suit[3], entity, EntityEquipmentSlot.FEET);
                    break;
                default:
                    continue;
            }

            if (matches && maskStrength > bestProtection) {
                bestProtection = maskStrength;
            }
        }

        // Only override if we matched something (mask, config entry, or mob whitelist).
        // Negative sentinel = fall through to ICBM.
        if (bestProtection >= 0.0f) {
            cir.setReturnValue(bestProtection);
        }
    }

    private static float susy$getMobResistance(EntityLivingBase entity) {
        ResourceLocation key = EntityList.getKey(entity);
        if (key == null) return 0.0f;
        String registryName = key.toString();

        for (String entry : EntityNativeGasResistance.mobGasResistance) {
            String namePart;
            float resistance;

            int eqIdx = entry.lastIndexOf('=');
            if (eqIdx >= 0) {
                namePart = entry.substring(0, eqIdx);
                try {
                    resistance = Float.parseFloat(entry.substring(eqIdx + 1).trim());
                } catch (NumberFormatException e) {
                    continue;
                }
            } else {
                namePart = entry;
                resistance = 1.0f;
            }

            if (namePart.trim().equals(registryName)) {
                return resistance;
            }
        }

        return 0.0f;
    }

    private static boolean susy$matchesSlot(String token, EntityLivingBase entity,
                                            EntityEquipmentSlot slot) {
        if (token.equals("*")) return true;

        ItemStack stack = entity.getItemStackFromSlot(slot);
        if (stack.isEmpty()) return false;

        String registryName = String.valueOf(stack.getItem().getRegistryName());

        int firstColon = token.indexOf(':');
        if (firstColon < 0) return false;

        int secondColon = token.indexOf(':', firstColon + 1);

        if (secondColon < 0) {
            return registryName.equals(token);
        } else {
            String tokenName = token.substring(0, secondColon);
            if (!registryName.equals(tokenName)) return false;

            try {
                return stack.getItemDamage() == Integer.parseInt(token.substring(secondColon + 1));
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }
}
