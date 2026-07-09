package supersymmetry.mixins.gaspunk;

import ladysnake.gaspunk.GasPunkConfig;
import ladysnake.gaspunk.api.AbstractGas;
import ladysnake.gaspunk.api.IBreathingHandler;
import ladysnake.gaspunk.api.IGas;
import ladysnake.gaspunk.api.event.GasEvent;
import ladysnake.gaspunk.gas.core.CapabilityBreathing;
import ladysnake.gaspunk.item.ItemGasMask;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Replaces the config gas mask immunity logic in DefaultBreathingHandler#isImmune()
 * with two new features:
 *
 *   1. Item metadata support = config entries can specify a damage/meta value after a second
 *      colon, e.g. "susy_armor:helmet:11=0.5" matches only that specific item meta.
 *      Omitting the meta (e.g. "susy_armor:helmet=0.5") matches any damage value, preserving
 *      backwards compatibility with the original config format.
 *      this is mostly done to make the gas mask logic work with things like the nomex suit,
 *      since we name all of our armor susy_armor:<int> for some reason
 *
 *   2. Mask strength = the "=<float>" suffix on each entry defines a protection threshold.
 *      The entity is immune to a gas only if maskStrength >= the maximum potency across all
 *      of the gas's AgentEffects. By default, all gas masks have a blocking potential of 0.5,
 *      meaning that unless configured they block the shitty tier gases such as carbon monoxide,
 *      but will fail at the stronger ones. Also the vanilla gaspunk gas mask now has a strength of 0.75
 *
 *
 * Config entry format (S:otherGasMasks in gaspunk.cfg):
 *   Single item:  "modid:itemname:meta=strength"       e.g. "susy_armor:cloth_hood:3=0.3"
 *   Full suit:    "head&chest&legs&feet=strength"      e.g. "susy_armor:hood:3&*&*&*=0.5"
 *   Wildcards:    "*" in any slot matches any item in that slot
 *   No meta:      "modid:itemname=strength"            matches any damage value
 *   No strength:  "modid:itemname"                     defaults to strength 1.0 (legacy)
 *
 * Strength semantics:
 *   immune = maskStrength >= maxPotencyOfGas
 */
@Mixin(value = CapabilityBreathing.DefaultBreathingHandler.class, remap = false)
public abstract class CapabilityBreathing_IsImmune_Mixin {

    @Shadow
    @Final
    protected EntityLivingBase owner;

    @Inject(
            method = "isImmune",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void susy$replaceIsImmuneLogic(IGas gas, float concentration,
                                           CallbackInfoReturnable<Boolean> cir) {
            boolean immune = false;

            float gasPotency = 0.0f;
            if (gas instanceof AbstractGas) {
                for (AbstractGas.AgentEffect effect : ((AbstractGas) gas).getAgents()) {
                    if (effect.getPotency() > gasPotency) {
                        gasPotency = effect.getPotency();
                    }
                }
            }

            Item helmet = this.owner.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem();
            float maskStrength;

            if (helmet instanceof ItemGasMask){
                maskStrength = 0.75f; //might be a better idea to put this in config, don't care doe
                if (maskStrength >= gasPotency) {
                    immune = true;
                }
            }
            else {
                for (String alt : GasPunkConfig.otherGasMasks) {
                    String slotsPart;

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
                        maskStrength = 0.5f;
                    }

                    if (maskStrength < gasPotency) {
                        continue;
                    }

                    String[] suit = slotsPart.split("&");
                    boolean fullSuit;

                    switch (suit.length) {
                        case 1:
                            fullSuit = susy$matchesSlot(suit[0], this.owner, EntityEquipmentSlot.HEAD);
                            break;
                        case 2:
                            fullSuit = susy$matchesSlot(suit[0], this.owner, EntityEquipmentSlot.HEAD)
                                    && susy$matchesSlot(suit[1], this.owner, EntityEquipmentSlot.CHEST);
                            break;
                        case 3:
                            fullSuit = susy$matchesSlot(suit[0], this.owner, EntityEquipmentSlot.HEAD)
                                    && susy$matchesSlot(suit[1], this.owner, EntityEquipmentSlot.CHEST)
                                    && susy$matchesSlot(suit[2], this.owner, EntityEquipmentSlot.LEGS);
                            break;
                        case 4:
                            fullSuit = susy$matchesSlot(suit[0], this.owner, EntityEquipmentSlot.HEAD)
                                    && susy$matchesSlot(suit[1], this.owner, EntityEquipmentSlot.CHEST)
                                    && susy$matchesSlot(suit[2], this.owner, EntityEquipmentSlot.LEGS)
                                    && susy$matchesSlot(suit[3], this.owner, EntityEquipmentSlot.FEET);
                            break;
                        default:
                            continue;
                    }

                    immune |= fullSuit;
                }
            }


        GasEvent.GasImmunityEvent event = new GasEvent.GasImmunityEvent(
                this.owner, (IBreathingHandler)(Object)this, gas, concentration, immune
        );
        MinecraftForge.EVENT_BUS.post(event);

        cir.setReturnValue(event.isImmune());
    }

    private static boolean susy$matchesSlot(String token, EntityLivingBase entity,
                                            EntityEquipmentSlot slot) {
        if (token.equals("*")) return true;

        ItemStack stack = entity.getItemStackFromSlot(slot);
        if (stack.isEmpty()) return false;

        String registryName = String.valueOf(stack.getItem().getRegistryName());

        int firstColon = token.indexOf(':');
        if (firstColon < 0) return false; // not a valid registry name

        int secondColon = token.indexOf(':', firstColon + 1);

        if (secondColon < 0) {
            // No meta = registry name match only.
            return registryName.equals(token);
        } else {
            // Meta present = match registry name then damage value.
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
