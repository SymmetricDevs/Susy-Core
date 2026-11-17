package supersymmetry.common.entities;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.*;
import net.minecraft.entity.boss.dragon.phase.PhaseList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.lwjgl.input.Keyboard;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import supersymmetry.client.renderer.handler.IAlwaysRender;

public class EntityKirov extends EntityFlying implements IAnimatable, IAlwaysRender, IEntityMultiPart {

    private double momentum = 0.95F;
    private double deltaRotation = 0.0F;

    private boolean leftInputDown;
    private boolean rightInputDown;
    private boolean forwardInputDown;
    private boolean backInputDown;
    private boolean ascendInputDown;
    private boolean descendInputDown;

    private double verticalAcceleration = 0.025D;
    private double maxVerticalSpeed = 0.3D;
    private double verticalMomentum = 0.90D;

    public MultiPartEntityPart[] parts;

    public EntityKirov(World worldIn) {
        super(worldIn);
        this.deathTime = 0;
        this.ignoreFrustumCheck = true;
        this.setNoGravity(true);
        this.setSize(8.0F, 8.0F);

        this.noClip = true;

        parts = new MultiPartEntityPart[8];

        for (int i = 0; i < 8; i++) {
            parts[i] = new MultiPartEntityPart(
                    this,
                    "part" + i,
                    12.0F,
                    12.0F
            );
        }
    }

    public EntityKirov(World worldIn, double x, double y, double z) {
        this(worldIn);
        this.setLocationAndAngles(x, y, z, 0.F, 0.F);
        this.setEntityBoundingBox(new AxisAlignedBB(x - 0.5, y, z - 0.5, x + 0.5, y + 2, z + 0.5));
    }

    public EntityKirov(World worldIn, BlockPos pos) {
        this(worldIn, (float) pos.getX() - 0.5F, (float) pos.getY(), (float) pos.getZ() + 0.5);
    }

    private AnimationFactory factory = new AnimationFactory(this);

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        if (!this.isDead) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("fly_loop",
                    ILoopType.EDefaultLoopTypes.LOOP));
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData
                .addAnimationController(new AnimationController<EntityKirov>(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    @Override
    public Entity[] getParts() {
        return parts;
    }

    private void updateParts() {
        float yaw = this.rotationYaw * 0.017453292F;

        double forwardX = -MathHelper.sin(yaw);
        double forwardZ = MathHelper.cos(yaw);

        for (int i = 0; i < 8; i++) {

            double offset = (i * 12.0D) - 40.0D;

            double x = this.posX - forwardX * offset;
            double y = this.posY - 8;
            double z = this.posZ - forwardZ * offset;

            parts[i].setLocationAndAngles(x, y, z, this.rotationYaw, 0.0F);
        }
    }

    @Override
    @NonNull
    public World getWorld() {
        return this.world;
    }

    @Override
    public boolean attackEntityFromPart(MultiPartEntityPart part, DamageSource source, float damage) {
        return false;
    }

    private void updateMotion() {
        this.momentum = 0.95F;

        this.motionX *= this.momentum;
        this.motionY *= this.verticalMomentum;
        this.motionZ *= this.momentum;

        this.deltaRotation *= this.momentum;

        this.motionY = MathHelper.clamp(this.motionY, -this.maxVerticalSpeed, this.maxVerticalSpeed);
    }

    private void controlAirship() {
        if (this.isBeingRidden()) {
            float acceleration = 0.0F;

            if (this.leftInputDown) {
                this.deltaRotation -= 0.1F;
            }

            if (this.rightInputDown) {
                this.deltaRotation += 0.1F;
            }

            if (this.ascendInputDown) {
                this.motionY += this.verticalAcceleration;
            }

            if (this.descendInputDown) {
                this.motionY -= this.verticalAcceleration;
            }

            this.rotationYaw += (float)this.deltaRotation;

            if (this.forwardInputDown) {
                acceleration += 0.04F;
            }

            if (this.backInputDown) {
                acceleration -= 0.005F;
            }

            this.motionX += MathHelper.sin(-this.rotationYaw * 0.017453292F) * acceleration;

            this.motionZ += MathHelper.cos(this.rotationYaw * 0.017453292F) * acceleration;
        }
    }

    @SideOnly(Side.CLIENT)
    public void updateInputs(
            boolean left,
            boolean right,
            boolean forward,
            boolean back,
            boolean ascend,
            boolean descend
    )
    {
        this.leftInputDown = left;
        this.rightInputDown = right;
        this.forwardInputDown = forward;
        this.backInputDown = back;
        this.ascendInputDown = ascend;
        this.descendInputDown = descend;
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        super.onUpdate();

        if (this.canPassengerSteer()) {
            this.updateMotion();
            if (this.world.isRemote) {
                this.controlAirship();
                if (this.canPassengerSteer()) {
                    Minecraft mc = Minecraft.getMinecraft();

                    boolean left = mc.gameSettings.keyBindLeft.isKeyDown();
                    boolean right = mc.gameSettings.keyBindRight.isKeyDown();

                    boolean forward = mc.gameSettings.keyBindForward.isKeyDown();
                    boolean back = mc.gameSettings.keyBindBack.isKeyDown();

                    boolean ascend = mc.gameSettings.keyBindJump.isKeyDown();
                    boolean descend = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL);

                    this.updateInputs(left, right, forward, back, ascend, descend);
                }
            }
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
        } else {
            this.motionX = 0.0D;
            this.motionY = 0.0D;
            this.motionZ = 0.0D;
        }

        this.updateParts();
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
    protected boolean canDespawn() {
        return this.isDead;
    }

    @Override
    public boolean canBeLeashedTo(@NotNull EntityPlayer player) {
        return false;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    public boolean canBeHitWithPotion() {
        return false;
    }

    @Override
    public void knockBack(@NotNull Entity entityIn, float strength, double xRatio, double zRatio) {}

    @Override
    public void setAir(int air) {
        super.setAir(300);
    }

    @Nullable
    public Entity getControllingPassenger() {
        return this.getPassengers().isEmpty() ? null : (Entity)this.getPassengers().getFirst();
    }

    @Override
    public boolean canBeSteered() {
        return !this.isDead && this.getControllingPassenger() instanceof EntityPlayer;
    }

    @Override
    public boolean processInteract(EntityPlayer player, @NonNull EnumHand hand) {
        if (player.isSneaking()) {
            return false;
        } else {
            if (!this.world.isRemote) {
                player.startRiding(this);
            }
            return true;
        }
    }

    @Override
    public void updatePassenger(@NonNull Entity passenger) {
        if (this.isPassenger(passenger)) {
            float f = MathHelper.cos(this.renderYawOffset * 0.017453292F);
            float f1 = MathHelper.sin(this.renderYawOffset * 0.017453292F);
            passenger.setPosition(
                    this.posX - (double)(27.0F * f1),
                    this.posY - 12.0D,
                    this.posZ + (double)(27.0F * f)
            );
        }
    }

}
