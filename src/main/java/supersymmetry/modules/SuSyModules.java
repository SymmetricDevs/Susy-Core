package supersymmetry.modules;

import gregtech.api.modules.IModuleContainer;
import gregtech.api.modules.ModuleContainer;
import supersymmetry.Supersymmetry;

@ModuleContainer
public class SuSyModules implements IModuleContainer {

    public static final String MODULE_CORE = "susy_core";
    public static final String MODULE_BDSAndM = "bdsandm_integration";
    public static final String MODULE_BAUBLES = "baubles_integration";
    public static final String MODULE_TOP = "susy_top_integration";
    public static final String MODULE_JEI = "susy_jei_integration";

    @Override
    public String getID() {
        return Supersymmetry.MODID;
    }
}
