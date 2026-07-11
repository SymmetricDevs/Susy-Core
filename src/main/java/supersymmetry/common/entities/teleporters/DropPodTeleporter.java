package supersymmetry.common.entities.teleporters;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ITeleporter;

public class DropPodTeleporter implements ITeleporter {

    @Override
    public void placeEntity(World world, Entity entity, float yaw) {
        // Do NOT spawn the entity here. Entity#changeDimension hands us the ORIGINAL entity and then spawns
        // a COPY of it itself. If we spawn as well, both instances share a UUID, so WorldServer#canAddEntity
        // rejects the copy ("Keeping entity that already exists with UUID"): the wrong (soon-to-be-dead)
        // instance stays in the world and changeDimension returns a detached copy. We only need to position
        // the entity so changeDimension's `new BlockPos(this)` picks the correct landing spot.
        entity.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw,
                entity.rotationPitch);
    }
}
