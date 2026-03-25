package supersymmetry.common.item.behavior;

import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentTranslation;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import supersymmetry.api.sound.SusySounds;
import supersymmetry.common.event.DimensionBreathabilityHandler;
import supersymmetry.common.world.atmosphere.AtmosphereWorldData;

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
        if (!(entity instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) entity;
        if (player.world.isRemote) return;
        if (player.ticksExisted % CHECK_INTERVAL != 0) return;

        IElectricItem electricItem = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electricItem == null) return;

        boolean isHeld = player.getHeldItemMainhand() == itemStack || player.getHeldItemOffhand() == itemStack;

        boolean inHazard = DimensionBreathabilityHandler.isInDepressurizationHazard(player);
        double pressure = !inHazard ? 1 : AtmosphereWorldData.get(player.getEntityWorld()).getGraph()
                .getOxygenation(player.getPosition());
        boolean insufficient = pressure < 0.1;

        long euCost = EU_PER_CHECK;
        if (isHeld) euCost += EU_PER_DISPLAY;
        if (insufficient) euCost += EU_PER_BEEP;

        if (electricItem.discharge(euCost, Integer.MAX_VALUE, true, false, false) < euCost) return;

        if (isHeld) {
            String level = getOxygenLevel(pressure);
            player.sendStatusMessage(
                    new TextComponentTranslation("metaitem.oxygen_sensor.oxygen_level." + level), true);
        }

        if (insufficient) {
            player.world.playSound(null, player.posX, player.posY, player.posZ,
                    SusySounds.OXYGEN_SENSOR_BEEP, SoundCategory.PLAYERS, 1.0F, 1.0F);
        }
    }

    private String getOxygenLevel(double pressure) {
        if (pressure < 0.01) return "none";
        if (pressure < 0.1) return "very_low";
        if (pressure < 0.3) return "low";
        if (pressure < 0.7) return "medium";
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
