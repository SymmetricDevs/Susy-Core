package supersymmetry.integration.bdsandm;

import funwayguy.bdsandm.blocks.tiles.TileEntityBarrel;
import funwayguy.bdsandm.core.BDSM;
import gregtech.api.cover.CoverRayTracer;
import gregtech.common.items.tool.rotation.CustomBlockRotations;
import gregtech.common.items.tool.rotation.ICustomRotationBehavior;
import net.minecraft.block.BlockDirectional;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import java.util.Objects;

public class BDSAndMModule {

    public static final ICustomRotationBehavior BDSAndM_BARREL_BEHAVIOR = (state, world, pos, hitResult) -> {
        EnumFacing gridSide = CoverRayTracer.determineGridSideHit(hitResult);
        if (gridSide == null) return false;
        gridSide = gridSide.getOpposite(); // idk what's happening here, blame the original author
        if (gridSide != state.getValue(BlockDirectional.FACING)) {
            TileEntityBarrel barrel = ((TileEntityBarrel) world.getTileEntity(pos));
            if (barrel != null) {
                state = state.withProperty(BlockDirectional.FACING, gridSide);
                NBTTagCompound tagCompound = barrel.writeToNBT(new NBTTagCompound());
                world.setBlockState(pos, state);
                Objects.requireNonNull(world.getTileEntity(pos)).readFromNBT(tagCompound);
                return true;
            }
        }
        return false;
    };

    public static void init() {
        CustomBlockRotations.registerCustomRotation(BDSM.blockMetalBarrel, BDSAndM_BARREL_BEHAVIOR);
        CustomBlockRotations.registerCustomRotation(BDSM.blockWoodBarrel, BDSAndM_BARREL_BEHAVIOR);
    }
}
