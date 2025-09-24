package supersymmetry.integration.immersiverailroading.model;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import cam72cam.immersiverailroading.library.ModelComponentType;
import cam72cam.immersiverailroading.model.components.ComponentProvider;
import cam72cam.immersiverailroading.model.components.ModelComponent;
import cam72cam.mod.model.obj.OBJModel;
import supersymmetry.api.SusyLog;

public class ModelHelper {

    // Don't feel like asming this
    public static ModelComponent parseCustomComponent(ComponentProvider provider, String regexString) {
        try {
            Method method = ComponentProvider.class.getDeclaredMethod("modelIDs", String.class);

            method.setAccessible(true);

            Set<String> ids = (Set<String>) method.invoke(provider, regexString);

            if (!ids.isEmpty()) {
                Field modelField = ComponentProvider.class.getDeclaredField("model");
                modelField.setAccessible(true);
                OBJModel model = (OBJModel) modelField.get(provider);
                ModelComponent component = new ModelComponent(ModelComponentType.IMMERSIVERAILROADING_BASE_COMPONENT,
                        null, null, model, ids);

                Field componentsField = ComponentProvider.class.getDeclaredField("components");
                componentsField.setAccessible(true);
                List<ModelComponent> components = (List<ModelComponent>) componentsField.get(provider);
                components.add(component);

                return component;
            }
        } catch (NoSuchMethodException e) {
            SusyLog.logger.error("Failed to reflect component provider", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            SusyLog.logger.error("Failed to reflect component provider", e);
        }

        return null;
    }
}
