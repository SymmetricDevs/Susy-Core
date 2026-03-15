package supersymmetry.common.entities;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import supersymmetry.api.SusyLog;
import supersymmetry.api.util.FisherPlane;
import supersymmetry.client.renderer.particles.SusyParticleSmokeLarge;

import java.util.Random;

public class EntityExplosion extends Entity {

    private int power;
    private boolean[][][] grid;
    private Vec3d fisherNormal;
    private static final Random rnd = new Random();
    private static final double v0 = 1d;

    public EntityExplosion(World worldIn) {
        super(worldIn);
        this.power = 5;
    }

    public EntityExplosion(World worldIn, double x, double y, double z) {
        this(worldIn);
        this.setPosition(x, y, z);
    }

    public EntityExplosion(World worldIn, double x, double y, double z, int power) {
        this(worldIn, x, y, z);
        this.power = power;
    }


    @Override
    protected void entityInit() {

    }

    @Override
    public void onUpdate() {

        super.onUpdate();

        if (this.ticksExisted == 1) {
            this.grid = new boolean[2 * this.power][2 * this.power][2 * this.power];
        } else if (this.ticksExisted == 2) {
            //SusyLog.logger.info("Filling grid");
            fillGrid();
        } else if (this.ticksExisted == 3) {
            //SusyLog.logger.info("Computing fisher");
            this.fisherNormal = FisherPlane.fisherNormal(grid);
            //SusyLog.logger.info(fisherNormal);
        } else if (this.ticksExisted == 100) {
            //SusyLog.logger.info("Boom");
            explode();
            if (world.isRemote) {
                spawnExplosionParticles();
            }
            this.setDead();
        }

    }

    protected void explode() {
        this.world.createExplosion(null, this.posX, this.posY, this.posZ, 5f, true);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean isInRangeToRenderDist(double distance) {
        return false;
    }

    @SideOnly(Side.CLIENT)
    protected void spawnExplosionParticles() {
        double v0scaled;
        for (int i = 0; i < 25; i++) {
            v0scaled = v0 * rnd.nextFloat() * 2f;
            SusyParticleSmokeLarge smoke = new SusyParticleSmokeLarge(
                    this.world,
                    this.posX + this.power * (rnd.nextFloat() - 0.5) * 0.5,
                    this.posY + this.power * (rnd.nextFloat() - 0.5) * 0.5,
                    this.posZ + this.power * (rnd.nextFloat() - 0.5) * 0.5,
                    v0scaled * (this.fisherNormal.x + 0.5 * (rnd.nextFloat() - 0.5)),
                    v0scaled * (this.fisherNormal.y + 0.5 * (rnd.nextFloat() - 0.5)),
                    v0scaled * (this.fisherNormal.z + 0.5 * (rnd.nextFloat() - 0.5))
            );
            Minecraft.getMinecraft().effectRenderer.addEffect(smoke);
        }
    }

    protected void fillGrid() {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos((int)this.posX - power, (int)this.posY - power, (int)this.posZ - power);
        int d = 2 * power;
        //SusyLog.logger.info(d);
        //SusyLog.logger.info(grid.length);
        for (int i = 0; i < d; i++) {
            for (int j = 0; j < d; j++) {
                for (int k = 0; k < d; k++) {
                    BlockPos newPos = p.add(i, j, k);
                    //SusyLog.logger.info(i);
                    //SusyLog.logger.info(j);
                    //SusyLog.logger.info(k);
                    grid[i][j][k] = world.getBlockState(newPos) == Blocks.AIR.getDefaultState();
                }
            }
        }
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {

    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {

    }
}
