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

    private int fireSuppressTimer = 0;

    @Shadow
    public abstract BlockPos getPos();

    @Inject(method = "update", at = @At("TAIL"))
    private void onUpdate(CallbackInfo ci) {
        CoverSprinkler self = (CoverSprinkler) (Object) this;

        if (self.getWorld().isRemote) return;

        if (++fireSuppressTimer < 20) return;
        fireSuppressTimer = 0;

        IFluidHandler fluidHandler = self.getCoverableView()
                .getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, self.getAttachedSide());
        if (fluidHandler == null) return;

        FluidStack fluid = fluidHandler.drain(Integer.MAX_VALUE, false);
        if (fluid == null) return;

        boolean canSuppressFire = false;

        Material mat = GregTechAPI.materialManager.getMaterial(fluid.getFluid().getName());
        if (mat != null) {
            FireSuppressantProperty fireProp = mat.getProperty(FireSuppressantProperty.FIRE_SUPPRESSANT);
            if (fireProp != null) {
                canSuppressFire = true;
            }
        }

        if (canSuppressFire) {
            suppressFire(self.getWorld(), self.getCoverableView().getPos());
        }
    }

    private static final int FIRE_SUPPRESSION_HEIGHT = 9;

    private void suppressFire(World world, BlockPos basePos) {

        System.out.println("FIRE DETECTED");
        AxisAlignedBB area = new AxisAlignedBB(
                basePos.offset(EnumFacing.SOUTH, 4).offset(EnumFacing.EAST, 4),
                basePos.offset(EnumFacing.NORTH, 4).offset(EnumFacing.WEST, 4)
        ).grow(0.1);

        int minX = (int) Math.floor(area.minX);
        int minZ = (int) Math.floor(area.minZ);
        int maxX = (int) Math.ceil(area.maxX);
        int maxZ = (int) Math.ceil(area.maxZ);
        int minY = basePos.getY() - FIRE_SUPPRESSION_HEIGHT + 1;
        int maxY = basePos.getY();

        for (int scanY = minY; scanY <= maxY; scanY++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos candidate = new BlockPos(x, scanY, z);
                    if (world.getBlockState(candidate).getBlock() instanceof BlockFire) {
                        world.setBlockToAir(candidate);
                    }
                }
            }
        }
    }
}
