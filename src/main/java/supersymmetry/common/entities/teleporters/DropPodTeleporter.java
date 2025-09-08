package supersymmetry.common.entities.teleporters;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ITeleporter;
import supersymmetry.common.entities.EntityDropPod;

public class DropPodTeleporter implements ITeleporter {
    @Override
    public void placeEntity(World world, Entity entity, float yaw) {
        EntityDropPod dropPod = new EntityDropPod(world, entity.posX, entity.posY, entity.posZ);
        world.spawnEntity(dropPod);
        entity.startRiding(dropPod);
    }
}
