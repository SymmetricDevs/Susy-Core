package supersymmetry.integration.immersiverailroading.model.part;

import cam72cam.immersiverailroading.model.ModelState;
import cam72cam.immersiverailroading.model.components.ComponentProvider;
import cam72cam.immersiverailroading.model.components.ModelComponent;
import supersymmetry.common.entities.EntityTransporterErector;
import supersymmetry.integration.immersiverailroading.model.ModelHelper;

import java.util.function.Function;


public class Rocket {

    public static final String ROCKET_COMPONENT_REGEX = ".*ROCKET*.";
    protected final ModelComponent rocket;

    public Rocket(ComponentProvider provider, ModelState state, Function<EntityTransporterErector, Boolean> renderRocket) {
        this.rocket = ModelHelper.parseCustomComponent(provider, ROCKET_COMPONENT_REGEX);
        state.push(settings -> settings.add((ModelState.GroupVisibility) (stock, string) ->
                rocket.modelIDs.contains(string) ?
                        stock instanceof EntityTransporterErector transporterErector && renderRocket.apply(transporterErector)
                        : null)
        ).include(rocket);
    }
}
