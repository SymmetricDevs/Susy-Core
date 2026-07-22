package supersymmetry.common.metatileentities.single.electric;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import gregtech.api.capability.GregtechCapabilities;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.GTValues;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import supersymmetry.common.entities.EntityDropPod;

public class MetaTileEntityFederationDropBeacon extends TieredMetaTileEntity {

    private static final int INTEL_SAMPLES_REQUIRED = 10; //do not overdo this
    private static final double OUTLIER_THRESHOLD = 64.0;
    private static final int PODS_MIN = 2;
    private static final int PODS_MAX = 4;
    private static final int POD_SPAWN_HEIGHT = 300;
    private static final int POD_SPREAD_RADIUS = 4;

    private UUID targetPlayerUUID = null;
    //private UUID targetPlayerUUID = UUID.fromString("31c4910d-9b69-4725-8969-9ed53ac8a7dc");

    private final List<Vec3d> positionSamples = new ArrayList<>();

    private int intelCount = 0;
    boolean launched = false;

    public MetaTileEntityFederationDropBeacon(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityFederationDropBeacon(this.metaTileEntityId, this.getTier());
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
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("intelCount", intelCount);
        if (targetPlayerUUID != null) {
            data.setString("targetUUID", targetPlayerUUID.toString());
        }

        NBTTagList sampleList = new NBTTagList();
        for (Vec3d sample : positionSamples) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setDouble("sx", sample.x);
            tag.setDouble("sz", sample.z);
            sampleList.appendTag(tag);
        }
        data.setTag("positionSamples", sampleList);

        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        intelCount = data.getInteger("intelCount");
        if (data.hasKey("targetUUID")) {
            targetPlayerUUID = UUID.fromString(data.getString("targetUUID"));
        }

        positionSamples.clear();
        NBTTagList sampleList = (NBTTagList) data.getTag("positionSamples");
        if (sampleList != null) {
            for (int i = 0; i < sampleList.tagCount(); i++) {
                NBTTagCompound tag = sampleList.getCompoundTagAt(i);
                positionSamples.add(new Vec3d(tag.getDouble("sx"), 0, tag.getDouble("sz")));
            }
        }
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
        this.energyContainer.removeEnergy(this.energyContainer.getEnergyCapacity());

        if (targetPlayerUUID == null) return;

        onChargeCycleComplete(getWorld());

        if (this.launched){
            this.doExplosion(0);
        }
    }

    private void onChargeCycleComplete(World world) {
        EntityPlayer player = world.getPlayerEntityByUUID(targetPlayerUUID);

        if (player != null) {
            // Sample the player's current XZ position, attempts to find center of factory based on average player pos
            // effectiveness up for debate
            positionSamples.add(new Vec3d(Math.floor(player.posX), 0, Math.floor(player.posZ)));

            intelCount++;

            if (intelCount >= INTEL_SAMPLES_REQUIRED) {
                if (!positionSamples.isEmpty()) {
                    Vec3d strikeTarget = computeStrikeTarget();
                    launchStrike(world, strikeTarget);
                    launched = true;
                }
            }
            markDirty();
        }
    }

    private Vec3d computeStrikeTarget() {
        double sumX = 0, sumZ = 0;
        for (Vec3d v : positionSamples) {
            sumX += v.x;
            sumZ += v.z;
        }
        double meanX = sumX / positionSamples.size();
        double meanZ = sumZ / positionSamples.size();

        //small filter in case the player leaves the area which produces noise
        List<Vec3d> filtered = new ArrayList<>();
        for (Vec3d v : positionSamples) {
            double dx = v.x - meanX;
            double dz = v.z - meanZ;
            if (Math.sqrt(dx * dx + dz * dz) <= OUTLIER_THRESHOLD) {
                filtered.add(v);
            }
        }

        // security measure in case filter fucks up
        if (filtered.isEmpty()) {
            return new Vec3d(meanX, 0, meanZ);
        }

        // cleanup
        double cleanSumX = 0, cleanSumZ = 0;
        for (Vec3d v : filtered) {
            cleanSumX += v.x;
            cleanSumZ += v.z;
        }
        return new Vec3d(cleanSumX / filtered.size(), 0, cleanSumZ / filtered.size());
    }

    private void launchStrike(World world, Vec3d target) {
        int podCount = PODS_MIN + GTValues.RNG.nextInt(PODS_MAX - PODS_MIN + 1);

        List<String> landingCommands = buildLandingCommands();

        for (int i = 0; i < podCount; i++) {
            double angle = GTValues.RNG.nextDouble() * 2 * Math.PI;
            double radius = GTValues.RNG.nextDouble() * POD_SPREAD_RADIUS;
            double spawnX = Math.floor(target.x + Math.cos(angle) * radius) + 0.5;
            double spawnZ = Math.floor(target.z + Math.sin(angle) * radius) + 0.5;
            double spawnY = POD_SPAWN_HEIGHT + GTValues.RNG.nextInt(10); //apparently this is better than math rand, not gonna question it

            EntityDropPod pod = new EntityDropPod(world, spawnX, spawnY, spawnZ);
            pod.canExplode(false);
            pod.setCommandsOnLanding(new ArrayList<>(landingCommands));
            world.spawnEntity(pod);
        }
    }

    private List<String> buildLandingCommands() {
        List<String> commands = new ArrayList<>();
        try {
            Object result = org.codehaus.groovy.runtime.InvokerHelper.invokeMethod(
                    MetaTileEntityFederationDropBeacon.class, "getFedStrikeCommands", new Object[] {});
            if (result instanceof List) {
                for (Object cmd : (List<?>) result) {
                    if (cmd instanceof String) commands.add((String) cmd);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return commands;
    }

    //WIP
    public void setTargetPlayerUUID(UUID uuid) {
        this.targetPlayerUUID = uuid;
        markDirty();
    }

    public UUID getTargetPlayerUUID() {
        return targetPlayerUUID;
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
