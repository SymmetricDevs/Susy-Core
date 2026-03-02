package supersymmetry.common.event;

import net.minecraft.entity.Entity;

public class DimensionRidingSwapData {

    public Entity mount;
    public Entity passenger;
    public long time;

    public DimensionRidingSwapData(Entity mount, Entity passenger) {
        this.mount = mount;
        this.passenger = passenger;
        this.time = mount.world.getTotalWorldTime();
    }
}
