package supersymmetry.integration.immersiverailroading.model.part;

import java.util.function.Function;

import cam72cam.immersiverailroading.model.ModelState;
import cam72cam.immersiverailroading.model.components.ComponentProvider;
import cam72cam.immersiverailroading.model.components.ModelComponent;
import supersymmetry.common.entities.EntityTransporterErector;
import supersymmetry.integration.immersiverailroading.model.ModelHelper;
import util.Matrix4;

public class Rocket {

    public static final String ROCKET_COMPONENT_REGEX = ".*ROCKET.*";
    protected final ModelComponent rocket;

    public Rocket(ComponentProvider provider, ModelState state,
                  Function<EntityTransporterErector, Boolean> renderRocket,
                  Function<EntityTransporterErector, Float> lifterAngle) {
        this.rocket = ModelHelper.parseCustomComponent(provider, ROCKET_COMPONENT_REGEX);
        state.push(settings -> settings
                .add((ModelState.GroupVisibility) (stock, string) -> {
                    if (rocket.modelIDs.contains(string)) {
                        if (stock instanceof EntityTransporterErector transporterErector) {
                            return renderRocket.apply(transporterErector); // true = visible, false = hidden
                        }
                        return false; // Hide if not the right entity type
                    }
                    return null; // Not a rocket component, let other handlers decide
                })
                .add((ModelState.Animator) (stock, partialTicks) -> new Matrix4()
                        .translate(-7, 1.1, 0)
                        .rotate(stock instanceof EntityTransporterErector transporterErector ?
                                lifterAngle.apply(transporterErector) : 0,
                                0, 0, 1)
                        .translate(+7, -1.1, 0)))
                .include(rocket);
    }
}
