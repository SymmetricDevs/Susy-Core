package supersymmetry.integration.immersiverailroading.model.part;

import java.util.function.Function;

import cam72cam.immersiverailroading.model.ModelState;
import cam72cam.immersiverailroading.model.components.ComponentProvider;
import cam72cam.immersiverailroading.model.components.ModelComponent;
import cam72cam.mod.math.Vec3d;
import supersymmetry.common.entities.EntityTunnelBore;
import supersymmetry.integration.immersiverailroading.model.ModelHelper;
import util.Matrix4;

public class Borer {

    // Taken from wheel part code
    public static final String BORER_COMPONENT_REGEX = ".*BORER*.";

    protected final ModelComponent borer;

    public Borer(ComponentProvider provider, ModelState state, Function<EntityTunnelBore, Float> angle) {
        this.borer = ModelHelper.parseCustomComponent(provider, BORER_COMPONENT_REGEX);
        Vec3d wheelPos = borer.center;
        state.push(settings -> settings.add((ModelState.Animator) (stock, partialTicks) -> new Matrix4()
                .translate(wheelPos.x, wheelPos.y, wheelPos.z)
                .rotate(Math.toRadians(stock instanceof EntityTunnelBore bore ?
                        angle.apply(bore) : 0),
                        1, 0, 0)
                .translate(-wheelPos.x, -wheelPos.y, -wheelPos.z))).include(borer);
    }
}
