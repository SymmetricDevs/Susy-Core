package supersymmetry.common.faction;

import net.minecraft.entity.EntityLiving;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import supersymmetry.Supersymmetry;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID)
public class GrenadeAIHandler {

    private static final Map<String, Integer> THROWABLE_ITEM_CHARGE_TICKS = new HashMap<>();
    static {
        THROWABLE_ITEM_CHARGE_TICKS.put("gaspunk:grenade", 35);
        THROWABLE_ITEM_CHARGE_TICKS.put("icbmclassic:grenade", 50);
        THROWABLE_ITEM_CHARGE_TICKS.put("techguns:fraggrenade", 40);
        //items that can be thrown (hold down right click and release)
    }

    private static final int DEFAULT_CHARGE_TICKS = 40;
    private static final int MAX_SAFE_CHARGE_TICKS = 50;

    private static Map<Item, Integer> resolvedChargeTicks = null;

    private static final double THROW_RANGE = 20.0D;

    private static Map<Item, Integer> getResolvedChargeTicks() {
        if (resolvedChargeTicks == null) {
            Map<Item, Integer> resolved = new HashMap<>();
            for (Map.Entry<String, Integer> entry : THROWABLE_ITEM_CHARGE_TICKS.entrySet()) {
                Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(entry.getKey()));
                if (item != null) {
                    int ticks = Math.min(entry.getValue(), MAX_SAFE_CHARGE_TICKS);
                    resolved.put(item, ticks);
                }
            }
            resolvedChargeTicks = resolved;
        }
        return resolvedChargeTicks;
    }

    static boolean isThrowable(ItemStack stack) {
        return !stack.isEmpty() && getResolvedChargeTicks().containsKey(stack.getItem());
    }

    static int getChargeTicks(ItemStack stack) {
        if (stack.isEmpty()) return DEFAULT_CHARGE_TICKS;
        return getResolvedChargeTicks().getOrDefault(stack.getItem(), DEFAULT_CHARGE_TICKS);
    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getSlot() != EntityEquipmentSlot.MAINHAND) return;
        if (event.getEntity().world.isRemote) return;
        if (!(event.getEntityLiving() instanceof EntityLiving)) return;

        EntityLiving mob = (EntityLiving) event.getEntityLiving();
        boolean nowHoldsThrowable = isThrowable(event.getTo());

        boolean hasThrowTask = mob.tasks.taskEntries.stream()
                .anyMatch(e -> e.action instanceof EntityAIThrowGrenade);

        if (nowHoldsThrowable && !hasThrowTask) {
            mob.tasks.addTask(2, new EntityAIThrowGrenade(mob, THROW_RANGE));
        } else if (!nowHoldsThrowable && hasThrowTask) {
            mob.tasks.taskEntries.stream()
                    .filter(e -> e.action instanceof EntityAIThrowGrenade)
                    .findFirst()
                    .ifPresent(e -> mob.tasks.removeTask(e.action));
        }
    }
}
