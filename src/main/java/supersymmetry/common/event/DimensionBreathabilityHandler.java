package supersymmetry.common.event;

import static net.minecraft.inventory.EntityEquipmentSlot.HEAD;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;

import supersymmetry.api.space.CelestialObjects;
import supersymmetry.api.util.SuSyDamageSources;
import supersymmetry.common.item.SuSyArmorItem;
import supersymmetry.common.world.atmosphere.AtmosphereWorldData;

public final class DimensionBreathabilityHandler {

    private static final Map<Integer, BreathabilityInfo[]> dimensionBreathabilityMap = new HashMap<>();

    private static final BreathabilityInfo SPACE = new BreathabilityInfo(SuSyDamageSources.DEPRESSURIZATION, 3);
    public static final int BENEATH_ID = 10;
    public static final int NETHER_ID = -1;

    public static final double ABSORB_ALL = -1;

    private DimensionBreathabilityHandler() {}

    public static void loadConfig() {
        dimensionBreathabilityMap.clear();

        int[] hello = { 1, 2, 3 };

        // Nether
        dimensionBreathabilityMap.put(-1,
                new BreathabilityInfo[] { new BreathabilityInfo(SuSyDamageSources.getToxicAtmoDamage(),
                        2) });
        // Beneath
        dimensionBreathabilityMap.put(10,
                new BreathabilityInfo[] { new BreathabilityInfo(SuSyDamageSources.getSuffocationDamage(),
                        0.5) });
        // SPACE
        dimensionBreathabilityMap.put(CelestialObjects.MOON.getDimension(), new BreathabilityInfo[] { SPACE });
    }

    public static boolean isInHazardousEnvironment(EntityPlayer player) {
        return dimensionBreathabilityMap.containsKey(player.dimension);
    }

    public static void tickPlayer(EntityPlayer player) {
        if (isInHazardousEnvironment(player)) {
            for (BreathabilityInfo info : dimensionBreathabilityMap.get(player.dimension)) {
                if (info.damageType == SuSyDamageSources.DEPRESSURIZATION) {
                    if (AtmosphereWorldData.get(player.getEntityWorld()).getGraph()
                            .getOxygenation(player.getPosition()) >= 0.1) {
                        return;
                    }
                } else if (info.damageType == SuSyDamageSources.DARKNESS) {
                    if (player.getBrightness() > 0.05F) {
                        return;
                    }
                }
                if (player.getItemStackFromSlot(HEAD).getItem() instanceof SuSyArmorItem item) {
                    if (item.isValid(player.getItemStackFromSlot(HEAD), player)) {
                        double damageAbsorbed = item.getDamageAbsorbed(player.getItemStackFromSlot(HEAD), player);
                        if (damageAbsorbed != ABSORB_ALL)
                            info.damagePlayer(player, damageAbsorbed);
                        return;
                    }
                }
                info.damagePlayer(player, 0);
            }

        }
    }

    public static boolean isInDepressurizationHazard(EntityPlayer player) {
        BreathabilityInfo[] infos = dimensionBreathabilityMap.get(player.dimension);
        if (infos == null) return false;
        for (BreathabilityInfo info : infos) {
            if (info.damageType == SuSyDamageSources.DEPRESSURIZATION) return true;
        }
        return false;
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

        public void damagePlayer(EntityPlayer player, double amountAbsorbed) {
            if (defaultDamage > amountAbsorbed) {
                player.attackEntityFrom(damageType, (float) defaultDamage - (float) amountAbsorbed);
            }
        }
    }
}
