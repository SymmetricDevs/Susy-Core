package supersymmetry.common.entities;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;

import cam72cam.immersiverailroading.entity.Freight;
import cam72cam.immersiverailroading.util.Speed;
import cam72cam.mod.entity.Entity;
import cam72cam.mod.entity.boundingbox.IBoundingBox;
import cam72cam.mod.entity.sync.TagSync;
import cam72cam.mod.math.Vec3i;
import cam72cam.mod.serialization.TagCompound;
import cam72cam.mod.serialization.TagField;
import supersymmetry.client.renderer.handler.IAlwaysRender;
import supersymmetry.common.metatileentities.multi.rocket.MetaTileEntityRocketAssembler;
import supersymmetry.integration.immersiverailroading.registry.TransporterErectorDefinition;

public class EntityTransporterErector extends Freight implements IAlwaysRender {

    // Fraction (0..1) of the rocket that has been assembled. 0 = no rocket present,
    // 1 = fully built. Drives the partial sweep render on the transporter erector.
    @TagField("assemblyProgress")
    @TagSync
    private float assemblyProgress = 0f;
    @TagField("nextAssemblyProgress")
    @TagSync
    private float nextAssemblyProgress = 0f;
    // Interpolation helper variables
    @TagField("start")
    @TagSync
    private float start = 0f;
    @TagField("end")
    @TagSync
    private float end = 0f;

    @TagField("lifterAngle")
    @TagSync
    private float lifterAngle = (float) 0;
    @TagField("liftingMode")
    @TagSync
    private LiftingMode liftingMode = LiftingMode.STOP;
    @TagField(value = "rocketNBT")
    @TagSync
    private TagCompound rocketNBT = new TagCompound();

    // In radians per tick
    private double liftingSpeed = 0.087 / 20;
    private MetaTileEntityRocketAssembler assembler;

    public EntityTransporterErector() {}

    @Override
    public IBoundingBox getBounds() {
        return super.getBounds();
    }

    @Override
    public int getInventorySize() {
        return 0;
    }

    @Override
    public int getInventoryWidth() {
        return 0;
    }

    public TransporterErectorDefinition getDefinition() {
        return super.getDefinition(TransporterErectorDefinition.class);
    }

    /** True only once the rocket is fully assembled (a partially-built rocket is not "loaded"). */
    public boolean isRocketLoaded() {
        return assemblyProgress >= 1f;
    }

    public void setRocketLoaded(boolean rocketLoaded) {
        nextAssemblyProgress = assemblyProgress = rocketLoaded ? 1f : 0f;
    }

    public float getVisualAssemblyProgress(float renderTime) {
        // Safely lerp (even if start = end)
        float interpTime = Math.clamp((renderTime - start) / Math.max((end - start), 0.000001f), 0, 1);
        return (1 - interpTime) * assemblyProgress + interpTime * nextAssemblyProgress;
    }

    public float getAssemblyProgress() {
        return assemblyProgress;
    }

    // Set the assembly progress to a specific value, and the assembler for tracking progress more closely
    public void setAssemblyProgress(float assemblyProgress, float nextAssemblyProgress, float start, float end) {
        this.assemblyProgress = Math.clamp(assemblyProgress, 0f, 1f);
        this.nextAssemblyProgress = Math.clamp(nextAssemblyProgress, 0f, 1f);
        this.start = start;
        this.end = end;
    }

    public LiftingMode getLiftingMode() {
        return liftingMode;
    }

    public void setLiftingMode(LiftingMode liftingMode) {
        this.liftingMode = liftingMode;
    }

    @Override
    public boolean canFitPassenger(Entity passenger) {
        return false;
    }

    public NBTTagCompound getRocketNBT() {
        return rocketNBT.internal;
    }

    public float getLifterAngle() {
        return lifterAngle;
    }

    @Override
    public void onTick() {
        super.onTick();
        this.setCurrentSpeed(Speed.ZERO);
        if (this.getCurrentSpeed().isZero()) {
            switch (this.liftingMode) {
                case UP:
                    if (this.lifterAngle + liftingSpeed < Math.PI / 2) {
                        this.lifterAngle += liftingSpeed;
                    } else {
                        this.lifterAngle = (float) Math.PI / 2;
                        this.setLiftingMode(LiftingMode.STOP);
                        // if (!this.getWorld().internal.isRemote) this.tryReleaseRocket();
                    }
                    break;
                case DOWN:
                    if (this.lifterAngle - liftingSpeed >= 0) {
                        this.lifterAngle -= liftingSpeed;
                    } else {
                        this.lifterAngle = (float) 0;
                        this.setLiftingMode(LiftingMode.STOP);
                    }
                    break;
                case STOP:
                    break;
            }
        }
    }

    public enum LiftingMode {
        UP,
        DOWN,
        STOP;
    }

    @Override
    public double getDirectFrictionNewtons(List<Vec3i> track) {
        if (this.lifterAngle > 0 || this.liftingMode == LiftingMode.UP) {
            return 1000000;
        }
        return super.getDirectFrictionNewtons(track);
    }

    @Override
    public double getWeight() {
        // Rocket mass scales with how much of it has been assembled.
        return super.getWeight() - 311500 * (1 - this.assemblyProgress);
    }
}
