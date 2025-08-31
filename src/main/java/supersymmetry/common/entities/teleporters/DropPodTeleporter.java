package supersymmetry.common.entities.teleporters;

import gregtech.api.util.GTTeleporter;
import gregtech.api.util.TeleportHandler;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.ITeleporter;
import supersymmetry.common.entities.EntityDropPod;

public class DropPodTeleporter implements ITeleporter {
    @Override
    public void placeEntity(World world, Entity entity, float yaw) {
        EntityDropPod dropPod = new EntityDropPod(entity.getEntityWorld(), entity.posX, entity.posY + 256, entity.posZ);
        entity.getEntityWorld().spawnEntity(dropPod);
        entity.startRiding(dropPod);
    }
}
