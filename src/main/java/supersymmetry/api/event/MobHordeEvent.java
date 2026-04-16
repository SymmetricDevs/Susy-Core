package supersymmetry.api.event;

import gregtech.api.util.GTTeleporter;
import gregtech.api.util.TeleportHandler;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
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
    private int timerMin;
    private int timerMax;
    public int timeoutPeriod;
    private int dimension = 0;
    private int maximumDistanceUnderground = -1;
    private boolean canUsePods = true;
    public String KEY;
    private boolean alignTheBlock = false;
    private List<Double> distribution;
    private List<Integer> exactDistribution;
    private int minhate = 0;
    private String faction = "";

    public static final Map<String, MobHordeEvent> EVENTS = new HashMap<>();

    private final List<Function<Double, Vec2>> patternFunctions = new ArrayList<>();
    private final List<Function<EntityLiving, EntityLiving>> postSpawnOverrides = new ArrayList<>();
    private List<Function<EntityPlayer, EntityLiving>> entitySupplierOverrides = new ArrayList<>();
    private Function<EntityLiving, EntityLiving> postSpawnModifier = null;
    private List<String> commandsOnLanding = new ArrayList<>();
    private List<List<String>> commandsOnLandingPattern = new ArrayList<>();
    private ResourceLocation requiredAdvancement = null;
    private boolean runOnce = false;

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
        //I know there is an earlier check, but we have trigger on advancement now
        World world = player.getServerWorld();
        if (!world.getGameRules().getBoolean("doInvasions")) {
            System.out.println("Invasion stopped, gamerule prevents execution");
            return false;
        }
        if (requiredAdvancement != null) {
            return false; //check if the event is locked behind advancement, if so, do not let natural spawn
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

    public MobHordeEvent runCommandOnLanding(String... commands) {
        this.commandsOnLanding = Arrays.asList(commands);
        return this;
    }

    public static List<String> addCommand(String... commands) {
        return Arrays.asList(commands);
    }

    //the great addpattern unfucking
    public MobHordeEvent addPattern(
            Function<Double, Vec2> patternFunction,
            List<String> commands,
            Function<EntityPlayer, EntityLiving> supplierOverride,
            Function<EntityLiving, EntityLiving> postSpawnModifier
    ) {
        if (patternFunction == null) {
            throw new IllegalArgumentException("patternFunction cannot be null");
        }
        if (commands == null) {
            commands = Collections.emptyList();
        }

        this.patternFunctions.add(patternFunction);
        this.commandsOnLandingPattern.add(commands);
        this.entitySupplierOverrides.add(supplierOverride); // can be null
        this.postSpawnOverrides.add(postSpawnModifier);     // can be null

        return this;
    }


    public static class Vec2 {
        public final double x;
        public final double z;

        public Vec2(double x, double z) {
            this.x = x;
            this.z = z;
        }
    }

    public MobHordeEvent alignBlock() {
        this.alignTheBlock = true;
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

    public MobHordeEvent setPostSpawnModifier(Function<EntityLiving, EntityLiving> modifier) {
        this.postSpawnModifier = modifier;
        return this;
    }

    public MobHordeEvent triggerOnAdvancement(ResourceLocation advancement) {
        this.requiredAdvancement = advancement;
        return this;
    }

    public MobHordeEvent runOnce() {
        this.runOnce = true;
        return this;
    }

    public MobHordeEvent setExactDistribution(Integer... distribution) {
        this.exactDistribution = Arrays.asList(distribution);
        return this;
    }

    public ResourceLocation getRequiredAdvancement() {
        return requiredAdvancement;
    }

    public boolean isRunOnce() {
        return runOnce;
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

            if (exactDistribution != null && exactDistribution.size() == patternsCount) {
                // exact distribution takes priority
                int totalAssigned = 0;

                for (int i = 0; i < patternsCount; i++) {
                    int value = Math.max(0, exactDistribution.get(i));
                    quantitiesPerPattern[i] = value;
                    totalAssigned += value;
                }

                int remainder = quantity - totalAssigned;
                int index = 0;
                while (remainder > 0) {
                    quantitiesPerPattern[index % patternsCount]++;
                    index++;
                    remainder--;
                }

            } else if (distribution != null && distribution.size() == patternsCount) {
                double totalWeight = distribution.stream().mapToDouble(Double::doubleValue).sum();

                int assigned = 0;
                double[] exactValues = new double[patternsCount];

                for (int i = 0; i < patternsCount; i++) {
                    double normalized = distribution.get(i) / totalWeight;
                    double exact = normalized * quantity;
                    exactValues[i] = exact;
                    quantitiesPerPattern[i] = (int) Math.floor(exact);
                    assigned += quantitiesPerPattern[i];
                }

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


            Double offsetx = player.posX + (Math.random() - 0.5) * 200;
            Double offsetz = player.posZ + (Math.random() - 0.5) * 200;

            for (int i = 0; i < patternsCount; i++) {
                int qtyForThisPattern = quantitiesPerPattern[i];

                List<String> commands = (i < commandsOnLandingPattern.size())
                        ? commandsOnLandingPattern.get(i)
                        : Collections.emptyList();

                Function<EntityPlayer, EntityLiving> supplierOverride =
                        (i < entitySupplierOverrides.size())
                                ? entitySupplierOverrides.get(i)
                                : null;

                Function<EntityLiving, EntityLiving> patternModifier =
                        (i < postSpawnOverrides.size()) ? postSpawnOverrides.get(i) : null;

                finishSpawning |= spawnMobWithPattern(
                        player,
                        uuidConsumer,
                        qtyForThisPattern,
                        patternFunctions.get(i),
                        commands,
                        supplierOverride,
                        patternModifier,
                        offsetx,
                        offsetz
                );
            }
        }

        return finishSpawning;
    }

    private boolean spawnMobWithPattern(EntityPlayer player, Consumer<UUID> uuidConsumer,
                                        int quantity,
                                        Function<Double, Vec2> pattern,
                                        List<String> commands,
                                        Function<EntityPlayer, EntityLiving> supplierOverride,
                                        Function<EntityLiving, EntityLiving> patternModifier,
                                        Double centerX, Double centerZ) {
        boolean didSpawn = false;

        int spawned = 0;
        Set<String> occupiedCoordinates = new HashSet<>();

        while (spawned < quantity) {

            EntityDropPod pod = new EntityDropPod(player.world);
            pod.rotationYaw = (float) (Math.random() * 360);

            // Combine global and pattern-specific commands
            List<String> allCommands = new ArrayList<>();

            UUID playerUUID = player.getUniqueID();
            String uuidString = playerUUID.toString();

            for (String cmd : commands) {
                allCommands.add(cmd.replace("%player_uuid%", uuidString));
            }
            for (String cmd : this.commandsOnLanding) {
                allCommands.add(cmd.replace("%player_uuid%", uuidString));
            }

            pod.setCommandsOnLanding(allCommands);

            // t for pattern
            double t = quantity == 1 ? 0.0 : ((double) spawned + 0.5) / quantity;

            // pattern offset
            Vec2 offset = pattern.apply(t);
            double x = centerX + offset.x;
            double y = 350 + Math.random() * 200;
            double z = centerZ + offset.z;

            if (alignTheBlock) {
                x = Math.floor(x) + 0.5;
                z = Math.floor(z) + 0.5;
            }

            String key = ((int) Math.floor(x)) + "," + ((int) Math.floor(z));
            if (occupiedCoordinates.contains(key)) continue;
            occupiedCoordinates.add(key);

            pod.setPosition(x, y, z);
            player.world.spawnEntity(pod);

            Function<EntityPlayer, EntityLiving> supplier =
                    supplierOverride != null ? supplierOverride : this.entitySupplier;

            EntityLiving passenger = supplier != null ? supplier.apply(player) : null;

            if (passenger != null) {
                passenger.setPosition(x, y, z);
                passenger.startRiding(pod, true);
                passenger.onInitialSpawn(
                        player.world.getDifficultyForLocation(new BlockPos(passenger)),
                        null
                );
                if (patternModifier != null) {
                    patternModifier.apply(passenger);
                } else if (this.postSpawnModifier != null) {
                    this.postSpawnModifier.apply(passenger);
                }
                player.world.spawnEntity(passenger);
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
        pod.rotationYaw = (float) (Math.random() * 360);

        EntityLiving mob = entitySupplier.apply(player);

        double x = player.posX + (Math.random() - 0.5) * 60;
        double y = 350 + Math.random() * 200;
        double z = player.posZ + (Math.random() - 0.5) * 60;

        mob.setPosition(x, y, z);
        mob.onInitialSpawn(
                player.world.getDifficultyForLocation(new BlockPos(mob)),
                null
        );
        mob.startRiding(pod, true);
        if (this.postSpawnModifier != null) {
            this.postSpawnModifier.apply(mob);
        }

        GTTeleporter teleporter = new GTTeleporter((WorldServer) player.world, x, y, z);
        TeleportHandler.teleport(mob, player.dimension, teleporter, x, y, z);

        player.world.spawnEntity(pod);
        player.world.spawnEntity(mob);

        pod.setPosition(x, y, z);
        List<String> processedCommands = new ArrayList<>();
        String uuidString = player.getUniqueID().toString();

        for (String cmd : this.commandsOnLanding) {
            processedCommands.add(cmd.replace("%player_uuid%", uuidString));
        }

        pod.setCommandsOnLanding(processedCommands);

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

            BlockPos topPos = player.world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z));
            double y = topPos.getY() + 0.01;

            double maxY = player.posY + 8;

            if (y > maxY) continue;

            mob.setPosition(x, y, z); //test

            if (!mob.getCanSpawnHere() || !mob.isNotColliding()) continue;

            player.world.spawnEntity(mob);
            mob.enablePersistence();
            uuidConsumer.accept(mob.getPersistentID());

            return true;
        }
        System.out.println("failure");
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

    public static void baseline(ResourceLocation advancement, int hate) {
        supersymmetry.common.faction.FactionBaselineRegistry.add(advancement, hate);
    }
}
