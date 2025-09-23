package supersymmetry.api.event;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import gregtech.api.util.GTTeleporter;
import gregtech.api.util.TeleportHandler;
import supersymmetry.common.entities.EntityDropPod;
import supersymmetry.common.event.MobHordePlayerData;
import supersymmetry.common.event.MobHordeWorldData;

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

    public MobHordeEvent(Function<EntityPlayer, EntityLiving> entitySupplier, int quantityMin, int quantityMax,
                         String name) {
        this(entitySupplier, quantityMin, quantityMax, name, 18000);
    }

    public MobHordeEvent(Function<EntityPlayer, EntityLiving> entitySupplier, int quantityMin, int quantityMax,
                         String name, int timeoutPeriod) {
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
        int quantity = (int) (Math.random() * (quantityMax - quantityMin) + quantityMin);
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
        AdvancementManager advManager = ObfuscationReflectionHelper.getPrivateValue(World.class, world,
                "field_191951_C");
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

        for (int i = 0; i < 4; i++) {
            double angle = Math.random() * 2 * Math.PI;
            int radius = 16 + (int) (20 * Math.random());
            double x = (int) (player.posX + radius * Math.cos(angle)) + 0.5;
            double z = (int) (player.posZ + radius * Math.sin(angle)) + 0.5;
            double y = Math.floor(player.posY) + 7;
            BlockPos pos = new BlockPos(x, y, z);
            IBlockState blockstate = player.world.getBlockState(pos);
            Block block = blockstate.getBlock();

            mob.setPosition(x, y, z);
            while ((!mob.getCanSpawnHere() || !mob.isNotColliding() || block.isAir(blockstate, player.world, pos)) &&
                    (Math.abs(mob.posY - player.posY) < 8)) {
                mob.setPosition(x, mob.posY - 1, z);
                pos = new BlockPos(x, mob.posY - 1, z);
                blockstate = player.world.getBlockState(pos);
                block = blockstate.getBlock();
            }

            if ((Math.abs(mob.posY - player.posY) < 8) && !block.isAir(blockstate, player.world, pos)) {
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
        return (maximumDistanceUnderground != -1 &&
                !player.world.canBlockSeeSky(new BlockPos(player).up(maximumDistanceUnderground)));
    }
}
