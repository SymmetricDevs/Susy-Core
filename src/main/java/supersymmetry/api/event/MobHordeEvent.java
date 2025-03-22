package supersymmetry.api.event;

import gregtech.api.util.GTTeleporter;
import gregtech.api.util.TeleportHandler;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
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
    private String ScriptNBTdata = "";
    private String ScriptNBTdataOuter = "";
    private static int dropPodCount = 0;
    private String pattern = "";
    private static double centerx = 0;
    private static double centerz = 0;
    private static Set<String> occupiedCoordinates = new HashSet<>();
    private double ratio = 0.2;

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

    public MobHordeEvent beScriptBlock(String NBTdata){
        this.ScriptNBTdata = NBTdata;
        return this;
    }
    public MobHordeEvent multiScriptBlockPlacement(String NBTdata,String NBTdata2){
        this.ScriptNBTdata = NBTdata;
        this.ScriptNBTdataOuter = NBTdata2;
        return this;
    }

    public MobHordeEvent organisedSpawn(String pattern){
        this.pattern = pattern;
        return this;
    }

    public MobHordeEvent setRatio(double ratio){
        this.ratio = ratio;
        return this;
    }


    public boolean run(EntityPlayer player) throws NBTException {
        MobHordeWorldData worldData = MobHordeWorldData.get(player.world);
        MobHordePlayerData playerData = worldData.getPlayerData(player.getPersistentID());
        dropPodCount = 0;
        occupiedCoordinates.clear();
        return run(player, playerData::addEntity);
    }
    public boolean run(EntityPlayer player, Consumer<UUID> uuidConsumer) throws NBTException {
        dropPodCount = 0;
        occupiedCoordinates.clear();
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
        AdvancementManager advManager = ObfuscationReflectionHelper.getPrivateValue(World.class, world, "field_191951_C");
        return advManager.getAdvancement(location);
    }

    public boolean spawnMobWithPod(EntityPlayer player, Consumer<UUID> uuidConsumer) throws NBTException {
        EntityDropPod pod = new EntityDropPod(player.world);
        pod.rotationYaw = (float) Math.random() * 360;

        if (this.ScriptNBTdata != ""){

            //there is probably a way to do this without copy-pasting stuff
            IBlockState blockState = Blocks.REDSTONE_BLOCK.getDefaultState();
            EntityFallingBlock mob = new EntityFallingBlock(player.world, 0,0,0, blockState);
            EntityFallingBlock block = new EntityFallingBlock(player.world, 0, 0, 0, blockState);
            EntityFallingBlock block2 = new EntityFallingBlock(player.world, 0, 0, 0, blockState);


            pod.CanExplode(false); //disable explosion so if things land close together they don't end up pushing the falling blocks
            block.fallTime = 1;
            block.shouldDropItem = false;
            block2.fallTime = 1;
            block2.shouldDropItem = false;
            NBTTagCompound NBTtags;


            int R = 60;
            double x = (int)(Math.floor(player.posX + Math.random() * R)); //default
            double z = (int)(Math.floor(player.posZ + Math.random() * R)); //default


            if (this.ScriptNBTdataOuter != "") {
                if (dropPodCount < this.quantityMax * ratio){
                    NBTtags = (NBTTagCompound) JsonToNBT.getTagFromJson(this.ScriptNBTdata);
                }
                else{
                    NBTtags = (NBTTagCompound) JsonToNBT.getTagFromJson(this.ScriptNBTdataOuter);
                }

            }
            else{
                NBTtags = (NBTTagCompound) JsonToNBT.getTagFromJson(this.ScriptNBTdata);
            }
            mob.readFromNBT(NBTtags);
            mob.shouldDropItem = false;

            if (dropPodCount == 0 && this.pattern != ""){
                centerx = (int)(Math.floor(player.posX + 30 + Math.random() * R/2));
                centerz = (int)(Math.floor(player.posZ + 30 + Math.random() * R/2));
            }
            dropPodCount++; //useless without pattern

            if (this.pattern != ""){

                switch(this.pattern){
                    case "square":
                        R = 50;
                        if (dropPodCount <= this.quantityMax * ratio) {
                            double innerHalf = R / 6;
                            boolean placed = false;

                            while (!placed) {
                                x = centerx + (int) Math.floor((Math.random() * (2 * innerHalf)) - innerHalf);
                                z = centerz + (int) Math.floor((Math.random() * (2 * innerHalf)) - innerHalf);

                                boolean tooClose = false;
                                for (String coord : occupiedCoordinates) {
                                    String[] parts = coord.split(",");
                                    int existingX = Integer.parseInt(parts[0]);
                                    int existingZ = Integer.parseInt(parts[1]);

                                    if (Math.abs(existingX - x) < 5 && Math.abs(existingZ - z) < 5) { //size of mortar is 5x5, therefore
                                        tooClose = true;
                                        break;
                                    }
                                }

                                if (!tooClose) {
                                    occupiedCoordinates.add((int) x + "," + (int) z);
                                    placed = true;
                                }
                            }
                        } else {
                            boolean placed = false;
                            while (!placed) {
                                x = (Math.random() < 0.5)
                                        ? (int) (Math.floor(Math.random() * -R / 3) - R / 6)
                                        : (int) (Math.floor(Math.random() * R / 3 + 1) + R / 6);

                                z = (Math.random() < 0.5)
                                        ? (int) (Math.floor(Math.random() * -R / 3) - R / 6)
                                        : (int) (Math.floor(Math.random() * R / 3 + 1) + R / 6);

                                x += centerx;
                                z += centerz;

                                if (!occupiedCoordinates.contains(x + "," + z)) {
                                    occupiedCoordinates.add((int) x + "," + (int) z);
                                    placed = true;
                                }
                            }
                        }

                        break;
                    default:
                        System.out.println("UNRECOGNISED PATTERN, SCRAMBLING");
                        break;
                }
            }

            x = x + 0.5;
            z = z + 0.5;
            int y = 256;
            //double y = 350 + Math.random() * 200;
            //has to be under 256 for now, otherwise this causes problems, and idk how to get rid of it :pain:
            //if someone wants to undertake this sisyphean task, figure out a way to override onUpdate() in EntityFallingBlock
            //and remove blockpos1.getY() > 256
            /*
                            if (!this.onGround && !flag1)
                {
                    if (this.fallTime > 100 && !this.world.isRemote && (blockpos1.getY() < 1 || blockpos1.getY() > 256) || this.fallTime > 600)
                    {
                        if (this.shouldDropItem && this.world.getGameRules().getBoolean("doEntityDrops"))
                        {
                            this.entityDropItem(new ItemStack(block, 1, block.damageDropped(this.fallTile)), 0.0F);
                        }

                        this.setDead();
                    }
                }
             */

            GTTeleporter teleporter = new GTTeleporter((WorldServer) player.world, x, y, z);
            TeleportHandler.teleport(block, player.dimension, teleporter, x, y, z);
            TeleportHandler.teleport(block2, player.dimension, teleporter, x, y, z);
            TeleportHandler.teleport(mob, player.dimension, teleporter, x, y, z);

            pod.setPosition(x, y, z);
            player.world.spawnEntity(pod);
            player.world.spawnEntity(mob);
            player.world.spawnEntity(block);
            player.world.spawnEntity(block2);
            block2.startRiding(block, true);
            block.startRiding(mob, true);
            mob.startRiding(pod, true);
            uuidConsumer.accept(mob.getUniqueID());
            return true;
        }

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
