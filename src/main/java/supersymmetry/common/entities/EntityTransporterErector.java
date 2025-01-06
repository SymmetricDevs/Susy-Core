package supersymmetry.common.entities;

import cam72cam.immersiverailroading.entity.Freight;
import cam72cam.immersiverailroading.inventory.SlotFilter;
import cam72cam.mod.entity.Entity;
import supersymmetry.integration.immersiverailroading.registry.TransporterErectorDefinition;

public class EntityTransporterErector extends Freight {

    private boolean isRocketLoaded = true;

    public EntityTransporterErector() {
    }

    public TransporterErectorDefinition getDefinition() {
        return super.getDefinition(TransporterErectorDefinition.class);
    }

    @Override
    public int getInventorySize() {
        return 1;
    }

    @Override
    public int getInventoryWidth() {
        return 1;
    }

    @Override
    protected void initContainerFilter() {
        this.cargoItems.filter.clear();
        this.cargoItems.filter.put(0, SlotFilter.NONE);
    }

    public boolean isRocketLoaded() {
        return isRocketLoaded;
    }

    public void setRocketLoaded(boolean rocketLoaded) {
        isRocketLoaded = rocketLoaded;
    }

    @Override
    public boolean canFitPassenger(Entity passenger) {
        return false;
    }
}
