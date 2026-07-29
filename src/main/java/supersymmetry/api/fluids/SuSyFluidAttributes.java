package supersymmetry.api.fluids;

import static supersymmetry.api.util.SuSyUtility.susyId;

import net.minecraft.client.resources.I18n;

import gregtech.api.fluids.attribute.FluidAttribute;

public class SuSyFluidAttributes {

    public static final FluidAttribute BASE = new FluidAttribute(susyId("base"),
            list -> list.add(I18n.format("susy.fluid.type_base.tooltip")),
            list -> list.add(I18n.format("susy.fluid_pipe.base_proof")));
}
