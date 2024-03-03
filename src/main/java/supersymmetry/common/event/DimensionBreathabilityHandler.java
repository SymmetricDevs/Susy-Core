package supersymmetry.common.event;

import gregtech.api.damagesources.DamageSources;
import gregtech.api.unification.material.Materials;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import supersymmetry.api.util.SuSyDamageSources;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static net.minecraft.inventory.EntityEquipmentSlot.*;

public final class DimensionBreathabilityHandler {

    private static FluidStack oxyStack;
    private static final Map<Integer, BreathabilityInfo> dimensionBreathabilityMap = new HashMap<>();
    private static BreathabilityInfo defaultDimensionBreathability;
    private static final Map<BreathabilityItemMapKey, BreathabilityInfo> itemBreathabilityMap = new HashMap<>();

    private static boolean hasDrainedOxy = false;
    private static boolean hasSuffocated = false;

    private DimensionBreathabilityHandler() {}

    public static void loadConfig() {
        oxyStack = Materials.Oxygen.getFluid(1);

        dimensionBreathabilityMap.clear();
        defaultDimensionBreathability = new BreathabilityInfo(false, false, -1, -1);

        // Nether
        dimensionBreathabilityMap.put(-1, new DimensionBreathabilityHandler.BreathabilityInfo(true, true, 1, 0));
        // Beneath
        dimensionBreathabilityMap.put(10, new DimensionBreathabilityHandler.BreathabilityInfo(true, true, 4, 0));


        itemBreathabilityMap.clear();
        itemBreathabilityMap.put(new BreathabilityItemMapKey(Item.getByNameOrId("susy_armor_item"), 0),
                new BreathabilityInfo(true, false, 10, 0));
        itemBreathabilityMap.put(new BreathabilityItemMapKey(Item.getByNameOrId("susy_armor_item"), 1),
                new BreathabilityInfo(true, true, 15, 0));
    }

    public static void tickPlayer(EntityPlayer player) {
        BreathabilityInfo dimInfo = dimensionBreathabilityMap.get(player.dimension);
        if (dimInfo == null) {
            dimInfo = defaultDimensionBreathability;
        }
        if (dimInfo.suffocation) suffocationCheck(player);
        if (dimInfo.toxic) toxicityCheck(player, dimInfo.toxicityRating);
        if (dimInfo.radiation) radiationCheck(player, dimInfo.radiationRating);
        hasDrainedOxy = false;
        hasSuffocated = false;
    }

    public static boolean isInHazardousEnvironment(EntityPlayer player) {
        BreathabilityInfo dimInfo = dimensionBreathabilityMap.get(player.dimension);
        if (dimInfo == null) {
            dimInfo = defaultDimensionBreathability;
        }
        return dimInfo.suffocation || dimInfo.toxic || dimInfo.radiation;
    }

    private static void suffocationCheck(EntityPlayer player) {
        BreathabilityInfo itemInfo = itemBreathabilityMap.get(getItemKey(player, HEAD));
        if (itemInfo != null && itemInfo.suffocation && tickAir(player)) return;
        suffocate(player);
    }

    private static void suffocate(EntityPlayer player) {
        if (hasSuffocated) return;
        player.attackEntityFrom(SuSyDamageSources.getSuffocationDamage(), 2);
        hasSuffocated = true;
    }

    private static void toxicityCheck(EntityPlayer player, int dimRating) {
        BreathabilityInfo itemInfo = itemBreathabilityMap.get(getItemKey(player, HEAD));
        if (itemInfo != null && itemInfo.toxic) {
            // if sealed, no need for toxicity check
            if (itemInfo.isSealed) {
                if (tickAir(player)) return;
                suffocate(player);
            } else if (dimRating > itemInfo.toxicityRating) {
                causeToxicDamage(player, dimRating - itemInfo.toxicityRating);
                return;
            }
        }
        causeToxicDamage(player, 100);
    }

    private static void causeToxicDamage(EntityPlayer player, int mult) {
        player.attackEntityFrom(SuSyDamageSources.getToxicAtmoDamage(), 0.03f * mult);
    }

    private static void radiationCheck(EntityPlayer player, int dimRating) {
        // natural radiation protection of 20
        int ratingSum = 20;

        BreathabilityInfo itemInfo = itemBreathabilityMap.get(getItemKey(player, HEAD));
        if (itemInfo != null && itemInfo.radiation) ratingSum += itemInfo.radiationRating;
        itemInfo = itemBreathabilityMap.get(getItemKey(player, CHEST));
        if (itemInfo != null && itemInfo.radiation) ratingSum += itemInfo.radiationRating;
        itemInfo = itemBreathabilityMap.get(getItemKey(player, LEGS));
        if (itemInfo != null && itemInfo.radiation) ratingSum += itemInfo.radiationRating;
        itemInfo = itemBreathabilityMap.get(getItemKey(player, FEET));
        if (itemInfo != null && itemInfo.radiation) ratingSum += itemInfo.radiationRating;

        if (dimRating > ratingSum) radiate(player, dimRating - ratingSum);
    }

    private static void radiate(EntityPlayer player, int mult) {
        player.attackEntityFrom(DamageSources.getRadioactiveDamage(), 0.01f * mult);
    }

    private static BreathabilityItemMapKey getItemKey(EntityPlayer player, EntityEquipmentSlot slot) {
        return new BreathabilityItemMapKey(player.getItemStackFromSlot(slot));
    }

    private static boolean tickAir(EntityPlayer player) {
        // don't drain if we are in creative
        if (player.isCreative()) return true;
        Optional<IFluidHandlerItem> tank = player.inventory.mainInventory.stream()
                .map(a -> a.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null))
                .filter(Objects::nonNull)
                .filter(a -> {
                    FluidStack drain = a.drain(oxyStack, false);
                    return drain != null && drain.amount > 0;
                }).findFirst();
        // don't drain if we've already drained
        if (!hasDrainedOxy) {
            tank.ifPresent(a -> a.drain(oxyStack, true));
            hasDrainedOxy = true;
        }
        return tank.isPresent();
    }

    public void addBreathabilityItem(ItemStack item, BreathabilityInfo info) {
        itemBreathabilityMap.put(new BreathabilityItemMapKey(item), info);
    }

    public void removeBreathabilityItem(ItemStack item) {
        itemBreathabilityMap.remove(new BreathabilityItemMapKey(item));
    }

    private static final class BreathabilityItemMapKey {

        public final Item item;
        public final int meta;

        BreathabilityItemMapKey(ItemStack stack) {
            this.item = stack.getItem();
            this.meta = stack.getMetadata();
        }

        BreathabilityItemMapKey(Item item, int meta) {
            this.item = item;
            this.meta = meta;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BreathabilityItemMapKey that = (BreathabilityItemMapKey) o;
            return meta == that.meta && Objects.equals(item, that.item);
        }

        @Override
        public int hashCode() {
            return Objects.hash(item, meta);
        }
    }

    public static final class BreathabilityInfo {

        public final boolean suffocation;
        public final boolean toxic;
        public final boolean radiation;

        private final int toxicityRating;
        private final int radiationRating;

        public final boolean isSealed;

        public BreathabilityInfo(boolean suffocation, boolean isSealed, int toxic, int radiation) {
            this.suffocation = suffocation;
            this.toxic = toxic != -1;
            this.radiation = toxic != -1;
            this.radiationRating = radiation;
            this.toxicityRating = toxic;
            this.isSealed = isSealed;
        }
    }
}
