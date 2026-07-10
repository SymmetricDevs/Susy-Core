package supersymmetry.common.entities;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.factory.EntityGuiData;
import com.cleanroommc.modularui.factory.GuiFactories;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.util.TeleportHandler;
import gregtech.modules.ModuleManager;
import io.netty.buffer.ByteBuf;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import supersymmetry.Supersymmetry;
import supersymmetry.api.items.CargoItemStackHandler;
import supersymmetry.api.rocketry.fuels.RocketFuelEntry;
import supersymmetry.api.util.SuSyDamageSources;
import supersymmetry.api.util.SuSyUtility;
import supersymmetry.client.audio.MovingSoundDropPod;
import supersymmetry.client.renderer.particles.SusyParticleFlame;
import supersymmetry.client.renderer.particles.SusyParticleSmoke;
import supersymmetry.common.EventHandlers;
import supersymmetry.common.entities.teleporters.DropPodTeleporter;
import supersymmetry.common.event.DimensionRidingSwapData;
import supersymmetry.common.event.GravityHandler;
import supersymmetry.common.network.CPacketRocketInteract;
import supersymmetry.common.rocketry.RocketConfiguration;
import supersymmetry.integration.baubles.BaublesModule;
import supersymmetry.modules.SuSyModules;

public class EntityLander extends EntityAbstractRocket
                          implements IAnimatable, IInventory, IGuiHolder<EntityGuiData>, IEntityAdditionalSpawnData {

    private static final DataParameter<Boolean> HAS_LANDED = EntityDataManager.<Boolean>createKey(EntityLander.class,
            DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> TIME_SINCE_LANDING = EntityDataManager
            .<Integer>createKey(EntityLander.class, DataSerializers.VARINT);

    private AnimationFactory factory = new AnimationFactory(this);
    public static final double MAX_LAUNCH_MASS = 10000;

    @SideOnly(Side.CLIENT)
    private MovingSoundDropPod soundDropPod;

    public EntityLander(World worldIn) {
        super(worldIn);
        setSize(3, 5);
    }

    public EntityLander(World worldIn, double x, double y, double z) {
        this(worldIn);
        this.setLocationAndAngles(x, y, z, 0.F, 0.F);
    }

    public EntityLander(World worldIn, BlockPos pos) {
        this(worldIn, (float) pos.getX() - 0.5F, (float) pos.getY(), (float) pos.getZ() + 0.5);
    }

    public boolean canPlayerDismount() {
        return this.isDead || this.getTimeSinceLanding() >= 30;
    }

    public boolean hasLanded() {
        return this.dataManager.get(HAS_LANDED);
    }

    public void setLanded(boolean landed) {
        this.dataManager.set(HAS_LANDED, landed);
    }

    public int getTimeSinceLanding() {
        return this.dataManager.get(TIME_SINCE_LANDING);
    }

    public void setTimeSinceLanding(int timeSinceLanding) {
        this.dataManager.set(TIME_SINCE_LANDING, timeSinceLanding);
    }

    @SideOnly(Side.CLIENT)
    protected void spawnFlightParticles(boolean goingUp) {
        if (this.isDead) {
            return;
        }

        double offset = goingUp ? 0.2D : 0.5D;
        SusyParticleFlame flame1 = new SusyParticleFlame(
                this.world,
                this.posX + 0.8D,
                this.posY + 0.9D + offset,
                this.posZ + 0.2D,
                1.5 * (GTValues.RNG.nextFloat() + 0.2) * 0.08,
                -1.5,
                1.5 * (GTValues.RNG.nextFloat() - 0.5) * 0.08);
        SusyParticleFlame flame2 = new SusyParticleFlame(
                this.world,
                this.posX + 0.8D,
                this.posY + 0.9D + offset,
                this.posZ - 0.2D,
                1.5 * (GTValues.RNG.nextFloat() + 0.2) * 0.08,
                -1.5,
                1.5 * (GTValues.RNG.nextFloat() - 0.5) * 0.08);
        SusyParticleFlame flame3 = new SusyParticleFlame(
                this.world,
                this.posX - 0.8D,
                this.posY + 0.9D + offset,
                this.posZ + 0.2D,
                1.5 * (GTValues.RNG.nextFloat() - 1.2) * 0.08,
                -1.5,
                1.5 * (GTValues.RNG.nextFloat() - 0.5) * 0.08);
        SusyParticleFlame flame4 = new SusyParticleFlame(
                this.world,
                this.posX - 0.8D,
                this.posY + 0.9D + offset,
                this.posZ - 0.2D,
                1.5 * (GTValues.RNG.nextFloat() - 1.2) * 0.08,
                -1.5,
                1.5 * (GTValues.RNG.nextFloat() - 0.5) * 0.08);

        SusyParticleSmoke smoke1 = new SusyParticleSmoke(
                this.world,
                this.posX + 0.8D,
                this.posY + 0.9D + offset,
                this.posZ + 0.2D,
                1.5 * (GTValues.RNG.nextFloat() + 0.2) * 0.16,
                -1.5,
                1.5 * (GTValues.RNG.nextFloat() - 0.5) * 0.16);
        SusyParticleSmoke smoke2 = new SusyParticleSmoke(
                this.world,
                this.posX + 0.8D,
                this.posY + 0.9D + offset,
                this.posZ - 0.2D,
                1.5 * (GTValues.RNG.nextFloat() + 0.2) * 0.16,
                -1.5,
                1.5 * (GTValues.RNG.nextFloat() - 0.5) * 0.16);
        SusyParticleSmoke smoke3 = new SusyParticleSmoke(
                this.world,
                this.posX - 0.8D,
                this.posY + 0.9D + offset,
                this.posZ + 0.2D,
                1.5 * (GTValues.RNG.nextFloat() - 1.2) * 0.16,
                -1.5,
                1.5 * (GTValues.RNG.nextFloat() - 0.5) * 0.16);
        SusyParticleSmoke smoke4 = new SusyParticleSmoke(
                this.world,
                this.posX - 0.8D,
                this.posY + 0.9D + offset,
                this.posZ - 0.2D,
                1.5 * (GTValues.RNG.nextFloat() - 1.2) * 0.16,
                -1.5,
                1.5 * (GTValues.RNG.nextFloat() - 0.5) * 0.16);

        Minecraft.getMinecraft().effectRenderer.addEffect(smoke1);
        Minecraft.getMinecraft().effectRenderer.addEffect(smoke2);
        Minecraft.getMinecraft().effectRenderer.addEffect(smoke3);
        Minecraft.getMinecraft().effectRenderer.addEffect(smoke4);

        Minecraft.getMinecraft().effectRenderer.addEffect(flame1);
        Minecraft.getMinecraft().effectRenderer.addEffect(flame2);
        Minecraft.getMinecraft().effectRenderer.addEffect(flame3);
        Minecraft.getMinecraft().effectRenderer.addEffect(flame4);
    }

    private void handleCollidedBlocks(boolean above) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                BlockPos pos = new BlockPos(this.posX + i, above ? this.posY + 2 : this.posY - 1, this.posZ + j);
                if (this.world.getBlockState(pos).getMaterial().isLiquid()) return;
                if (this.world.getBlockState(pos).getBlockHardness(this.world, pos) < 0.3) {
                    this.world.setBlockToAir(pos);
                } else if (above) {
                    this.explode();
                    this.world.removeEntityDangerously(this);
                    break;
                }
            }
        }
    }

    private void handleCollidedBlocks() {
        handleCollidedBlocks(false);
    }

    protected float getExplosionStrength() {
        if (getRidingEntity() != null && getRidingEntity() instanceof EntityPlayer) {
            return 6;
        }
        return 1;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(HAS_LANDED, false);
        this.dataManager.register(TIME_SINCE_LANDING, 0);
    }

    @Override
    public void writeEntityToNBT(@NotNull NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("landed", this.hasLanded());
        compound.setInteger("time_since_landing", this.getTimeSinceLanding());
    }

    @Override
    public void readEntityFromNBT(@NotNull NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setLanded(compound.getBoolean("landed"));
        this.setTimeSinceLanding(compound.getInteger("time_since_landing"));
    }

    @Override
    protected void act() {
        if (getPassengers().isEmpty()) {
            return;
        }

        NBTTagCompound tag = this.getEntityData().getCompoundTag(EntityAbstractRocket.ROCKET_CONFIG_KEY);
        RocketConfiguration config = new RocketConfiguration(tag);
        // Land on next planet
        RocketConfiguration.MissionConfiguration next = config.popFront();
        while (!config.isEmpty() && next.missionType != RocketConfiguration.MissionType.Manned) {
            next = config.popFront();
        }
        Entity passenger = this.getPassengers().get(0);

        if (next.missionType != RocketConfiguration.MissionType.Manned) {
            if (passenger instanceof EntityLivingBase living) {
                living.attackEntityFrom(SuSyDamageSources.REENTRY, 100000000);
            }
            passenger.setDead();
            return;
        }

        EntityLander dropPod = new EntityLander(this.world, next.landingPos.getX(), 350, next.landingPos.getZ());
        dropPod.setInventory(cargo);

        // Pop the next mission from the rocket configuration
        dropPod.getEntityData().setTag(EntityAbstractRocket.ROCKET_CONFIG_KEY, config.serialize());

        TeleportHandler.teleport(dropPod, next.dimension, new DropPodTeleporter(), next.landingPos.getX(), 350,
                next.landingPos.getZ());

        EventHandlers.travellingPassengers.add(new DimensionRidingSwapData(dropPod, passenger));
    }

    @Override
    public void startCountdown(int length) {
        NBTTagCompound tag = this.getEntityData().getCompoundTag(EntityAbstractRocket.ROCKET_CONFIG_KEY);
        RocketConfiguration config = new RocketConfiguration(tag);
        if (config.isEmpty()) {
            sendMessageToPassengers(new TextComponentTranslation("susy.rocket.msg.not_configured"));
            if (cargo.isEmpty()) {
                this.setDead();
            }
            return;
        }
        double gravMult = GravityHandler.getGravityMultiplier(this.world);
        if (gravMult > 0.4) {
            sendMessageToPassengers(new TextComponentTranslation("susy.rocket.msg.gravity_too_high"));
            if (cargo.isEmpty()) {
                this.setDead();
            }
            return;
        }
        if (getCargoMass() > MAX_LAUNCH_MASS) {
            sendMessageToPassengers(new TextComponentTranslation("susy.rocket.msg.too_heavy"));
            return;
        }

        super.startCountdown(length);
    }

    public void sendMessageToPassengers(TextComponentTranslation translation) {
        for (Entity passenger : this.getPassengers()) {
            if (passenger instanceof EntityPlayer player) {
                player.sendStatusMessage(translation, true);
            }
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (!world.isRemote) {
            if (!this.onGround && this.motionY < 0.0D && this.posY < 256) {
                // Lerp between motionY and 0.5 at 10%
                this.motionY = MathHelper.clampedLerp(this.motionY, -0.5F, 0.1D);
            }

            if (!this.hasLanded()) {
                this.handleCollidedBlocks();
                this.getPassengers().forEach(e -> e.fallDistance = 0);
            }

            this.setLanded(this.hasLanded() || this.onGround);

            if (this.hasLanded()) {
                if (this.getTimeSinceLanding() == 0) {
                    int posXRounded = MathHelper.floor(this.posX);
                    int posYBeneath = MathHelper.floor(this.posY - 1.20000000298023224D);
                    int posZRounded = MathHelper.floor(this.posZ);
                    IBlockState blockBeneath = this.world
                            .getBlockState(new BlockPos(posXRounded, posYBeneath, posZRounded));

                    if (blockBeneath.getMaterial() != Material.AIR) {
                        SoundType soundType = blockBeneath.getBlock().getSoundType(blockBeneath, world,
                                new BlockPos(posXRounded, posYBeneath, posZRounded), this);
                        this.playSound(soundType.getBreakSound(), soundType.getVolume() * 3.0F,
                                soundType.getPitch() * 0.2F);
                    }
                }
                this.setTimeSinceLanding(this.getTimeSinceLanding() + 1);
            }

            if (this.isLaunched()) {
                if (this.motionY < 10.D) {
                    if (this.motionY < 1.D) {
                        this.motionY += 0.1;
                    }
                    this.motionY *= 1.1D;
                }
                this.handleCollidedBlocks(true);
            }
            if (this.posY > 1000 && isLaunched()) {
                if (this.hasActed() && this.getPassengers().isEmpty()) {
                    this.setDead();
                } else {
                    act();
                    this.setActed(true);
                }
            }
        } else {
            if (!this.hasLanded()) {
                this.spawnFlightParticles(false);
            }
            if (this.isLaunched()) {
                this.spawnFlightParticles(true);
            }
            if (soundDropPod != null) {
                if (!this.hasLanded() || this.isLaunched()) {
                    soundDropPod.startPlaying();
                } else {
                    soundDropPod.stopPlaying();
                }
            }
        }
    }

    @Override
    public RocketFuelEntry getFuel() {
        return null;
    }

    @Override
    protected void removePassenger(@NotNull Entity passenger) {
        if (this.canPlayerDismount()) {
            super.removePassenger(passenger);
            if (passenger instanceof EntityLiving living) {
                living.setNoAI(false);
            }
        }
    }

    @Override
    public void updatePassenger(@NotNull Entity passenger) {
        super.updatePassenger(passenger);
        float xOffset = MathHelper.sin(this.rotationYaw * 0.1F);
        float zOffset = MathHelper.cos(this.rotationYaw * 0.1F);
        passenger.setPosition(this.posX + (double) (0.1F * xOffset),
                this.posY + (double) (this.height * 0.2F) + passenger.getYOffset() + 0.0D,
                this.posZ - (double) (0.1F * zOffset));

        if (passenger instanceof EntityLivingBase) {
            ((EntityLivingBase) passenger).renderYawOffset = this.rotationYaw;
        }
    }

    @Override
    public boolean shouldDismountInWater(@NotNull Entity rider) {
        return false;
    }

    @Override
    public boolean handleWaterMovement() {
        return isInWater();
    }

    @Override
    public void fall(float distance, float damageMultiplier) {}

    @Override
    public void setAir(int air) {
        super.setAir(300);
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        if (this.hasLanded()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.ladder.extend",
                    ILoopType.EDefaultLoopTypes.PLAY_ONCE));
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData
                .addAnimationController(new AnimationController<EntityLander>(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }

    @Override
    protected void addPassenger(Entity passenger) {
        if (this.getPassengers().isEmpty()) {
            super.addPassenger(passenger);
            if (passenger instanceof EntityLiving living) {
                living.setNoAI(true);
            }
        }
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        if (this.world.isRemote) {
            setupDropPodSound();
        }
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        NBTTagCompound cargoTag = new NBTTagCompound();
        if (this.cargo != null) {
            cargoTag = this.cargo.serializeNBT();
        }
        ByteBufUtils.writeTag(buffer, cargoTag);
    }

    @Override
    public void readSpawnData(ByteBuf buffer) {
        try {
            NBTTagCompound cargoTag = ByteBufUtils.readTag(buffer);
            if (this.cargo == null) {
                this.cargo = new CargoItemStackHandler(0, 0);
            }
            this.cargo.deserializeNBT(cargoTag);
        } catch (Exception e) {
            // cargo will be synced through UI or other means if spawn data fails
        }
    }

    @SideOnly(Side.CLIENT)
    public void setupDropPodSound() {
        this.soundDropPod = new MovingSoundDropPod(this);
        Minecraft.getMinecraft().getSoundHandler().playSound(this.soundDropPod);
    }

    @Override
    public int getSizeInventory() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return this.cargo.isEmpty();
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return this.cargo.getExposedStack();
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        this.markDirty();
        return cargo.extractItem(0, count, false);
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        this.markDirty();
        return cargo.extractItem(0, cargo.getExposedStack().getCount(), false);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        this.markDirty();
        cargo.insertItem(0, stack, false);
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {
        // Entity inventories typically don't need special dirty handling
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        if (this.isDead) {
            return false;
        }
        return player.getDistanceSq(this) <= 64.0D;
    }

    @Override
    public void openInventory(EntityPlayer player) {
        // No special handling needed
    }

    @Override
    public void closeInventory(EntityPlayer player) {
        // No special handling needed
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {
        // No fields to set
    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        this.cargo.clear();
    }

    public CargoItemStackHandler getInventory() {
        return this.cargo;
    }

    public void setInventory(CargoItemStackHandler cargoItemStackHandler) {
        this.cargo = cargoItemStackHandler;
    }

    @Override
    public ModularPanel buildUI(EntityGuiData data, PanelSyncManager syncManager, UISettings settings) {
        SlotGroup cargoInventory = new SlotGroup("cargo", 1, 1000, true);
        syncManager.registerSlotGroup(cargoInventory);

        ItemStackHandler insertScratch = new ItemStackHandler(1);

        ModularSlot insertSlot = new ModularSlot(insertScratch, 0)
                .filter(SuSyUtility::isAllowedItemForSpace)          // reuse cargo's own gate
                .changeListener((newItem, onlyAmount, client, init) -> {
                    if (init || newItem.isEmpty()) return;
                    ItemStack remainder = cargo.insertItem(0, newItem, false);
                    insertScratch.setStackInSlot(0, remainder);       // leftover stays visible
                });

        IItemHandler extractView = new IItemHandler() {

            public int getSlots() {
                return 1;
            }

            public ItemStack getStackInSlot(int s) {
                return cargo.getExposedStack();
            }

            public ItemStack insertItem(int s, ItemStack st, boolean sim) {
                return st;
            }

            public ItemStack extractItem(int s, int amt, boolean sim) {
                return cargo.extractItem(0, amt, sim);
            }

            public int getSlotLimit(int s) {
                return 64;
            }
        };

        ModularSlot extractSlot = new ModularSlot(extractView, 0) {

            @Override
            public void putStack(@NotNull ItemStack stack) {
                cargo.takeFromExposedStack(stack);
            }
        }.accessibility(false, true);

        syncManager.addCloseListener(player -> {
            ItemStack leftover = insertScratch.getStackInSlot(0);
            if (!leftover.isEmpty()) {
                player.inventory.placeItemBackInInventory(player.world, leftover); // returns fit, drops overflow
                insertScratch.setStackInSlot(0, ItemStack.EMPTY);
            }
        });

        return ModularPanel.defaultPanel("lander")
                .child(new Flow(GuiAxis.X)
                        .top(18)
                        .margin(7, 0)
                        .widthRel(1f)
                        .coverChildrenHeight()
                        .child(new ItemSlot().slot(insertSlot.singletonSlotGroup()))
                        .child(new ItemSlot().slot(extractSlot.slotGroup(cargoInventory)))
                        .child(new Flow(GuiAxis.Y).childPadding(10).coverChildrenHeight()
                                .child(IKey.lang("susy.lander.mass", () -> new Object[] { getCargoMass() }).asWidget()
                                        .rightRel(0.3f).height(18))
                                .child(IKey.lang("susy.lander.volume", () -> new Object[] { getCargoVolumeString() })
                                        .asWidget()
                                        .rightRel(0.3f).height(18))))
                .bindPlayerInventory();
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        if (!player.world.isRemote && player.isSneaking()) {
            GuiFactories.entity().open(player, this);
            return true;
        }
        player.startRiding(this);
        return false;
    }

    @Override // The override is about leashing the rocket, which makes it alright to completely ignore
    public EnumActionResult applyPlayerInteraction(EntityPlayer player, Vec3d hitVec, EnumHand hand) {
        if (player.isRidingSameEntity(this) || hitVec.length() > 7) return EnumActionResult.PASS;
        if (!this.world.isRemote) {
            processInitialInteract(player, hand);
        } else {
            GregTechAPI.networkHandler.sendToServer(new CPacketRocketInteract(this, hand, hitVec));
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public double getCargoMass() {
        double mass = 0;
        for (Entity passenger : getPassengers()) {
            if (passenger instanceof EntityPlayer player) {
                mass += 70;
                for (ItemStack stack : player.inventory.mainInventory) {
                    mass += (double) CargoItemStackHandler.getMassPerItem(stack) / 1000;
                }
                for (ItemStack stack : player.inventory.armorInventory) {
                    mass += (double) CargoItemStackHandler.getMassPerItem(stack) / 1000;
                }
                for (ItemStack stack : player.inventory.offHandInventory) {
                    mass += (double) CargoItemStackHandler.getMassPerItem(stack) / 1000;
                }
                if (ModuleManager.getInstance().isModuleEnabled(Supersymmetry.MODID, SuSyModules.MODULE_BAUBLES)) {
                    mass += (double) BaublesModule.getBaubleMass(player) / 1000;
                }
            }
        }
        return mass + (double) cargo.mass() / 1000;
    }

    public String getCargoVolumeString() {
        return cargo.getCurrentVolume() + "/" + cargo.getMaxVolume();
    }
}
