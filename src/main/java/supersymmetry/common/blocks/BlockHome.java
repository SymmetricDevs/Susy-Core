package supersymmetry.common.blocks;

import javax.annotation.Nullable;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

import supersymmetry.api.blocks.VariantHorizontalRotatableBlock;

public class BlockHome extends VariantHorizontalRotatableBlock<BlockHome.HomeType> {

    public BlockHome() {
        super(Material.IRON);
        this.setTranslationKey("home_block");
        this.setHardness(0.5f);
        this.setSoundType(SoundType.METAL);
        this.setHarvestLevel("pickaxe", 1);
        this.setDefaultState(getState(HomeType.HOME_PRIMITIVE));
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    public boolean isBed(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                         @Nullable Entity player) {
        return true;
    }

    @Override
    public boolean onBlockActivated(World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state,
                                    @NotNull EntityPlayer playerIn,
                                    @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY,
                                    float hitZ) {
        if (worldIn.isRemote) return true;
        if ((worldIn.provider.canRespawnHere() && worldIn.getBiome(pos) != net.minecraft.init.Biomes.HELL)) {
            net.minecraftforge.event.ForgeEventFactory.onPlayerSpawnSet(playerIn, pos, true);
            playerIn.bedLocation = pos;
            playerIn.setSpawnPoint(playerIn.bedLocation, false);
            playerIn.sendStatusMessage(new TextComponentTranslation("tile.home.allowed"), true);
            return true;
        } else {
            playerIn.sendStatusMessage(new TextComponentTranslation("tile.home.denied"), true);
            return false;
        }
    }

    public enum HomeType implements IStringSerializable {

        HOME_PRIMITIVE("home_primitive"),
        HOME_GT_BRUTALIST("home_gt_brutalist"),
        HOME_RENEWAL_BRUTALIST("home_renewal_brutalist"),
        HOME_SCIFI("home_scifi");

        public final String name;

        HomeType(String name) {
            this.name = name;
        }

        @NotNull
        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String toString() {
            return this.getName();
        }
    }
}
