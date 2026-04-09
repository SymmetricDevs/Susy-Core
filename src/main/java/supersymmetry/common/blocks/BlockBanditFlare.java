package supersymmetry.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import java.util.Random;

public class BlockBanditFlare extends Block {
    //WIP
    public BlockBanditFlare() {
        super(Material.IRON);
        this.setTranslationKey("bandit_flare_block");
        this.setHardness(0.5f);
        this.setSoundType(SoundType.METAL);
        this.setHarvestLevel("pickaxe", 1);
        this.setRegistryName(new ResourceLocation("susy", "bandit_flare_block"));
    }

    //example override method
    @Override
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if (!worldIn.isRemote) return; // CLIENT SIDE ONLY

        // Spawn multiple particles per tick for a strong flare effect
        for (int i = 0; i < 10; i++) {

            double x = pos.getX() + 0.5 + (rand.nextDouble() - 0.5) * 0.3;
            double y = pos.getY() + rand.nextDouble() * 1.5;
            double z = pos.getZ() + 0.5 + (rand.nextDouble() - 0.5) * 0.3;

            double motionX = 0;
            double motionY = 0.1 + rand.nextDouble() * 0.2; // upward motion
            double motionZ = 0;

            worldIn.spawnParticle(
                    EnumParticleTypes.REDSTONE,
                    x, y, z,
                    motionX, motionY, motionZ
            );
        }
    }
}
