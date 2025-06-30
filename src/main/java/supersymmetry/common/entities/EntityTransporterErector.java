package supersymmetry.common.entities;

import cam72cam.immersiverailroading.entity.Freight;
import cam72cam.mod.entity.Entity;
import cam72cam.mod.entity.sync.TagSync;
import cam72cam.mod.serialization.TagField;
import supersymmetry.integration.immersiverailroading.registry.TransporterErectorDefinition;

public class EntityTransporterErector extends Freight {
    @TagField("borerAngle")
    @TagSync
    private boolean isRocketLoaded;
    @TagField("lifterAngle")
    @TagSync
    private float lifterAngle;
    @TagField("lifting")
    @TagSync
    private boolean lifting;

    // In radians per tick
    private double liftingSpeed = 0.087/20;

    public EntityTransporterErector() {
        this.setRocketLoaded(false);
    }

    public TransporterErectorDefinition getDefinition() {
        return super.getDefinition(TransporterErectorDefinition.class);
    }

    @Override
    public int getInventorySize() {
        return 2;
    }

    @Override
    public int getInventoryWidth() {
        return 2;
    }

    @Override
    protected void initContainerFilter() {
        this.cargoItems.filter.clear();
        //this.cargoItems.filter.put(0, SlotFilter.NONE);
    }

    public boolean isRocketLoaded() {
        return !this.cargoItems.get(0).isEmpty();
        //return isRocketLoaded;
    }

    public void setRocketLoaded(boolean rocketLoaded) {
        isRocketLoaded = rocketLoaded;
    }

    public boolean isLifting() {
        return !this.cargoItems.get(1).isEmpty();
        //return lifting;
    }

    public void setLifting(boolean lifting) {
        this.lifting = lifting;
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
        if(isLifting()) {
            if(this.getCurrentSpeed().isZero()) {
                if (this.lifterAngle + liftingSpeed < Math.PI / 2){
                    this.lifterAngle += liftingSpeed;
                } else {
                    this.lifterAngle = (float) Math.PI/2;
                    this.setLifting(false);
                }
            }
        }
    }
}
