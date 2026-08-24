package supersymmetry.common.blocks;

import java.util.Random;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

public class BlockActiveEvaporationBed extends RedstoneActiveBlock<BlockEvaporationBed.EvaporationBedType> {

    public BlockActiveEvaporationBed() {
        this(false);
    }

    protected BlockActiveEvaporationBed(boolean inverted) {
        super(Material.CLAY, inverted);
        setTranslationKey("evaporation_bed_active");
        setHardness(0.5f);
        setResistance(0.5f);
        setSoundType(SoundType.GROUND);
        setHarvestLevel("shovel", 0);
        setDefaultState(getState(BlockEvaporationBed.EvaporationBedType.DIRT));
    }

    @NotNull @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.SOLID;
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    protected boolean isBloomEnabled(BlockEvaporationBed.EvaporationBedType value) {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(@NotNull IBlockState stateIn, @NotNull World worldIn, @NotNull BlockPos pos,
                                  @NotNull Random random) {
        if (!isEffectActive(stateIn) || random.nextInt(4) != 0) {
            return;
        }
        float x = pos.getX() + random.nextFloat();
        float y = pos.getY() + 0.75F + random.nextFloat() * 0.25F;
        float z = pos.getZ() + random.nextFloat();
        float vx = 0.02F * random.nextFloat() - 0.01F;
        float vy = 0.05F + 0.10F * random.nextFloat();
        float vz = 0.02F * random.nextFloat() - 0.01F;
        worldIn.spawnParticle(EnumParticleTypes.CLOUD, x, y, z, vx, vy, vz);
    }
}
