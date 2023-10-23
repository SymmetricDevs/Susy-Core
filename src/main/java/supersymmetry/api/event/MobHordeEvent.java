package supersymmetry.api.event;

import gregtech.api.util.GTTeleporter;
import gregtech.api.util.TeleportHandler;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import supersymmetry.common.entities.EntityDropPod;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class MobHordeEvent {
    private Supplier<EntityLiving> entitySupplier;
    private int quantityMin;
    private int quantityMax;
    private boolean nightOnly;
    private ResourceLocation advancementUnlock;

    public static final List<MobHordeEvent> EVENTS = new ArrayList<>();

    public MobHordeEvent(Supplier<EntityLiving> entitySupplier, int quantityMin, int quantityMax) {
        this.entitySupplier = entitySupplier;
        this.quantityMin = quantityMin;
        this.quantityMax = quantityMax;
        this.EVENTS.add(this);
    }

    public MobHordeEvent setNightOnly(boolean nightOnly) {
        this.nightOnly = nightOnly;
        return this;
    }

    public MobHordeEvent setAdvancementUnlock(ResourceLocation advancementUnlock) {
        this.advancementUnlock = advancementUnlock;
        return this;
    }

    public void run(EntityPlayer player) {
        int quantity = quantityMin + (int) (Math.random() * quantityMax);
        for (int i = 0; i < quantity; i++) {
            spawnMobWithPod(player);
        }
    }

    public boolean canRun(EntityPlayerMP player) {
        if (advancementUnlock != null) {
            Advancement advancement = resourceLocationToAdvancement(advancementUnlock, player.world);
            if (!player.getAdvancements().getProgress(advancement).isDone())
                return false;
        }
        return !(player.world.isDaytime() && nightOnly);
    }

    private static Advancement resourceLocationToAdvancement(ResourceLocation location, World world) {
        AdvancementManager advManager = ObfuscationReflectionHelper.getPrivateValue(World.class, world, "field_191951_C");
        return advManager.getAdvancement(location);
    }

    public void spawnMobWithPod(EntityPlayer player) {
        EntityDropPod pod = new EntityDropPod(player.world);
        pod.rotationYaw = (float) Math.random() * 360;
        EntityLiving mob = entitySupplier.get();

        double x = player.posX + Math.random() * 60;
        double y = 350 + Math.random() * 100;
        double z = player.posZ + Math.random() * 60;

        GTTeleporter teleporter = new GTTeleporter((WorldServer) player.world, x, y, z);
        TeleportHandler.teleport(mob, player.dimension, teleporter, x, y, z);

        mob.startRiding(pod, true);
        mob.enablePersistence();

        pod.setPosition(x, y, z);
        player.world.spawnEntity(mob);
        player.world.spawnEntity(pod);
    }

}
