package supersymmetry.integration.groovyscript;

import com.cleanroommc.groovyscript.api.GroovyPlugin;
import com.cleanroommc.groovyscript.compat.mods.GroovyContainer;
import com.cleanroommc.groovyscript.sandbox.expand.ExpansionHelper;
import dev.tianmi.sussypatches.common.SusConfig;
import gregtech.api.fluids.FluidBuilder;
import gregtech.api.modules.GregTechModule;
import gregtech.api.unification.material.Material;
import gregtech.integration.IntegrationSubmodule;
import org.jetbrains.annotations.NotNull;
import supersymmetry.Supersymmetry;
import supersymmetry.modules.SuSyModules;

@GregTechModule(moduleID = SuSyModules.MODULE_GRS,
        containerID = Supersymmetry.MODID,
        modDependencies = "groovyscript",
        name = "SuSy GroovyScript Integration",
        description = "GroovyScript Integration Module")
public class GrSModule extends IntegrationSubmodule implements GroovyPlugin {

    @NotNull
    @Override
    public String getModId() {
        return Supersymmetry.MODID;
    }

    @NotNull
    @Override
    public String getContainerName() {
        return Supersymmetry.NAME;
    }

    @Override
    public void onCompatLoaded(GroovyContainer<?> container) {
        if (SusConfig.API.recipeInfo) {
            ExpansionHelper.mixinClass(Material.class, SuSyExpansions.class);
            ExpansionHelper.mixinMethod(FluidBuilder.class, SuSyExpansions.class, "basic");
        }
    }
}
