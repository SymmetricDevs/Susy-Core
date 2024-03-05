package supersymmetry.integration.immersiverailroading.model.part;

import cam72cam.immersiverailroading.model.ComponentRenderer;
import cam72cam.immersiverailroading.model.components.ComponentProvider;
import cam72cam.immersiverailroading.model.components.ModelComponent;
import cam72cam.mod.math.Vec3d;
import org.lwjgl.opengl.GL11;
import supersymmetry.integration.immersiverailroading.model.ModelHelper;

public class Borer {

    // Taken from wheel part code
    public static final String BORER_COMPONENT_REGEX = ".*BORER*.";

    protected final ModelComponent borer;

    public Borer(ComponentProvider provider) {
        this.borer = ModelHelper.parseCustomComponent(provider, BORER_COMPONENT_REGEX);
    }

    public void render(float angle, ComponentRenderer draw) {
        Vec3d borerPos = this.borer.center;
        ComponentRenderer matrix = draw.push();
        Throwable var5 = null;

        try {
            GL11.glTranslated(borerPos.x, borerPos.y, borerPos.z);
            GL11.glRotated(angle, 1.0, 0.0, 0.0);
            GL11.glTranslated(-borerPos.x, -borerPos.y, -borerPos.z);
            matrix.render(this.borer);
        } catch (Throwable var14) {
            var5 = var14;
            throw var14;
        } finally {
            if (matrix != null) {
                if (var5 != null) {
                    try {
                        matrix.close();
                    } catch (Throwable var13) {
                        var5.addSuppressed(var13);
                    }
                } else {
                    matrix.close();
                }
            }
        }
    }
}
