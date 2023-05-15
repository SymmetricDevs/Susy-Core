package supersymmetry.common.blocks;

import gregtech.api.block.VariantBlock;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockHome extends VariantBlock<BlockHome.HomeType> {

    public BlockHome() {
        super(Material.IRON);
        this.setTranslationKey("home_block");
        this.setHardness(0.5f);
        this.setSoundType(SoundType.METAL);
        this.setHarvestLevel("wrench", 2);
        this.setDefaultState(getState(HomeType.PRIMITIVE));
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos, @NotNull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    public boolean isBed(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable Entity player) {
        return true;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (worldIn.isRemote) {
            return false;
        } else {
            if (worldIn.provider.canRespawnHere() && worldIn.getBiome(pos) != net.minecraft.init.Biomes.HELL) {
                playerIn.sendStatusMessage(new TextComponentTranslation("tile.home.allowed"), true);
                net.minecraftforge.event.ForgeEventFactory.onPlayerSpawnSet(playerIn, pos, true);
                playerIn.bedLocation = pos;
                playerIn.setSpawnPoint(playerIn.bedLocation, false);
            } else {
                playerIn.sendStatusMessage(new TextComponentTranslation("tile.home.denied"), true);
            }
            return true;
        }
    }

    public enum HomeType implements IStringSerializable {
        PRIMITIVE("primitive"),
        GT_BRUTALIST("gt_brutalist"),
        RENEWAL_BRUTALIST("renewal_brutalist"),
        SCIFI("scifi");


        public final String name;

        HomeType(String name) {
            this.name = name;
        }

        @NotNull
        @Override
        public String getName() {
            return this.name;
        }

        public String toString() {
            return this.getName();
        }
    }
}
