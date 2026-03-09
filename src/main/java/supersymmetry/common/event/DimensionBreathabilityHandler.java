package supersymmetry.common.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import supersymmetry.api.space.CelestialObjects;
import supersymmetry.api.util.SuSyDamageSources;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.item.SuSyArmorItem;

import java.util.HashMap;
import java.util.Map;

import static net.minecraft.inventory.EntityEquipmentSlot.HEAD;

public final class DimensionBreathabilityHandler {

    private static final Map<Integer, BreathabilityInfo> dimensionBreathabilityMap = new HashMap<>();

    private static final BreathabilityInfo SPACE = new BreathabilityInfo(SuSyDamageSources.DEPRESSURIZATION, 3);
    public static final int BENEATH_ID = 10;
    public static final int NETHER_ID = -1;

    public static final double ABSORB_ALL = -1;

    private DimensionBreathabilityHandler() {}

    public static void loadConfig() {
        dimensionBreathabilityMap.clear();

        // Nether
        dimensionBreathabilityMap.put(-1, new BreathabilityInfo(SuSyDamageSources.getToxicAtmoDamage(), 2));
        // Beneath
        dimensionBreathabilityMap.put(10, new BreathabilityInfo(SuSyDamageSources.getSuffocationDamage(), 0.5));
        // SPACE
        dimensionBreathabilityMap.put(CelestialObjects.MOON.getDimension(), SPACE);
    }

    public static boolean isInHazardousEnvironment(EntityPlayer player) {
        return dimensionBreathabilityMap.containsKey(player.dimension);
    }

    public static void tickPlayer(EntityPlayer player) {
        if (isInHazardousEnvironment(player)) {
            BreathabilityInfo info = dimensionBreathabilityMap.get(player.dimension);
            if (info.damageType == SuSyDamageSources.DEPRESSURIZATION) {
                if (findOxygen(player)) {
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

    public static boolean findOxygen(EntityPlayer player) {
        World world = player.getEntityWorld();
        AxisAlignedBB aabb = player.getEntityBoundingBox().expand(2, 2, 2).expand(-2, -2, -2);
        for (BlockPos pos : BlockPos.getAllInBox(new BlockPos(aabb.minX, aabb.minY, aabb.minZ), new BlockPos(aabb.maxX, aabb.maxY, aabb.maxZ))) {
            if (world.getBlockState(pos).getBlock() == SuSyBlocks.BREATHING_GAS) {
                return true;
            }
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
