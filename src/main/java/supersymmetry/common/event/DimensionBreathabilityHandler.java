package supersymmetry.common.event;

import static net.minecraft.inventory.EntityEquipmentSlot.HEAD;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import supersymmetry.api.space.CelestialObjects;
import supersymmetry.api.util.SuSyDamageSources;
import supersymmetry.common.blocks.BlockBreathingGas;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.item.SuSyArmorItem;

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
        addHazard(-1, new BreathabilityInfo(SuSyDamageSources.getToxicAtmoDamage(), 2));
        // Beneath
        addHazard(10, new BreathabilityInfo(SuSyDamageSources.getSuffocationDamage(), 0.5));
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

    public static boolean isInHazardousEnvironment(EntityPlayer player) {
        return dimensionBreathabilityMap.containsKey(player.dimension);
    }

    public static void tickPlayer(EntityPlayer player) {
        if (isInHazardousEnvironment(player)) {
            for (BreathabilityInfo info : dimensionBreathabilityMap.get(player.dimension)) {
                if (info.damageType == SuSyDamageSources.DEPRESSURIZATION) {
                    if (countBreathingGas(player, BlockBreathingGas.GasType.OXYGEN, 2) == 2) {
                        return;
                    }
                } else if (info.damageType == SuSyDamageSources.DARKNESS) {
                    if (player.getBrightness() > 0.05F ||
                            countBreathingGas(player, BlockBreathingGas.GasType.PESTICIDE, 2) == 2) {
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
                info.damagePlayer(player);
            }
        }
    }

    public static boolean isInDepressurizationHazard(EntityPlayer player) {
        List<BreathabilityInfo> infos = dimensionBreathabilityMap.get(player.dimension);
        if (infos == null) return false;
        for (BreathabilityInfo info : infos) {
            if (info.damageType == SuSyDamageSources.DEPRESSURIZATION) return true;
        }
        return false;
    }

    public static int countBreathingGas(EntityPlayer player, BlockBreathingGas.GasType type, int stopAt) {
        World world = player.getEntityWorld();
        AxisAlignedBB aabb = new AxisAlignedBB(player.getPosition(), player.getPosition())
                .expand(3, 3, 3).expand(-3, -3, -3);
        int count = 0;
        for (BlockPos pos : BlockPos.getAllInBox(new BlockPos(aabb.minX, aabb.minY, aabb.minZ),
                new BlockPos(aabb.maxX, aabb.maxY, aabb.maxZ))) {
            IBlockState state = world.getBlockState(pos);
            if (state.getBlock() == SuSyBlocks.BREATHING_GAS && SuSyBlocks.BREATHING_GAS.getState(state) == type) {
                count++;
                if (count == stopAt) {
                    break;
                }
            }
        }
        return count;
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
