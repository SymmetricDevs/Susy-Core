package supersymmetry.common.blocks;

import gregtech.api.fluids.GTFluidBlock;
import gregtech.api.fluids.GTFluidMaterial;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import supersymmetry.common.metatileentities.single.electric.MetaTileEntityDefoliator.DefoliatorReplacements;

public class BlockMercuryFluid extends GTFluidBlock {
  private static Material m = Materials.Mercury;

  static {
    // not sure how thats gonna go
    m.getFluid().setDensity(13500);
  }

  public BlockMercuryFluid() {
    super(m.getFluid(), new GTFluidMaterial(MapColor.GRAY, true), m);
    this.setQuantaPerBlock(16); // need to basically reimplement FluidBase to set this higher
    this.setTickRate(5);
    this.setTickRandomly(true);
  }

  @Override
  public void updateTick(World world, BlockPos pos, IBlockState selfstate, Random rand) {
    Map<ResourceLocation, IBlockState> r = DefoliatorReplacements.getReplacements();
    Map<String, IBlockState> r2 = DefoliatorReplacements.getMetaReplacements();

    // if (rand.nextInt() % 100 < 20) {
    {
      // for (int i = 0; i < 5; i++) {
        int dx = rand.nextInt(10) - 5;
        int dz = rand.nextInt(10) - 5;
        int dy = rand.nextInt(2) - 1;

        BlockPos target = pos.add(dx, dy, dz);

        if (!world.isValid(target)) return;

        IBlockState state = world.getBlockState(target);
        Block block = state.getBlock();
        if (block instanceof BlockCrops) {
          world.destroyBlock(target, false);
          // continue;
          return;
        }
        ResourceLocation blockName = ForgeRegistries.BLOCKS.getKey(block);
        if (blockName == null) return;

        String metaKey = blockName + ":" + block.getMetaFromState(state);
        IBlockState replacement = r2.get(metaKey);

        if (replacement == null) replacement = r.get(blockName);

        if (replacement != null) {
          world.setBlockState(target, replacement, 2);
          // SusyLog.logger.info(
          //     "{} -> {} = {}", pos, target, replacement.getBlock().getLocalizedName());
        }
      }
    // }
    super.updateTick(world, pos, selfstate, rand);
  }

  @Override
  public void onEntityCollision(
      @NotNull World worldIn,
      @NotNull BlockPos pos,
      @NotNull IBlockState state,
      @NotNull Entity entityIn) {
    super.onEntityCollision(worldIn, pos, state, entityIn);
    if (entityIn instanceof EntityLivingBase && worldIn.getTotalWorldTime() % 20 == 0) {
      ((EntityLivingBase) entityIn).addPotionEffect(new PotionEffect(MobEffects.POISON, 80, 0));
    }
  }
}
