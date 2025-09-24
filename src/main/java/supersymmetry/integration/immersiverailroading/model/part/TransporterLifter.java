package supersymmetry.integration.immersiverailroading.model.part;

import java.util.function.Function;

import cam72cam.immersiverailroading.model.ModelState;
import cam72cam.immersiverailroading.model.components.ComponentProvider;
import cam72cam.immersiverailroading.model.components.ModelComponent;
import supersymmetry.common.entities.EntityTransporterErector;
import supersymmetry.integration.immersiverailroading.model.ModelHelper;
import util.Matrix4;

public class TransporterLifter {

    public static final String LIFTER_COMPONENT_REGEX = ".*LIFTER*.";

    protected final ModelComponent lifter;

    public TransporterLifter(ComponentProvider provider, ModelState state,
                             Function<EntityTransporterErector, Float> lifterAngle) {
        this.lifter = ModelHelper.parseCustomComponent(provider, LIFTER_COMPONENT_REGEX);
        state.push(settings -> settings.add((ModelState.Animator) (stock, partialTicks) -> new Matrix4()
                .translate(-7, 1.1, 0)
                .rotate(stock instanceof EntityTransporterErector transporterErector ?
                        lifterAngle.apply(transporterErector) : 0,
                        0, 0, 1)
                .translate(+7, -1.1, 0))).include(lifter);
    }
}
