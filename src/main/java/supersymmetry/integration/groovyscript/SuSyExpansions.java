package supersymmetry.integration.groovyscript;

import com.cleanroommc.groovyscript.api.GroovyLog;
import gregtech.api.GregTechAPI;
import gregtech.api.fluids.FluidBuilder;
import gregtech.api.fluids.store.FluidStorageKey;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.FluidProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.integration.groovy.MaterialPropertyExpansion;
import supersymmetry.api.fluids.SuSyFluidAttributes;
import supersymmetry.api.fluids.SusyFluidStorageKeys;
import supersymmetry.api.unification.material.properties.MillBallProperty;
import supersymmetry.api.unification.material.properties.SuSyPropertyKey;

import static gregtech.api.fluids.FluidConstants.ROOM_TEMPERATURE;

public class SuSyExpansions {

    public static void addMillBall(Material m, int durability) {
        if (checkFrozen("add mill ball property to a material")) return;
        m.setProperty(SuSyPropertyKey.MILL_BALL, new MillBallProperty(durability));
    }

    public static void setBaseProof(Material m, boolean baseProof) {
        if (checkFrozen("set a material as base-proof")) return;
        if (!m.hasProperty(PropertyKey.FLUID_PIPE)) {
            GroovyLog.get().error("Material {} does not have an FluidPipeProperty!", m);
            return;
        }

        m.getProperty(PropertyKey.FLUID_PIPE).setCanContain(SuSyFluidAttributes.BASE, baseProof);
    }

    public static void setupSlurries(Material m) {
        setupFluidTypes(m, SusyFluidStorageKeys.SLURRY, SusyFluidStorageKeys.IMPURE_SLURRY);
    }

    public static void setupFluidTypes(Material m, FluidStorageKey... keys) {
        setupFluidTypes(m, ROOM_TEMPERATURE, keys);
    }

    public static void setupFluidTypes(Material m, int temp, FluidStorageKey... keys) {
        if (checkFrozen("add fluid types to a material")) return;
        boolean hasFluidProperty =  m.hasProperty(PropertyKey.FLUID);
        var property = hasFluidProperty  ? m.getProperty(PropertyKey.FLUID)  : new FluidProperty();
        for (var key: keys) {
            property.enqueueRegistration(key, new FluidBuilder().temperature(temp));
        }
        if (!hasFluidProperty) m.setProperty(PropertyKey.FLUID, property);
    }

    public static void setOreByProducts(Material m, Material... byproducts) {
        if (checkFrozen("set ore pyproducts of a material")) return;
        if (!m.hasProperty(PropertyKey.ORE)) {
            GroovyLog.get().error("Material {} does not have an OreProperty!", m);
            return;
        }
        m.getProperty(PropertyKey.ORE).setOreByProducts(byproducts);
    }

    public static void addFluidPipes(Material m, int maxFluidTemperature, int throughput, boolean gasProof,
                                     boolean acidProof, boolean cryoProof, boolean plasmaProof, boolean baseProof) {
        if (checkFrozen("add fluid pipes to a material")) return;
        MaterialPropertyExpansion.addFluidPipes(m, maxFluidTemperature, throughput, gasProof, acidProof, cryoProof, plasmaProof);
        m.getProperty(PropertyKey.FLUID_PIPE).setCanContain(SuSyFluidAttributes.BASE, baseProof);
    }


    protected static boolean checkFrozen(String description) {
        if (!GregTechAPI.materialManager.canModifyMaterials()) {
            GroovyLog.get().error("Cannot {} now, must be done in preInit loadStage and material event", description);
            return true;
        }
        return false;
    }
}
