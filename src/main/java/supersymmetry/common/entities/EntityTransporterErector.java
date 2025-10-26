package supersymmetry.common.entities;

import net.minecraft.nbt.NBTTagCompound;

import cam72cam.immersiverailroading.entity.Freight;
import cam72cam.mod.entity.Entity;
import cam72cam.mod.entity.boundingbox.IBoundingBox;
import cam72cam.mod.entity.sync.TagSync;
import cam72cam.mod.serialization.TagField;
import supersymmetry.client.renderer.handler.IAlwaysRender;
import supersymmetry.integration.immersiverailroading.registry.TransporterErectorDefinition;

public class EntityTransporterErector extends Freight implements IAlwaysRender {

    @TagField("isRocketLoaded")
    @TagSync
    private boolean isRocketLoaded;
    @TagField("lifterAngle")
    @TagSync
    private float lifterAngle = (float) 0;
    @TagField("liftingMode")
    @TagSync
    private LiftingMode liftingMode = LiftingMode.STOP;
    @TagField("rocketNBT")
    @TagSync
    private NBTTagCompound rocketNBT;

    // In radians per tick
    private double liftingSpeed = 0.087 / 20;

    public EntityTransporterErector() {
        this.setRocketLoaded(true);
    }

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

    public boolean isRocketLoaded() {
        return isRocketLoaded;
    }

    public void setRocketLoaded(boolean rocketLoaded) {
        isRocketLoaded = rocketLoaded;
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
        return rocketNBT;
    }

    public float getLifterAngle() {
        return lifterAngle;
    }

    @Override
    public void onTick() {
        super.onTick();

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
}
