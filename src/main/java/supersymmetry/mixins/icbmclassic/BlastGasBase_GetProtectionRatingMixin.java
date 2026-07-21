package supersymmetry.mixins.icbmclassic;

import icbm.classic.content.blast.gas.BlastGasBase;
import ladysnake.gaspunk.GasPunkConfig;
import ladysnake.gaspunk.item.ItemGasMask;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * ICBM now respects gaspunk config file.
 *
 * reuses pre-existing protection system that was just here all this time apparently
 *
 */
@Mixin(value = BlastGasBase.class, remap = false)
public abstract class BlastGasBase_GetProtectionRatingMixin {

    @Inject(
            method = "getProtectionRating",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void susy$injectGasPunkProtectionRating(EntityLivingBase entity,
                                                    CallbackInfoReturnable<Float> cir) {
        // Fast path: vanilla GasPunk ItemGasMask.
        // uses default hard-coded value
        Item helmet = entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem();
        if (helmet instanceof ItemGasMask) {
            cir.setReturnValue(0.75f);
            return;
        }

        float bestProtection = -1.0f; // sentinel: no match found yet

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
                    matches = susy$matchesSlot(suit[0], entity, EntityEquipmentSlot.HEAD)
                            && susy$matchesSlot(suit[1], entity, EntityEquipmentSlot.CHEST);
                    break;
                case 3:
                    matches = susy$matchesSlot(suit[0], entity, EntityEquipmentSlot.HEAD)
                            && susy$matchesSlot(suit[1], entity, EntityEquipmentSlot.CHEST)
                            && susy$matchesSlot(suit[2], entity, EntityEquipmentSlot.LEGS);
                    break;
                case 4:
                    matches = susy$matchesSlot(suit[0], entity, EntityEquipmentSlot.HEAD)
                            && susy$matchesSlot(suit[1], entity, EntityEquipmentSlot.CHEST)
                            && susy$matchesSlot(suit[2], entity, EntityEquipmentSlot.LEGS)
                            && susy$matchesSlot(suit[3], entity, EntityEquipmentSlot.FEET);
                    break;
                default:
                    continue;
            }

            if (matches && maskStrength > bestProtection) {
                bestProtection = maskStrength;
            }
        }

        // Only override if we matched something. Negative sentinel = fall through to ICBM.
        if (bestProtection >= 0.0f) {
            cir.setReturnValue(bestProtection);
        }
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
