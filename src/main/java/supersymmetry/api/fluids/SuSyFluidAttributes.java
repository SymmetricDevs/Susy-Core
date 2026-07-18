package supersymmetry.api.fluids;

import gregtech.api.fluids.attribute.FluidAttribute;
import net.minecraft.client.resources.I18n;

import static supersymmetry.api.util.SuSyUtility.susyId;

public class SuSyFluidAttributes {
    public static final FluidAttribute BASE = new FluidAttribute(susyId("base"),
            list -> list.add(I18n.format("susy.fluid.type.base.tooltip")),
            list -> list.add(I18n.format("susy.fluid_pipe.base_proof"))
    );
}
