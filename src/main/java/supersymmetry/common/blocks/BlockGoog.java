package supersymmetry.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import gregtech.client.renderer.texture.cube.SimpleCubeRenderer;

public class BlockGoog extends Block {

    public static final SimpleCubeRenderer GOOG = new SimpleCubeRenderer("goog");

    public BlockGoog() {
        super(Material.CARPET);
        setTranslationKey("goog");
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
                                    EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            float pitch = worldIn.rand.nextFloat() * 0.2F + 0.9F;
            worldIn.playSound(null, pos, SoundEvents.ENTITY_CAT_PURREOW, SoundCategory.NEUTRAL, 0.5F, pitch);
        }
        return true;
    }
}
