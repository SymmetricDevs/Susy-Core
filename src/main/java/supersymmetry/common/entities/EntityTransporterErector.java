package supersymmetry.common.entities;

import cam72cam.immersiverailroading.entity.Freight;
import cam72cam.mod.entity.Entity;
import cam72cam.mod.entity.sync.TagSync;
import cam72cam.mod.serialization.TagField;
import net.minecraft.util.math.Vec3d;
import supersymmetry.integration.immersiverailroading.registry.TransporterErectorDefinition;

public class EntityTransporterErector extends Freight {
    @TagField("borerAngle")
    @TagSync
    private boolean isRocketLoaded;
    @TagField("lifterAngle")
    @TagSync
    private float lifterAngle = (float) Math.PI/4;
    @TagField("liftingMode")
    @TagSync
    private LiftingMode liftingMode = LiftingMode.STOP;

    // In radians per tick
    private double liftingSpeed = 0.087/20;

    public EntityTransporterErector() {
        this.setRocketLoaded(true);
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

    public float getLifterAngle() {
        return lifterAngle;
    }

    @Override
    public void onTick() {
        super.onTick();

        if(this.getCurrentSpeed().isZero()) {
            switch (this.liftingMode) {
                case UP:
                    if (this.lifterAngle + liftingSpeed < Math.PI / 2){
                        this.lifterAngle += liftingSpeed;
                    } else {
                        this.lifterAngle = (float) Math.PI/2;
                        this.setLiftingMode(LiftingMode.STOP);
                        if(!this.getWorld().internal.isRemote) this.tryReleaseRocket();
                    }
                    break;
                case DOWN:
                    if (this.lifterAngle - liftingSpeed >= 0){
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

        if (this.isRocketLoaded && this.internal.ticksExisted >= 0) {
            this.setLiftingMode(LiftingMode.UP);
        }

        if (!this.isRocketLoaded && this.internal.ticksExisted >= 1000) {
            this.setLiftingMode(LiftingMode.DOWN);
        }
    }

    public void tryReleaseRocket() {
        if (this.isRocketLoaded()) {
            Vec3d offset = new Vec3d(0,-5.5, 11.8);
            offset = offset.rotateYaw((float) (this.getRotationYaw() / 180. * Math.PI));
            Vec3d position = this.getPosition().internal().add(offset);
            EntityRocket rocket = new EntityRocket(this.getWorld().internal, position, this.getRotationYaw() + 45);
            this.getWorld().internal.spawnEntity(rocket);
            this.setRocketLoaded(false);
        }
    }

    public enum LiftingMode{
        UP,
        DOWN,
        STOP;
    }
}
