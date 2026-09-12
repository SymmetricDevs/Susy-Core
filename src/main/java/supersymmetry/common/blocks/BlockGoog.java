package supersymmetry.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
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
    public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn) {
        float pitch = worldIn.rand.nextFloat() * 0.2F + 0.9F;
        worldIn.playSound(null, pos, SoundEvents.ENTITY_CAT_PURREOW, SoundCategory.NEUTRAL, 0.5F, pitch);
    }
}
