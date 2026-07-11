package supersymmetry.common.event;

import net.minecraft.entity.Entity;

public class DimensionRidingSwapData {

    public Entity mount;
    public Entity passenger;
    public long time;
    // Set once the player has actually been moved into the mount's dimension. The
    // re-mount is deferred until MOUNT_DELAY ticks after this, so the client has time
    // to finish its two-phase world/player reload before SPacketSetPassengers arrives.
    public boolean transferred = false;
    public long transferTime = 0;

    public DimensionRidingSwapData(Entity mount, Entity passenger) {
        this.mount = mount;
        this.passenger = passenger;
        this.time = mount.world.getTotalWorldTime();
    }
}
