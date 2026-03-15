package supersymmetry.common.item.behavior;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentTranslation;
import supersymmetry.api.sound.SusySounds;
// import supersymmetry.common.blocks.BlockBreathingGas;
import supersymmetry.common.event.DimensionBreathabilityHandler;

import java.util.List;

public class OxygenSensorBehavior implements IItemBehaviour {

    // Base cost per check (every CHECK_INTERVAL ticks)
    private static final long EU_PER_CHECK = 2L;
    // Extra cost for status display (when held in hand)
    private static final long EU_PER_DISPLAY = 4L;
    // Extra cost for beeping (when insufficient oxygen in hazardous zone)
    private static final long EU_PER_BEEP = 8L;

    private static final int CHECK_INTERVAL = 20; // ticks (1 second)

    @Override
    public void onUpdate(ItemStack itemStack, Entity entity) {
        //i dont know whats meant to happen here and i failed to merge it correctly sorry


        // if (!(entity instanceof EntityPlayer player)) return;
        // if (player.world.isRemote) return;
        // if (player.ticksExisted % CHECK_INTERVAL != 0) return;
        //
        // IElectricItem electricItem = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        // if (electricItem == null) return;
        //
        // boolean isHeld = player.getHeldItemMainhand() == itemStack
        //         || player.getHeldItemOffhand() == itemStack;
        //
        // boolean inHazard = DimensionBreathabilityHandler.isInDepressurizationHazard(player);
        // int oxygenCount = DimensionBreathabilityHandler.countBreathingGas(player, BlockBreathingGas.GasType.OXYGEN, -1);
        // boolean insufficient = inHazard && oxygenCount < 2;
        //
        // long euCost = EU_PER_CHECK;
        // if (isHeld) euCost += EU_PER_DISPLAY;
        // if (insufficient) euCost += EU_PER_BEEP;
        //
        // if (electricItem.discharge(euCost, Integer.MAX_VALUE, true, false, false) < euCost) return;
        //
        // if (isHeld) {
        //     String level = getOxygenLevel(oxygenCount);
        //     player.sendStatusMessage(
        //             new TextComponentTranslation("metaitem.oxygen_sensor.oxygen_level." + level), true);
        // }
        //
        // if (insufficient) {
        //     player.world.playSound(null, player.posX, player.posY, player.posZ,
        //             SusySounds.OXYGEN_SENSOR_BEEP, SoundCategory.PLAYERS, 1.0F, 1.0F);
        // }
    }

    private String getOxygenLevel(int count) {
        if (count == 0) return "none";
        if (count < 2) return "very_low";
        if (count < 5) return "low";
        if (count < 9) return "medium";
        return "high";
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        IElectricItem electricItem = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electricItem != null) {
            lines.add(I18n.format("metaitem.oxygen_sensor.tooltip.charge",
                    electricItem.getCharge(), electricItem.getMaxCharge()));
        }
        lines.add(I18n.format("metaitem.oxygen_sensor.tooltip.1"));
    }
}
