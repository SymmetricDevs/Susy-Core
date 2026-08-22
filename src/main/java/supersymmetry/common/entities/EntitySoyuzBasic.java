package supersymmetry.common.entities;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockWall;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import cam72cam.mod.entity.boundingbox.BoundingBox;
import supersymmetry.api.rocketry.rockets.AbstractRocketBlueprint;
import supersymmetry.client.audio.MovingSoundRocket;
import supersymmetry.client.renderer.handler.IAlwaysRender;
import supersymmetry.client.renderer.particles.SusyParticleSmokeLarge;
import supersymmetry.common.rocketry.SuccessCalculation.LaunchResult;

public class EntitySoyuzBasic extends EntityBlueprintRocket implements IAlwaysRender {

    /**
     * Horizontal (x, z) offsets of the four boosters plus the core engine.
     */
    private static final double[][] SOYUZ_ENGINE_OFFSETS = { { 0, 0 }, { 3, 0 }, { 0, 3 }, { -3, 0 }, { 0, -3 } };

    @SideOnly(Side.CLIENT)
    private MovingSoundRocket soundRocket;

    public EntitySoyuzBasic(World worldIn) {
        super(worldIn);
    }

    public EntitySoyuzBasic(World worldIn, double x, double y, double z, float rotationYaw) {
        super(worldIn, x, y, z, rotationYaw);
    }

    // Hull dimensions. Subclasses that reuse this rocket's behaviour with a smaller
    // model override these; they are
    // called from the constructor, so implementations must return constants.

    protected float getRocketWidth() {
        return 3F;
    }

    protected float getRocketHeight() {
        return 46F;
    }

    /**
     * Half-width of the collision box. Wider than the hull so that it covers the
     * strap-on boosters.
     */
    protected double getCollisionRadius() {
        return 5;
    }

    /**
     * Half-width of the rendered model's bounding box.
     */
    protected double getModelRadius() {
        return 4;
    }

    /**
     * Lowest point on the hull, in blocks above the base, where a player can climb
     * aboard.
     */
    protected double getBoardingWindowMin() {
        return 37;
    }

    /**
     * Highest point on the hull, in blocks above the base, where a player can climb
     * aboard.
     */
    protected double getBoardingWindowMax() {
        return 44;
    }

    public EntitySoyuzBasic(World worldIn, BlockPos pos, float rotationYaw) {
        this(worldIn, (float) pos.getX() + 0.5F, pos.getY(), (float) pos.getZ() + 0.5F, rotationYaw);
    }

    public EntitySoyuzBasic(World worldIn, Vec3d pos, float rotationYaw) {
        this(worldIn, pos.x, pos.y, pos.z, rotationYaw);
    }

    public void launchRocket() {
        if (this.getFuel() == null) {
            setCountdownStarted(false);
            return;
        }
        if (!world.isRemote) {
            if (this.getEntityData().hasKey("rocket")) {
                NBTTagCompound rocketNBT = this.getEntityData().getCompoundTag("rocket");
                AbstractRocketBlueprint blueprint = AbstractRocketBlueprint.getCopyOf(rocketNBT.getString("name"));
                blueprint.readFromNBT(rocketNBT);
                BlockPos assemblerPosition = BlockPos.fromLong(this.getEntityData().getLong("assemblerPosition"));
                if (!assemblerPosition.equals(BlockPos.NULL_VECTOR) &&
                        this.getPosition().distanceSq(assemblerPosition) < 100) {
                    this.setCrashPosition(assemblerPosition);
                    this.setLaunchResult(LaunchResult.CRASHES);
                } else {
                    long augmentation = rocketNBT.getLong("AFSimprovement");
                    if (this.getPassengers().stream()
                            .noneMatch((entity -> entity instanceof EntityPlayer player && player.isCreative()))) {
                        this.setLaunchResult(blueprint.calculateSuccess(this, augmentation));
                    } else {
                        this.setLaunchResult(LaunchResult.LAUNCHES);
                    }
                }
            } else {
                this.setLaunchResult(LaunchResult.EXPLODES);
            }
        }
        super.launchRocket();
    }

