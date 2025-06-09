package supersymmetry.mixins.minecraft;

import net.minecraft.block.BlockFire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import supersymmetry.common.world.WorldProviderPlanet;

@Mixin(BlockFire.class)
public class BlockFireMixin {
    @Inject(method = "onBlockAdded", at = @At("HEAD"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void blockFire(World worldIn, BlockPos pos, IBlockState state, CallbackInfo ci) {
        if (worldIn.provider instanceof WorldProviderPlanet provider && !provider.getPlanet().supportsFire) {
            worldIn.setBlockToAir(pos);
        }
    }
}
