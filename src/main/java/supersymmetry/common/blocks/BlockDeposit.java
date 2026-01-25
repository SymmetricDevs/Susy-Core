package supersymmetry.common.blocks;

import javax.annotation.Nonnull;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

import gregtech.api.block.VariantBlock;

public class BlockDeposit extends VariantBlock<BlockDeposit.DepositBlockType> {

    public BlockDeposit() {
        super(Material.ROCK);
        setTranslationKey("deposit_block");
        setResistance(1200.0f);
        setSoundType(SoundType.METAL);
        setDefaultState(getState(DepositBlockType.ORTHOMAGMATIC));
        setBlockUnbreakable();
    }

    @Override
    public boolean canSilkHarvest(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state,
                                  @NotNull EntityPlayer player) {
        return false;
    }

    @NotNull
    @Override
    protected ItemStack getSilkTouchDrop(@NotNull IBlockState state) {
        return new ItemStack(Blocks.AIR, 1);
    }

    @NotNull
    @Override
    @SuppressWarnings("deprecation")
    public EnumPushReaction getPushReaction(@NotNull IBlockState state) {
        return EnumPushReaction.BLOCK;
    }

    @Override
    public void dropBlockAsItemWithChance(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state,
                                          float chance, int fortune) {
        super.dropBlockAsItemWithChance(worldIn, pos, state, 0.0F, 0);
    }

    public enum DepositBlockType implements IStringSerializable {

        ORTHOMAGMATIC("orthomagmatic"),
        METAMORPHIC("metamorphic"),
        SEDIMENTARY("sedimentary"),
        HYDROTHERMAL("hydrothermal"),
        ALLUVIAL("alluvial"),
        MAGMATIC_HYDROTHERMAL("magmatic_hydrothermal"),
        ICE_CAP("ice_cap"),
        CRATER_DEPOSIT("crater_deposit");

        private final String name;

        DepositBlockType(String name) {
            this.name = name;
        }

        @Nonnull
        public String getName() {
            return this.name;
        }
    }
}