    @Override
    public void setDead() {
        super.setDead();
        if (world.isRemote && soundRocket != null)
            soundRocket.stopPlaying();
    }

    @Override
    protected float getExplosionStrength() {
        return 50; // Needs to cover the player
    }

    @SideOnly(Side.CLIENT)
    protected void spawnLaunchParticles(double v) {
        float startPos = this.getStartPos();
        float randFloat = getRNG().nextFloat();
        float randSpeed = getRNG().nextFloat();
        SusyParticleSmokeLarge smoke_x1 = new SusyParticleSmokeLarge(this.world, this.posX, startPos - 3, this.posZ,
                0.5 + randSpeed, v * (randFloat - 0.5) * 0.16, v * (randFloat - 0.5) * 0.16);
        SusyParticleSmokeLarge smoke_x2 = new SusyParticleSmokeLarge(this.world, this.posX, startPos - 3, this.posZ,
                -(0.5 + randSpeed), v * (randFloat - 0.5) * 0.16, v * (randFloat - 0.5) * 0.16);
        SusyParticleSmokeLarge smoke_z1 = new SusyParticleSmokeLarge(this.world, this.posX, startPos - 3, this.posZ,
                v * (randFloat - 0.5) * 0.16, v * (randFloat - 0.5) * 0.16, 0.5 + randSpeed);
        SusyParticleSmokeLarge smoke_z2 = new SusyParticleSmokeLarge(this.world, this.posX, startPos - 3, this.posZ,
                v * (randFloat - 0.5) * 0.16, v * (randFloat - 0.5) * 0.16, -(0.5 + randSpeed));
        Minecraft.getMinecraft().effectRenderer.addEffect(smoke_x1);
        Minecraft.getMinecraft().effectRenderer.addEffect(smoke_x2);
        Minecraft.getMinecraft().effectRenderer.addEffect(smoke_z1);
        Minecraft.getMinecraft().effectRenderer.addEffect(smoke_z2);
    }



    @Override
    public void onUpdate() {
        super.onUpdate();
        long age = this.world.getTotalWorldTime();
        int launchTime = this.getLaunchTime();

        if (isCountdownStarted() && world.isRemote) {
            if (launchTime - age > 50 && soundRocket == null) {
                setupRocketSound();
                soundRocket.startPlaying();
            }
            if (age % 2 == 0) {
                if (launchTime - age < 60 && launchTime - age > 0) {
                    this.spawnLaunchParticles(0.025 * (age - launchTime + 60));
                }
            }

        }
        if (isLaunched() && world.isRemote) {
            if (age % 2 == 0) {
                if (launchTime - age > -100 && launchTime - age < 0) {
                    this.spawnLaunchParticles(1.5);
                } else if (launchTime - age > -150 && launchTime - age < -100) {
                    this.spawnLaunchParticles(-0.03 * (age - launchTime + 150));
                }
            }
        }
    }

    @Override
    protected double[][] getEngineOffsets() {
        return SOYUZ_ENGINE_OFFSETS;
    }

    @SideOnly(Side.CLIENT)
    public void setupRocketSound() {
        this.soundRocket = new MovingSoundRocket(this);
        Minecraft.getMinecraft().getSoundHandler().playSound(this.soundRocket);
    }

