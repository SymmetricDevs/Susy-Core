package supersymmetry.loaders;

import cam72cam.immersiverailroading.ImmersiveRailroading;
import cam72cam.immersiverailroading.registry.DefinitionManager;
import cam72cam.immersiverailroading.registry.EntityRollingStockDefinition;
import cam72cam.mod.ModCore;
import cam72cam.mod.entity.EntityRegistry;
import com.google.gson.JsonObject;
import supersymmetry.api.SusyLog;
import supersymmetry.common.entities.EntityTunnelBore;
import supersymmetry.integration.immersiverailroading.registry.TunnelBoreDefinition;

import java.lang.reflect.Field;
import java.util.Map;

public class SuSyIRLoader {
    public static void init() {
        try {
            Field jsonLoadersField = DefinitionManager.class.getDeclaredField("jsonLoaders");
            jsonLoadersField.setAccessible(true);
            Map<String, JsonLoader> jsonLoaders = (Map<String, JsonLoader>) jsonLoadersField.get(DefinitionManager.class);
            jsonLoaders.put("tunnel_bore", TunnelBoreDefinition::new);
        } catch (NoSuchFieldException e) {
            SusyLog.logger.error("Failed to reflect definition manager json loaders", e);
        } catch (IllegalAccessException e) {
            SusyLog.logger.error("Failed to access definition manager json loaders", e);
        }

        try {
            Field instanceField = ImmersiveRailroading.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            ModCore.Mod instance = (ModCore.Mod) instanceField.get(ImmersiveRailroading.class);
            EntityRegistry.register(instance, EntityTunnelBore::new, 512);
        } catch (NoSuchFieldException e) {
            SusyLog.logger.error("Failed to reflect immersive railroading instance", e);
        } catch (IllegalAccessException e) {
            SusyLog.logger.error("Failed to access immersive railroading instance", e);
        }
    }

    @FunctionalInterface
    private interface JsonLoader {
        EntityRollingStockDefinition apply(String var1, JsonObject var2) throws Exception;
    }
}
