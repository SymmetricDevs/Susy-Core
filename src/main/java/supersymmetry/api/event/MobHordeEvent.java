package supersymmetry.api.event;

import gregtech.api.util.GTTeleporter;
import gregtech.api.util.TeleportHandler;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import supersymmetry.common.entities.EntityDropPod;
import supersymmetry.common.event.MobHordePlayerData;
import supersymmetry.common.event.MobHordeWorldData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class MobHordeEvent {
    private Function<EntityPlayer, EntityLiving> entitySupplier;
    private int quantityMin;
    private int quantityMax;
    private boolean nightOnly;
    private ResourceLocation advancementUnlock;
    private int timerMin;
    private int timerMax;
    public int timeoutPeriod;
    private int dimension = 0;
    private int maximumDistanceUnderground = -1;
    private boolean canUsePods = true;
    public String KEY;

    public static final Map<String, MobHordeEvent> EVENTS = new HashMap<>();

    public MobHordeEvent(Function<EntityPlayer, EntityLiving> entitySupplier, int quantityMin, int quantityMax, String name) {
        this(entitySupplier, quantityMin, quantityMax, name, 18000);
    }

    public MobHordeEvent(Function<EntityPlayer, EntityLiving> entitySupplier, int quantityMin, int quantityMax, String name, int timeoutPeriod) {
        this.entitySupplier = entitySupplier;
        this.quantityMin = quantityMin;
        this.quantityMax = quantityMax;
        this.KEY = name;
        this.timeoutPeriod = timeoutPeriod;
        this.EVENTS.put(name, this);
    }

    public MobHordeEvent setNightOnly(boolean nightOnly) {
        this.nightOnly = nightOnly;
        return this;
    }

    public MobHordeEvent setTimeout(int timeout) {
        this.timeoutPeriod = timeout;
        return this;
    }

    public MobHordeEvent setAdvancementUnlock(ResourceLocation advancementUnlock) {
        this.advancementUnlock = advancementUnlock;
        return this;
    }

    public boolean run(EntityPlayer player) {
        MobHordeWorldData worldData = MobHordeWorldData.get(player.world);
        MobHordePlayerData playerData = worldData.getPlayerData(player.getPersistentID());
        return run(player, playerData::addEntity);
    }
    public boolean run(EntityPlayer player, Consumer<UUID> uuidConsumer) {
        int quantity = quantityMin + (int) (Math.random() * quantityMax);
        boolean didSpawn = false;
        if (hasToBeUnderground(player) || !canUsePods) {
            for (int i = 0; i < quantity; i++) {
                didSpawn |= spawnMobWithoutPod(player, uuidConsumer);
            }
        } else {
            for (int i = 0; i < quantity; i++) {
                didSpawn |= spawnMobWithPod(player, uuidConsumer);
            }
        }
        return didSpawn;
    }

    public boolean canRun(EntityPlayerMP player) {
        if (advancementUnlock != null) {
            Advancement advancement = resourceLocationToAdvancement(advancementUnlock, player.world);
            if (!player.getAdvancements().getProgress(advancement).isDone())
                return false;
        }
        if (player.dimension != this.dimension) {
            return false;
        }
        return !(player.world.isDaytime() && nightOnly) || hasToBeUnderground(player);
    }

    private static Advancement resourceLocationToAdvancement(ResourceLocation location, World world) {
        AdvancementManager advManager = ObfuscationReflectionHelper.getPrivateValue(World.class, world, "field_191951_C");
        return advManager.getAdvancement(location);
    }

    public boolean spawnMobWithPod(EntityPlayer player, Consumer<UUID> uuidConsumer) {
        EntityDropPod pod = new EntityDropPod(player.world);
        pod.rotationYaw = (float) Math.random() * 360;
        EntityLiving mob = entitySupplier.apply(player);

        double x = player.posX + Math.random() * 60;
        double y = 350 + Math.random() * 200;
        double z = player.posZ + Math.random() * 60;

        GTTeleporter teleporter = new GTTeleporter((WorldServer) player.world, x, y, z);
        TeleportHandler.teleport(mob, player.dimension, teleporter, x, y, z);

        pod.setPosition(x, y, z);
        player.world.spawnEntity(pod);
        player.world.spawnEntity(mob);

        mob.startRiding(pod, true);
        mob.onInitialSpawn(player.world.getDifficultyForLocation(new BlockPos(mob)), (IEntityLivingData) null);
        mob.enablePersistence();

        uuidConsumer.accept(mob.getPersistentID());

        return true;
    }

    public boolean spawnMobWithoutPod(EntityPlayer player, Consumer<UUID> uuidConsumer) {
        EntityLiving mob = entitySupplier.apply(player);

        for (int i = 0; i < 3; i++) {
            double angle = Math.random() * 2 * Math.PI;
            int x = (int) (player.posX + 15 * Math.cos(angle));
            int z = (int) (player.posZ + 15 * Math.sin(angle));

            mob.setPosition(x, player.posY - 5, z);
            while ((!mob.getCanSpawnHere() || !mob.isNotColliding()) && mob.posY < player.posY + 12) {
                mob.setPosition(x, mob.posY + 1, z);
            }
            if (mob.posY < player.posY + 12) {
                player.world.spawnEntity(mob);
                mob.enablePersistence();
                uuidConsumer.accept(mob.getPersistentID());
                return true;
            }
        }
        return false;
    }

    public int getNextDelay() {
        return timerMin + (int) (Math.random() * (double) (timerMax - timerMin));
    }

    public MobHordeEvent setTimer(int min, int max) {
        this.timerMin = min;
        this.timerMax = max;
        return this;
    }

    public MobHordeEvent setDimension(int dimension) {
        this.dimension = dimension;
        return this;
    }

    public MobHordeEvent setMaximumDistanceUnderground(int maximumDistanceUnderground) {
        this.maximumDistanceUnderground = maximumDistanceUnderground;
        return this;
    }

    public MobHordeEvent setCanUsePods(boolean canUsePods) {
        this.canUsePods = canUsePods;
        return this;
    }

    protected boolean hasToBeUnderground(EntityPlayer player) {
        return (maximumDistanceUnderground != -1 && !player.world.canBlockSeeSky(new BlockPos(player).up(maximumDistanceUnderground)));
    }
}
