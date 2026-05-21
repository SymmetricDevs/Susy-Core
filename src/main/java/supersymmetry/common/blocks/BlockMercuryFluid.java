package supersymmetry.common.blocks;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

import gregtech.api.fluids.GTFluidBlock;
import gregtech.api.fluids.GTFluidMaterial;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import supersymmetry.common.item.SuSyArmorItem;
import supersymmetry.common.item.armor.AdvancedBreathingTank;
import supersymmetry.common.item.armor.BreathingApparatus;
import supersymmetry.common.metatileentities.single.electric.MetaTileEntityDefoliator.DefoliatorReplacements;

public class BlockMercuryFluid extends GTFluidBlock {

    private static Material m = Materials.Mercury;

    private static final Map<ResourceLocation, IBlockState> REPLACEMENTS = DefoliatorReplacements.getReplacements();
    private static final Map<String, IBlockState> META_REPLACEMENTS = DefoliatorReplacements.getMetaReplacements();

    static {
        m.getFluid().setDensity(13500);
        m.getFluid().setLuminosity(0);
        // #757a80
        m.getFluid().setColor(0x757a80);
    }

    public BlockMercuryFluid() {
        super(m.getFluid(), new GTFluidMaterial(MapColor.GRAY, true), m);
        this.setQuantaPerBlock(16); // need to basically reimplement FluidBase to set this higher
        this.setTickRate(1);
        this.lightOpacity = 255;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.SOLID;
    }

    @Override
    public void randomTick(World world, BlockPos pos, IBlockState state2, Random rand) {
        for (int i = 0; i < 3; i++) {
            poisonSurroundings(world, pos, rand);
        }
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState selfstate, Random rand) {
        // poisonSurroundings(world, pos, rand);
        super.updateTick(world, pos, selfstate, rand);
    }

    @Override
    public void onEntityCollision(
                                  @NotNull World worldIn,
                                  @NotNull BlockPos pos,
                                  @NotNull IBlockState state,
                                  @NotNull Entity entityIn) {
        super.onEntityCollision(worldIn, pos, state, entityIn);
        final String LAST_COLLISION_KEY = "susy.mercury.collisionTick";
        if (entityIn instanceof EntityPlayer player) {
            // this makes it so that this function is only called once properly even when the player
            // stands in multiple mercury blocks
            NBTTagCompound data = player.getEntityData();
            if (data.getLong(LAST_COLLISION_KEY) == worldIn.getTotalWorldTime()) return;
            data.setLong(LAST_COLLISION_KEY, worldIn.getTotalWorldTime());
        }
        entityIn.addVelocity(0, 0.06, 0);
        if (!(entityIn instanceof EntityLivingBase living && worldIn.getTotalWorldTime() % 20 == 0))
            return;
        // full armor set check
        if (living instanceof EntityPlayer player) {
            ItemStack chest = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
            if (chest.getItem() instanceof SuSyArmorItem chestItem &&
                    chestItem.getItem(chest).getArmorLogic() instanceof AdvancedBreathingTank tank &&
                    tank.getOxygen(chest) > 0 && Arrays.stream(EntityEquipmentSlot.values())
                            .filter(s -> s.getSlotType() == EntityEquipmentSlot.Type.ARMOR)
                            .map(player::getItemStackFromSlot)
                            .allMatch(
                                    stack -> stack.getItem() instanceof SuSyArmorItem armor &&
                                            armor.getItem(stack).getArmorLogic() instanceof BreathingApparatus)) {
                if (!player.isInsideOfMaterial(net.minecraft.block.material.Material.WATER)) {
                    tank.changeOxygen(chest, -1);
                }
                return;
            }
        }
        living.addPotionEffect(new PotionEffect(MobEffects.POISON, 80, 1));
        living.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 80, 1));
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        return 0;
    }

    @Override
    protected int calculateFlowCost(World world, BlockPos pos, int recurseDepth, int side) {
        int cost = 1000;
        for (int adjSide = 0; adjSide < 4; adjSide++) {
            if (SIDES.get(adjSide) == SIDES.get(side).getOpposite()) continue;
            BlockPos pos2 = pos.offset(SIDES.get(adjSide));
            if (!canFlowInto(world, pos2) || isSourceBlock(world, pos2)) continue;
            if (canFlowInto(world, pos2.up(densityDir))) return recurseDepth;
            if (recurseDepth >= 3) continue;
            cost = Math.min(cost, calculateFlowCost(world, pos2, recurseDepth + 1, adjSide));
        }
        return cost;
    }

    private void poisonSurroundings(World world, BlockPos pos, Random rand) {
        int dx = rand.nextInt(8) - rand.nextInt(8);
        int dz = rand.nextInt(8) - rand.nextInt(8);
        int dy = rand.nextInt(6) - rand.nextInt(2);

        BlockPos target = pos.add(dx, dy, dz);
        if (target == pos) return;
        // copied from the defoliator
        if (!world.isBlockLoaded(target)) return;

        IBlockState state = world.getBlockState(target);
        Block block = state.getBlock();
        if (block == Blocks.AIR) {
            return;
        }
        if (block instanceof BlockCrops) {
            world.destroyBlock(target, false);
            return;
        }
        ResourceLocation blockName = block.getRegistryName();
        if (blockName == null) return;

        String metaKey = blockName + ":" + block.getMetaFromState(state);
        IBlockState replacement = META_REPLACEMENTS.get(metaKey);

        if (replacement == null) replacement = REPLACEMENTS.get(blockName);

        if (replacement != null) {
            world.setBlockState(target, replacement, 3);
            // SusyLog.logger.info(
            // "{} -> {} = {}", pos, target, replacement.getBlock().getLocalizedName());
        }
    }
}
