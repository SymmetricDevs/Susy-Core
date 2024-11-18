package supersymmetry.common.entities;

import cam72cam.immersiverailroading.entity.Freight;
import cam72cam.immersiverailroading.inventory.SlotFilter;
import supersymmetry.integration.immersiverailroading.registry.TransporterErectorDefinition;

public class EntityTransporterErector extends Freight {

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
        return 0;
    }

    @Override
    protected void initContainerFilter() {
        this.cargoItems.filter.clear();
        this.cargoItems.filter.put(0, SlotFilter.NONE);
    }
}
