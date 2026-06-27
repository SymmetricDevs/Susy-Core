package supersymmetry.integration.immersiverailroading.model.part;

import cam72cam.immersiverailroading.model.ModelState;
import cam72cam.immersiverailroading.model.components.ComponentProvider;
import cam72cam.immersiverailroading.model.components.ModelComponent;
import supersymmetry.integration.immersiverailroading.model.ModelHelper;

public class Rocket {

    public static final String ROCKET_COMPONENT_REGEX = ".*ROCKET.*";
    protected final ModelComponent rocket;

    public Rocket(ComponentProvider provider, ModelState state) {
        this.rocket = ModelHelper.parseCustomComponent(provider, ROCKET_COMPONENT_REGEX);
        // The rocket is never drawn through the base pipeline: it can only ever be shown
        // whole, and we need a per-face partial sweep instead. TransporterErectorModel#postRender
        // draws a length-sorted prefix of the rocket geometry itself, so here we simply hide the
        // rocket groups from the standard group draw.
        state.push(settings -> settings
                .add((ModelState.GroupVisibility) (stock, string) ->
                        rocket != null && rocket.modelIDs.contains(string) ? Boolean.FALSE : null))
                .include(rocket);
    }

    public ModelComponent getComponent() {
        return rocket;
    }
}
