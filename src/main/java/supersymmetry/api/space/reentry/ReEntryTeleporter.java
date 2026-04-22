package supersymmetry.api.space.reentry;

import net.minecraft.entity.Entity;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;

/**
 * Simple teleporter that places the entity at the exact (x, y, z) position
 * inside the target dimension without creating a nether portal.
 */
public class ReEntryTeleporter extends Teleporter {

    private final int targetX;
    private final int targetY;
    private final int targetZ;

    public ReEntryTeleporter(WorldServer world, int x, int y, int z) {
        super(world);
        this.targetX = x;
        this.targetY = y;
        this.targetZ = z;
    }

    @Override
    public void placeInPortal(Entity entity, float yaw) {
        entity.setPosition(targetX + 0.5, targetY, targetZ + 0.5);
    }

    @Override
    public boolean placeInExistingPortal(Entity entity, float yaw) {
        entity.setPosition(targetX + 0.5, targetY, targetZ + 0.5);
        return true;
    }

    @Override
    public boolean makePortal(Entity entity) {
        // No portal needed – we drop directly into the world
        return true;
    }

    @Override
    public void removeStalePortalLocations(long worldTime) {
        // Nothing to clean up
    }
}