    @Override
    public double getMountedYOffset() {
        return 38D;
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        AxisAlignedBB aabb = new AxisAlignedBB(passenger.getPosition()).grow(5, 1, 5);
        List<AxisAlignedBB> boxes = this.world.getCollisionBoxes(passenger, aabb);
        if (!boxes.isEmpty()) {
            Vec3d newPos = boxes.get(0).getCenter();
            passenger.setPosition(newPos.x, newPos.y, newPos.z);
            float f = passenger.width / 2.0F;
            float f1 = passenger.height;
            AxisAlignedBB pAABB = new AxisAlignedBB(newPos.x - (double) f, newPos.y, newPos.z - (double) f,
                    newPos.x + (double) f, newPos.y + (double) f1, newPos.z + (double) f);

            // Raise upwards until the passenger no longer intersects anything in the world
            while (world.collidesWithAnyBlock(pAABB)) {
                newPos = newPos.add(0, 0.5, 0);
                pAABB = new AxisAlignedBB(newPos.x - (double) f, newPos.y, newPos.z - (double) f, newPos.x + (double) f,
                        newPos.y + (double) f1, newPos.z + (double) f);
            }
            passenger.setPositionAndUpdate(newPos.x, newPos.y, newPos.z);
        }
    }

    // Fixes an issue related to intersecting with the transporter erector.
    @Override
    public void move(MoverType type, double x, double y, double z) {
        if (this.noClip) {
            this.setEntityBoundingBox(this.getEntityBoundingBox().offset(x, y, z));
            this.resetPositionToBB();
        } else {
            if (type == MoverType.PISTON) {
                return;
            }

            this.world.profiler.startSection("move");

            double d2 = x;
            double d3 = y;
            double d4 = z;

            List<AxisAlignedBB> list1 = this.world.getCollisionBoxes(this, this.getEntityBoundingBox().expand(x, y, z));
            AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();

            if (y != 0.0D) {
                int k = 0;

                for (int l = list1.size(); k < l; ++k) {
                    if (!(list1.get(k) instanceof BoundingBox)) // Hacky way to fix clipping into the T/E
                        y = list1.get(k).calculateYOffset(this.getEntityBoundingBox(), y);
                }

                this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, y, 0.0D));
            }

            if (x != 0.0D) {
                int j5 = 0;

                for (int l5 = list1.size(); j5 < l5; ++j5) {
                    if (!(list1.get(j5) instanceof BoundingBox))
                        x = list1.get(j5).calculateXOffset(this.getEntityBoundingBox(), x);
                }

                if (x != 0.0D) {
                    if (!(list1 instanceof BoundingBox))
                        this.setEntityBoundingBox(this.getEntityBoundingBox().offset(x, 0.0D, 0.0D));
                }
            }

