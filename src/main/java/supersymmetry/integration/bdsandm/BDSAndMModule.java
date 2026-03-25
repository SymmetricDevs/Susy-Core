package supersymmetry.integration.bdsandm;

import net.minecraft.block.BlockDirectional;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

import funwayguy.bdsandm.core.BDSM;
import gregtech.api.cover.CoverRayTracer;
import gregtech.api.modules.GregTechModule;
import gregtech.common.items.tool.rotation.CustomBlockRotations;
import gregtech.common.items.tool.rotation.ICustomRotationBehavior;
import gregtech.integration.IntegrationSubmodule;
import supersymmetry.Supersymmetry;
import supersymmetry.api.SusyLog;
import supersymmetry.modules.SuSyModules;

@GregTechModule(
                moduleID = SuSyModules.MODULE_BDSAndM,
                containerID = Supersymmetry.MODID,
                modDependencies = "bdsandm",
                name = "SuSy BDSAndM Integration",
                description = "SuSy BDSAndM Integration Module")
public class BDSAndMModule extends IntegrationSubmodule {

    public static final ICustomRotationBehavior BDSAndM_BARREL_BEHAVIOR = (state, world, pos, hitResult) -> {
        EnumFacing gridSide = CoverRayTracer.determineGridSideHit(hitResult);
        if (gridSide == null) return false;
        gridSide = gridSide.getOpposite(); // IDK what's happening here, blame the original author
        if (gridSide != state.getValue(BlockDirectional.FACING)) {
            world.setBlockState(pos, state.withProperty(BlockDirectional.FACING, gridSide));
            return true;
        }
        return false;
    };

    @Override
    public void init(FMLInitializationEvent event) {
        SusyLog.logger.info("BDSAndM found. Enabling integration...");
        CustomBlockRotations.registerCustomRotation(BDSM.blockMetalBarrel, BDSAndM_BARREL_BEHAVIOR);
        CustomBlockRotations.registerCustomRotation(BDSM.blockWoodBarrel, BDSAndM_BARREL_BEHAVIOR);
        CustomBlockRotations.registerCustomRotation(BDSM.blockMetalCrate, BDSAndM_BARREL_BEHAVIOR);
        CustomBlockRotations.registerCustomRotation(BDSM.blockWoodCrate, BDSAndM_BARREL_BEHAVIOR);
    }
}
