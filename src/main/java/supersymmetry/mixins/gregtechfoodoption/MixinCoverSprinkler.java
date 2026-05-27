package supersymmetry.mixins.gregtechfoodoption;

import gregtech.api.GregTechAPI;
import gregtech.api.unification.material.Material;
import gregtechfoodoption.covers.CoverSprinkler;
import net.minecraft.block.BlockFire;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import supersymmetry.common.util.FireSuppressantProperty;

@Mixin(value = CoverSprinkler.class, remap = false)
public abstract class MixinCoverSprinkler {


    @Shadow
    public abstract BlockPos getPos();

    @Inject(method = "update", at = @At("TAIL"))
    private void onUpdate(CallbackInfo ci) {
        CoverSprinkler self = (CoverSprinkler) (Object) this;

        if (self.getWorld().isRemote) return;

        IFluidHandler fluidHandler = self.getCoverableView()
                .getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, self.getAttachedSide());
        if (fluidHandler == null) return;

        FluidStack fluid = fluidHandler.drain(Integer.MAX_VALUE, false);
        if (fluid == null) return;

        boolean canSuppressFire = false;
        int fireBlocksPerTick = 1;

        Material mat = GregTechAPI.materialManager.getMaterial(fluid.getFluid().getName());
        if (mat != null) {
            FireSuppressantProperty fireProp = mat.getProperty(FireSuppressantProperty.FIRE_SUPPRESSANT);
            if (fireProp != null) {
                canSuppressFire = true;
                fireBlocksPerTick = fireProp.getBlocksPerTick();
            }
        }

        if (canSuppressFire) {
            suppressFire(self.getWorld(), getPos(), fireBlocksPerTick);
        }
    }

    private void suppressFire(World world, BlockPos basePos, int maxBlocks) {
        int suppressed = 0;
        AxisAlignedBB area = new AxisAlignedBB(
                basePos.offset(EnumFacing.SOUTH, 4).offset(EnumFacing.EAST, 4),
                basePos.offset(EnumFacing.NORTH, 4).offset(EnumFacing.WEST, 4)
        ).grow(0.1);

        int minX = (int) Math.floor(area.minX);
        int minZ = (int) Math.floor(area.minZ);
        int maxX = (int) Math.ceil(area.maxX);
        int maxZ = (int) Math.ceil(area.maxZ);
        int y    = basePos.getY();

        for (int scanY = y; scanY <= y + 1 && suppressed < maxBlocks; scanY++) {
            for (int x = minX; x <= maxX && suppressed < maxBlocks; x++) {
                for (int z = minZ; z <= maxZ && suppressed < maxBlocks; z++) {
                    BlockPos candidate = new BlockPos(x, scanY, z);
                    if (world.getBlockState(candidate).getBlock() instanceof BlockFire) {
                        world.setBlockToAir(candidate);
                        suppressed++;
                    }
                }
            }
        }
    }
}
