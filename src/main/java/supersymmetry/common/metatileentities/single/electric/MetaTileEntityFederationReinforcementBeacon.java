package supersymmetry.common.metatileentities.single.electric;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supersymmetry.common.entities.EntityDropPod;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class MetaTileEntityFederationReinforcementBeacon extends TieredMetaTileEntity {

    private static final int PODS_MIN = 8;
    private static final int PODS_MAX = 12;
    private static final int POD_SPAWN_HEIGHT = 300;
    private static final int POD_SPREAD_RADIUS = 4;

    public static Function<World, EntityLiving> fedPayloadProvider = null;
    public static Consumer<EntityLiving> fedPostSpawnModifier = null;

    public MetaTileEntityFederationReinforcementBeacon(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityFederationReinforcementBeacon(this.metaTileEntityId, this.getTier());
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER && side != null) {
            return null;
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void update() {
        super.update();

        this.energyContainer.changeEnergy(GTValues.VH[getTier() - 1]);
        if (this.energyContainer.getEnergyStored() < this.energyContainer.getEnergyCapacity()) return;
        if (getWorld().isRemote) return;

        // Drain stored energy
        this.energyContainer.removeEnergy(this.energyContainer.getEnergyCapacity());

        // Launch pods targeting the beacon's location
        BlockPos pos = getPos();
        launchStrike(getWorld(), pos.getX() + 0.5, pos.getZ() + 0.5);

        // Self-destruct beacon block
        this.doExplosion(0);
    }

    private void launchStrike(World world, double targetX, double targetZ) {
        int podCount = PODS_MIN + GTValues.RNG.nextInt(PODS_MAX - PODS_MIN + 1);

        for (int i = 0; i < podCount; i++) {
            double angle = GTValues.RNG.nextDouble() * 2 * Math.PI;
            double radius = GTValues.RNG.nextDouble() * POD_SPREAD_RADIUS;
            double spawnX = Math.floor(targetX + Math.cos(angle) * radius) + 0.5;
            double spawnZ = Math.floor(targetZ + Math.sin(angle) * radius) + 0.5;
            double spawnY = POD_SPAWN_HEIGHT + (int)(Math.random() * 80);

            EntityDropPod pod = new EntityDropPod(world);
            pod.canExplode(false);
            pod.rotationYaw = (float) (Math.random() * 360);
            pod.setPosition(spawnX, spawnY, spawnZ);

            if (fedPayloadProvider != null) {
                EntityLiving mob = fedPayloadProvider.apply(world);
                if (mob != null) {
                    mob.setPosition(spawnX, spawnY, spawnZ);

                    // 1. First run initial spawn (Techguns generates base gear/stats here)
                    mob.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(mob)), null);
                    mob.startRiding(pod, true);

                    // 2. Apply custom weapon & NBT overrides AFTER onInitialSpawn completes
                    if (fedPostSpawnModifier != null) {
                        fedPostSpawnModifier.accept(mob);
                    }

                    mob.enablePersistence();

                    world.spawnEntity(pod);
                    world.spawnEntity(mob);
                    continue;
                }
            }

            world.spawnEntity(pod);
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        tooltip.add(I18n.format("susy.machine.federation_drop_beacon.tooltip.info"));
        tooltip.add(I18n.format("susy.machine.federation_drop_beacon.tooltip.description"));
        tooltip.add(I18n.format("susy.machine.federation_drop_beacon.tooltip.description1"));
        tooltip.add(I18n.format("susy.machine.federation_drop_beacon.tooltip.description2"));
    }

    @Override
    public boolean getIsWeatherOrTerrainResistant() {
        return true;
    }
}
