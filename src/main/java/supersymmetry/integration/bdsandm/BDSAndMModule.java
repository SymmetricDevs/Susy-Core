package supersymmetry.integration.bdsandm;

import funwayguy.bdsandm.blocks.tiles.TileEntityBarrel;
import funwayguy.bdsandm.core.BDSM;
import gregtech.api.GTValues;
import gregtech.api.cover.CoverRayTracer;
import gregtech.api.modules.GregTechModule;
import gregtech.common.items.tool.rotation.CustomBlockRotations;
import gregtech.common.items.tool.rotation.ICustomRotationBehavior;
import gregtech.integration.IntegrationSubmodule;
import net.minecraft.block.BlockDirectional;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import supersymmetry.modules.SuSyModules;

import java.util.Objects;

@GregTechModule(
        moduleID = SuSyModules.MODULE_BDSAndM,
        containerID = GTValues.MODID,
        modDependencies = "bdsandm",
        name = "SuSy BDSAndM Integration",
        description = "BDSAndM Integration Module")
public class BDSAndMModule extends IntegrationSubmodule {

    public static final ICustomRotationBehavior BDSAndM_BARREL_BEHAVIOR = (state, world, pos, hitResult) -> {
        EnumFacing gridSide = CoverRayTracer.determineGridSideHit(hitResult);
        if (gridSide == null) return false;
        gridSide = gridSide.getOpposite(); // IDK what's happening here, blame the original author
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

    @Override
    public void init(FMLInitializationEvent event) {
        getLogger().info("BDSAndM found. Enabling integration...");
        CustomBlockRotations.registerCustomRotation(BDSM.blockMetalBarrel, BDSAndM_BARREL_BEHAVIOR);
        CustomBlockRotations.registerCustomRotation(BDSM.blockWoodBarrel, BDSAndM_BARREL_BEHAVIOR);
    }
}
