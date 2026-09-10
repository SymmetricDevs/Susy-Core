package supersymmetry.common.metatileentities.single.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
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
    private final OrientedOverlayRenderer overlay;

    public MetaTileEntityFederationReinforcementBeacon(ResourceLocation metaTileEntityId, OrientedOverlayRenderer overlay, int tier) {
        super(metaTileEntityId, tier);
        this.overlay = overlay;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityFederationReinforcementBeacon(this.metaTileEntityId, this.overlay, this.getTier());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.overlay.renderOrientedState(
                renderState,
                translation,
                pipeline,
                Cuboid6.full,
                getFrontFacing(),
                true,
                true
        );
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
