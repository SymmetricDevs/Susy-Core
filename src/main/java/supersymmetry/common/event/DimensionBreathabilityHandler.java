package supersymmetry.common.event;

import gregtech.api.damagesources.DamageSources;
import gregtech.api.unification.material.Materials;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import supersymmetry.api.util.SuSyDamageSources;
import supersymmetry.common.item.SuSyArmorItem;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static net.minecraft.inventory.EntityEquipmentSlot.*;

public final class DimensionBreathabilityHandler {

    private static final Map<Integer, BreathabilityInfo> dimensionBreathabilityMap = new HashMap<>();

    private DimensionBreathabilityHandler() {}

    public static void loadConfig() {

        dimensionBreathabilityMap.clear();

        // Nether
        dimensionBreathabilityMap.put(-1, new DimensionBreathabilityHandler.BreathabilityInfo(SuSyDamageSources.getToxicAtmoDamage(), 2));
        // Beneath
        dimensionBreathabilityMap.put(10, new DimensionBreathabilityHandler.BreathabilityInfo(SuSyDamageSources.getSuffocationDamage(), 1));

    }


    public static boolean tickAir(EntityPlayer player, FluidStack oxyStack) {
        // don't drain if we are in creative
        if (player.isCreative()) return true;
        Optional<IFluidHandlerItem> tank = player.inventory.mainInventory.stream()
                .map(a -> a.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null))
                .filter(Objects::nonNull)
                .filter(a -> {
                    FluidStack drain = a.drain(oxyStack, false);
                    return drain != null && drain.amount > 0;
                }).findFirst();
        return tank.isPresent();
    }

    public static boolean isInHazardousEnvironment(EntityPlayer player) {
        return dimensionBreathabilityMap.containsKey(player.dimension);
    }

    public static void tickPlayer(EntityPlayer player) {
        if (isInHazardousEnvironment(player)) {
            if (player.getItemStackFromSlot(HEAD).getItem() instanceof SuSyArmorItem item) {
                if (item.isValid(player.getItemStackFromSlot(HEAD), player.dimension)) {
                    double remainingDamage = item.tryTick(player.getItemStackFromSlot(HEAD), player, player.dimension);
                    if (remainingDamage > 0) {
                        player.getItemStackFromSlot(HEAD).damageItem((int) remainingDamage, player);
                    }
                    return;
                }
            }
            dimensionBreathabilityMap.get(player.dimension).damagePlayer(player);
        }
    }


    public static final class BreathabilityInfo {
        public DamageSource damageType;
        public double defaultDamage;

        public BreathabilityInfo(DamageSource damageType, double defaultDamage) {
            this.damageType = damageType;
            this.defaultDamage = defaultDamage;
        }

        public void damagePlayer(EntityPlayer player) {
            player.attackEntityFrom(damageType, (float) defaultDamage);
        }
    }
}
