package supersymmetry.common.entities;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import supersymmetry.client.audio.MovingSoundDrone;

public class EntityDrone extends EntityLiving implements IAnimatable {

    private static final DataParameter<Integer> AGE = EntityDataManager.<Integer>createKey(EntityDrone.class, DataSerializers.VARINT);

    private AnimationFactory factory = new AnimationFactory(this);

    @SideOnly(Side.CLIENT)
    private MovingSoundDrone soundDrone;

    public EntityDrone(World worldIn) {
        super(worldIn);
    }

    public EntityDrone(World worldIn, double x, double y, double z) {
        this(worldIn);
        this.setEntityBoundingBox(new AxisAlignedBB(x-1, y+0, z-1, x+1, y+1, z+1));
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(AGE, 0);
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        if (this.world.isRemote) {
            setupDroneSound();
        }
    }

    @SideOnly(Side.CLIENT)
    public void setupDroneSound() {
        this.soundDrone = new MovingSoundDrone(this);
        Minecraft.getMinecraft().getSoundHandler().playSound(this.soundDrone);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        int age = this.dataManager.get(AGE);

        if (!world.isRemote) {

            if (age >= 55) {
                this.motionY += 0.125;
            }

            this.isDead = this.posY > 300 || age > 255;

        } else if (this.soundDrone != null) {

            if (this.firstUpdate) {
                this.soundDrone.startPlaying();
            }

        }

        this.dataManager.set(AGE, age + 1);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);;
        compound.setInteger("Age", this.dataManager.get(AGE));
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.dataManager.set(AGE, compound.getInteger("Age"));
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {

        int age = this.dataManager.get(AGE);

        if (age <= 55) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.drone.takeoff", ILoopType.EDefaultLoopTypes.PLAY_ONCE));
        }

        if (age >= 55) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.drone.flying", ILoopType.EDefaultLoopTypes.LOOP));
        }

        return software.bernie.geckolib3.core.PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController<EntityDrone>(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
