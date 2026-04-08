package supersymmetry.api.event;

import gregtech.api.util.GTTeleporter;
import gregtech.api.util.TeleportHandler;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import supersymmetry.common.entities.EntityDropPod;
import supersymmetry.common.event.MobHordePlayerData;
import supersymmetry.common.event.MobHordeWorldData;
import supersymmetry.common.faction.FactionHateManager;

import java.util.*;
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
    private final List<Function<Double, Vec2>> patternFunctions = new ArrayList<>();
    private boolean allignTheBlock = false;
    private List<String> commandsOnLanding = new ArrayList<>();
    private List<List<String>> commandsOnLandingPattern = new ArrayList<>();
    private List<Double> distribution;
    private int minhate = 0;
    private String faction = "";

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


    public boolean run(EntityPlayer player) throws NBTException {
        MobHordeWorldData worldData = MobHordeWorldData.get(player.world);
        MobHordePlayerData playerData = worldData.getPlayerData(player.getPersistentID());
        return run(player, playerData::addEntity);
    }
    public boolean run(EntityPlayer player, Consumer<UUID> uuidConsumer) throws NBTException {
        int quantity = (int) (Math.random() * (quantityMax - quantityMin) + quantityMin);
        if (quantity <= 0) quantity = 1;

        boolean didSpawn = false;

        if (hasToBeUnderground(player) || !canUsePods) {
            for (int i = 0; i < quantity; i++) {
                didSpawn |= spawnMobWithoutPod(player, uuidConsumer);
            }
        } else {
            didSpawn |= spawnMobWithPod(player, uuidConsumer, quantity);
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
            int hate = FactionHateManager.getHate(player, faction);
            if (hate < minhate) {
                return false;
            }
        return !(player.world.isDaytime() && nightOnly) || hasToBeUnderground(player);
    }

    private static Advancement resourceLocationToAdvancement(ResourceLocation location, World world) {
        AdvancementManager advManager = ObfuscationReflectionHelper.getPrivateValue(World.class, world, "field_191951_C");
        return advManager.getAdvancement(location);
    }

    public MobHordeEvent runCommandOnLanding(String... commands) {
        this.commandsOnLanding = Arrays.asList(commands);
        return this;
    }

    public static List<String> addCommand(String... commands) {
        return Arrays.asList(commands);
    }

    //pattern handlers
    // Pattern without commands (existing behavior)
    public MobHordeEvent addPattern(Function<Double, Vec2> patternFunction) {
        this.patternFunctions.add(patternFunction);
        this.commandsOnLandingPattern.add(Collections.emptyList()); // keep list sizes aligned
        return this;
    }

    // Pattern with a List<String> of commands
    public MobHordeEvent addPattern(Function<Double, Vec2> patternFunction, List<String> commands) {
        this.patternFunctions.add(patternFunction);
        this.commandsOnLandingPattern.add(commands);
        return this;
    }

    // Pattern with a single command shorthand
    public MobHordeEvent addPattern(Function<Double, Vec2> patternFunction, String command) {
        return addPattern(patternFunction, addCommand(command));
    }


    public static class Vec2 {
        public final double x;
        public final double z;

        public Vec2(double x, double z) {
            this.x = x;
            this.z = z;
        }
    }

    public MobHordeEvent allignBlock() {
        this.allignTheBlock = true;
        return this;
    }

    public MobHordeEvent setDistribution(Double... distribution) {
        this.distribution = Arrays.asList(distribution);
        return this;
    }

    public MobHordeEvent minHate(String faction, int minhate) {
        this.faction = faction;
        this.minhate = minhate;
        return this;
    }

    public boolean spawnMobWithPod(EntityPlayer player, Consumer<UUID> uuidConsumer, int quantity) {

        boolean finishSpawning = false;

        int patternsCount = patternFunctions.size();

        if (patternsCount == 0) {
            // NO PATTERN normal spawn like before
            for (int i = 0; i < quantity; i++) {
                finishSpawning |= spawnMobWithoutPattern(player, uuidConsumer);
            }
        } else {
            // WITH PATTERNS new code

            int[] quantitiesPerPattern = new int[patternsCount];
            if (distribution != null && distribution.size() == patternsCount) {
                double totalWeight = distribution.stream().mapToDouble(Double::doubleValue).sum();
                // First pass: assign floor values
                int assigned = 0;
                double[] exactValues = new double[patternsCount];

                for (int i = 0; i < patternsCount; i++) {
                    double normalized = distribution.get(i) / totalWeight;
                    double exact = normalized * quantity;
                    exactValues[i] = exact;
                    quantitiesPerPattern[i] = (int) Math.floor(exact);
                    assigned += quantitiesPerPattern[i];
                }

                // Handle leftovers (due to flooring)
                int remainder = quantity - assigned;
                while (remainder > 0) {
                    int bestIndex = 0;
                    double bestFraction = 0;
                    for (int i = 0; i < patternsCount; i++) {
                        double fraction = exactValues[i] - quantitiesPerPattern[i];
                        if (fraction > bestFraction) {
                            bestFraction = fraction;
                            bestIndex = i;
                        }
                    }
                    quantitiesPerPattern[bestIndex]++;
                    remainder--;
                }
            } else {
                // fallback to equal distribution
                int baseQtyPerPattern = quantity / patternsCount;
                int remainder = quantity % patternsCount;
                for (int i = 0; i < patternsCount; i++) {
                    quantitiesPerPattern[i] = baseQtyPerPattern + (i < remainder ? 1 : 0);
                }
            }


            Double offsetx = player.posX + (Math.random() - 0.5) * 60;
            Double offsetz = player.posZ + (Math.random() - 0.5) * 60;

            for (int i = 0; i < patternsCount; i++) {
                int qtyForThisPattern = quantitiesPerPattern[i];

                List<String> commands = (i < commandsOnLandingPattern.size())
                        ? commandsOnLandingPattern.get(i)
                        : Collections.emptyList();

                finishSpawning |= spawnMobWithPattern(
                        player,
                        uuidConsumer,
                        qtyForThisPattern,
                        patternFunctions.get(i),
                        commands,
                        offsetx,
                        offsetz
                );
            }
        }

        return finishSpawning;
    }

    private boolean spawnMobWithPattern(EntityPlayer player, Consumer<UUID> uuidConsumer,
                                        int quantity, Function<Double, Vec2> pattern,
                                        List<String> commands, Double centerX, Double centerZ) {
        boolean didSpawn = false;

        int spawned = 0;
        Set<String> occupiedCoordinates = new HashSet<>();

        while (spawned < quantity) {

            EntityDropPod pod = new EntityDropPod(player.world);
            pod.rotationYaw = (float) (Math.random() * 360);

            // Combine global and pattern-specific commands
            List<String> allCommands = new ArrayList<>();
            allCommands.addAll(commands);
            allCommands.addAll(this.commandsOnLanding);
            pod.setCommandsOnLanding(allCommands);

            // t for pattern
            double t = quantity == 1 ? 0.0 : ((double) spawned + 0.5) / quantity;

            // pattern
            Vec2 offset = pattern.apply(t);
            double x = centerX + offset.x;
            double y = 350 + Math.random() * 200;
            double z = centerZ + offset.z;

            if (allignTheBlock) {
                x = Math.floor(x) + 0.5;
                z = Math.floor(z) + 0.5;
            }

            String key = ((int) Math.floor(x)) + "," + ((int) Math.floor(z));
            if (occupiedCoordinates.contains(key)) continue;
            occupiedCoordinates.add(key);

            pod.setPosition(x, y, z);
            player.world.spawnEntity(pod);

            EntityLiving passenger = entitySupplier != null ? entitySupplier.apply(player) : null;
            if (passenger != null) {
                passenger.setPosition(x, y, z);
                player.world.spawnEntity(passenger);
                passenger.startRiding(pod, true);
                passenger.onInitialSpawn(player.world.getDifficultyForLocation(new BlockPos(passenger)), null);
                passenger.enablePersistence();
                uuidConsumer.accept(passenger.getPersistentID());
            } else {
                uuidConsumer.accept(pod.getUniqueID());
            }

            didSpawn = true;
            spawned++;
        }

        return didSpawn;
    }

    //moved over bru's old code
    private boolean spawnMobWithoutPattern(EntityPlayer player, Consumer<UUID> uuidConsumer) {
        EntityDropPod pod = new EntityDropPod(player.world);
        pod.rotationYaw = (float) Math.random() * 360;
        EntityLiving mob = entitySupplier.apply(player);

        double x = player.posX + (Math.random() - 0.5) * 60;
        double y = 350 + Math.random() * 200;
        double z = player.posZ + (Math.random() - 0.5) * 60;

        GTTeleporter teleporter = new GTTeleporter((WorldServer) player.world, x, y, z);
        TeleportHandler.teleport(mob, player.dimension, teleporter, x, y, z);

        pod.setPosition(x, y, z);
        player.world.spawnEntity(pod);
        player.world.spawnEntity(mob);

        pod.setCommandsOnLanding(this.commandsOnLanding);

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
            while ((!mob.getCanSpawnHere() || !mob.isNotColliding() || block.isAir(blockstate, player.world, pos))
                    && (Math.abs(mob.posY - player.posY) < 8)) {
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
        return (maximumDistanceUnderground != -1 && !player.world.canBlockSeeSky(new BlockPos(player).up(maximumDistanceUnderground)));
    }
}
