package supersymmetry.common.event;

import static net.minecraft.inventory.EntityEquipmentSlot.HEAD;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

import supersymmetry.api.space.CelestialObjects;
import supersymmetry.api.util.SuSyDamageSources;
import supersymmetry.common.item.SuSyArmorItem;
import supersymmetry.common.world.atmosphere.AtmosphereWorldData;

public final class DimensionBreathabilityHandler {

    private static final Map<Integer, List<BreathabilityInfo>> dimensionBreathabilityMap = new HashMap<>();

    private static final BreathabilityInfo SPACE = new BreathabilityInfo(SuSyDamageSources.DEPRESSURIZATION, 4);
    public static final int BENEATH_ID = 10;
    public static final int NETHER_ID = -1;

    public static final double ABSORB_ALL = -1;

    private DimensionBreathabilityHandler() {}

    public static void loadConfig() {
        dimensionBreathabilityMap.clear();

        // Nether
        addHazard(-1, new BreathabilityInfo(SuSyDamageSources.getToxicAtmoDamage(), 2).ignoreMobs());
        // Beneath
        addHazard(10, new BreathabilityInfo(SuSyDamageSources.getSuffocationDamage(), 0.5).ignoreMobs());
        // SPACE
        addHazard(CelestialObjects.MOON.getDimension(), SPACE);
    }

    public static void addHazard(int dim, BreathabilityInfo info) {
        dimensionBreathabilityMap.compute(dim, (d, list) -> {
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(info);

            return list;
        });
    }

    public static boolean isInHazardousEnvironment(Entity player) {
        return dimensionBreathabilityMap.containsKey(player.dimension);
    }

    public static void tickEntity(Entity entity) {
        if (isInHazardousEnvironment(entity)) {
            for (BreathabilityInfo info : dimensionBreathabilityMap.get(entity.dimension)) {
                if (info.ignoresMobs && entity instanceof EntityMob) {
                    continue;
                }
                if (info.damageType == SuSyDamageSources.DEPRESSURIZATION) {
                    if (AtmosphereWorldData.get(entity.getEntityWorld()).getGraph()
                            .getOxygenation(entity.getPosition()) >= 0.1) {
                        continue;
                    }
                } else if (info.damageType == SuSyDamageSources.DARKNESS) {
                    if (entity.getBrightness() > 0.05F) {
                        continue;
                    }
                }
                if (entity instanceof EntityPlayer player) {
                    if (player.getItemStackFromSlot(HEAD).getItem() instanceof SuSyArmorItem item) {
                        if (item.isValid(player.getItemStackFromSlot(HEAD), player)) {
                            double damageAbsorbed = item.getDamageAbsorbed(player.getItemStackFromSlot(HEAD), player);
                            if (damageAbsorbed != ABSORB_ALL)
                                info.damagePlayer(player, damageAbsorbed);
                            return;
                        }
                    }
                }
                info.damagePlayer(entity);
            }
        }
    }

    public static boolean isInDepressurizationHazard(EntityPlayer player) {
        List<BreathabilityInfo> infos = dimensionBreathabilityMap.get(player.dimension);
        if (infos == null)
            return false;
        for (BreathabilityInfo info : infos) {
            if (info.damageType == SuSyDamageSources.DEPRESSURIZATION)
                return true;
        }
        return false;
    }

    public static boolean isInDepressurizationHazard(World world) {
        List<BreathabilityInfo> infos = dimensionBreathabilityMap.get(world.provider.getDimension());
        if (infos == null)
            return false;
        for (BreathabilityInfo info : infos) {
            if (info.damageType == SuSyDamageSources.DEPRESSURIZATION)
                return true;
        }
        return false;
    }

    public static final class BreathabilityInfo {

        public DamageSource damageType;
        public double defaultDamage;
        public boolean ignoresMobs;

        public BreathabilityInfo(DamageSource damageType, double defaultDamage) {
            this.damageType = damageType;
            this.defaultDamage = defaultDamage;
        }

        public void damagePlayer(Entity player) {
            damagePlayer(player, 0);
        }

        public void damagePlayer(Entity player, double amountAbsorbed) {
            if (ignoresMobs && player instanceof EntityMob) return;
            if (defaultDamage > amountAbsorbed) {
                player.attackEntityFrom(damageType, (float) defaultDamage - (float) amountAbsorbed);
            }
        }

        public BreathabilityInfo ignoreMobs() {
            ignoresMobs = true;
            return this;
        }
    }
}