            if (z != 0.0D) {
                int k5 = 0;

                for (int i6 = list1.size(); k5 < i6; ++k5) {
                    if (!(list1.get(k5) instanceof BoundingBox))
                        z = list1.get(k5).calculateZOffset(this.getEntityBoundingBox(), z);
                }

                if (z != 0.0D) {
                    this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, 0.0D, z));
                }
            }

            boolean flag = this.onGround || d3 != y && d3 < 0.0D;

            if (this.stepHeight > 0.0F && flag && (d2 != x || d4 != z)) {
                double d14 = x;
                double d6 = y;
                double d7 = z;
                AxisAlignedBB axisalignedbb1 = this.getEntityBoundingBox();
                this.setEntityBoundingBox(axisalignedbb);
                y = this.stepHeight;
                List<AxisAlignedBB> list = this.world.getCollisionBoxes(this,
                        this.getEntityBoundingBox().expand(d2, y, d4));
                AxisAlignedBB axisalignedbb2 = this.getEntityBoundingBox();
                AxisAlignedBB axisalignedbb3 = axisalignedbb2.expand(d2, 0.0D, d4);
                double d8 = y;
                int j1 = 0;

                for (int k1 = list.size(); j1 < k1; ++j1) {
                    d8 = list.get(j1).calculateYOffset(axisalignedbb3, d8);
                }

                axisalignedbb2 = axisalignedbb2.offset(0.0D, d8, 0.0D);
                double d18 = d2;
                int l1 = 0;

                for (int i2 = list.size(); l1 < i2; ++l1) {
                    d18 = list.get(l1).calculateXOffset(axisalignedbb2, d18);
                }

                axisalignedbb2 = axisalignedbb2.offset(d18, 0.0D, 0.0D);
                double d19 = d4;
                int j2 = 0;

                for (int k2 = list.size(); j2 < k2; ++j2) {
                    d19 = list.get(j2).calculateZOffset(axisalignedbb2, d19);
                }

                axisalignedbb2 = axisalignedbb2.offset(0.0D, 0.0D, d19);
                AxisAlignedBB axisalignedbb4 = this.getEntityBoundingBox();
                double d20 = y;
                int l2 = 0;

                for (int i3 = list.size(); l2 < i3; ++l2) {
                    d20 = list.get(l2).calculateYOffset(axisalignedbb4, d20);
                }

                axisalignedbb4 = axisalignedbb4.offset(0.0D, d20, 0.0D);
                double d21 = d2;
                int j3 = 0;

                for (int k3 = list.size(); j3 < k3; ++j3) {
                    d21 = list.get(j3).calculateXOffset(axisalignedbb4, d21);
                }

                axisalignedbb4 = axisalignedbb4.offset(d21, 0.0D, 0.0D);
                double d22 = d4;
                int l3 = 0;

                for (int i4 = list.size(); l3 < i4; ++l3) {
                    d22 = list.get(l3).calculateZOffset(axisalignedbb4, d22);
                }

                axisalignedbb4 = axisalignedbb4.offset(0.0D, 0.0D, d22);
                double d23 = d18 * d18 + d19 * d19;
                double d9 = d21 * d21 + d22 * d22;

                if (d23 > d9) {
                    x = d18;
                    z = d19;
                    y = -d8;
                    this.setEntityBoundingBox(axisalignedbb2);
                } else {
                    x = d21;
                    z = d22;
                    y = -d20;
                    this.setEntityBoundingBox(axisalignedbb4);
                }

                int j4 = 0;

                for (int k4 = list.size(); j4 < k4; ++j4) {
                    y = list.get(j4).calculateYOffset(this.getEntityBoundingBox(), y);
                }

                this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, y, 0.0D));

                if (d14 * d14 + d7 * d7 >= x * x + z * z) {
                    x = d14;
                    y = d6;
                    z = d7;
                    this.setEntityBoundingBox(axisalignedbb1);
                }
            }

            this.world.profiler.endSection();
            this.world.profiler.startSection("rest");
            this.resetPositionToBB();
            this.collidedHorizontally = d2 != x || d4 != z;
            this.collidedVertically = d3 != y;
            this.onGround = this.collidedVertically && d3 < 0.0D;
            this.collided = this.collidedHorizontally || this.collidedVertically;
            int j6 = MathHelper.floor(this.posX);
            int i1 = MathHelper.floor(this.posY - 0.20000000298023224D);
            int k6 = MathHelper.floor(this.posZ);
            BlockPos blockpos = new BlockPos(j6, i1, k6);
            IBlockState iblockstate = this.world.getBlockState(blockpos);

            if (iblockstate.getMaterial() == Material.AIR) {
                BlockPos blockpos1 = blockpos.down();
                IBlockState iblockstate1 = this.world.getBlockState(blockpos1);
                Block block1 = iblockstate1.getBlock();

                if (block1 instanceof BlockFence || block1 instanceof BlockWall || block1 instanceof BlockFenceGate) {
                    iblockstate = iblockstate1;
                    blockpos = blockpos1;
                }
            }

            this.updateFallState(y, this.onGround, iblockstate, blockpos);

            if (d2 != x) {
                this.motionX = 0.0D;
            }

            if (d4 != z) {
                this.motionZ = 0.0D;
            }

            Block block = iblockstate.getBlock();

            if (d3 != y) {
                block.onLanded(this.world, this);
            }

            try {
                this.doBlockCollisions();
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Checking entity block collision");
                CrashReportCategory crashreportcategory = crashreport
                        .makeCategory("Entity being checked for collision");
                this.addEntityCrashInfo(crashreportcategory);
                throw new ReportedException(crashreport);
            }

            this.world.profiler.endSection();
        }
    }
}
