package supersymmetry.loaders;

import java.lang.reflect.Field;
import java.util.Map;

import cam72cam.immersiverailroading.ImmersiveRailroading;
import cam72cam.immersiverailroading.entity.EntityMoveableRollingStock;
import cam72cam.immersiverailroading.model.StockModel;
import cam72cam.immersiverailroading.registry.DefinitionManager;
import cam72cam.immersiverailroading.registry.EntityRollingStockDefinition;
import cam72cam.immersiverailroading.util.DataBlock;
import cam72cam.mod.ModCore;
import cam72cam.mod.entity.EntityRegistry;
import cam72cam.mod.render.EntityRenderer;
import cam72cam.mod.render.IEntityRender;
import cam72cam.mod.render.opengl.RenderState;
import supersymmetry.api.SusyLog;
import supersymmetry.common.entities.EntityTunnelBore;
import supersymmetry.integration.immersiverailroading.registry.TunnelBoreDefinition;

public class SuSyIRLoader {

    public static void initDefinitions() {
        // Left as a warning to all future generations -- MTBO
        // Actually you didn't do anything wrong, you just need to put null as input
        // when you're getting object from field because it's a static field -- Surreal
        /*
         * try {
         * Field jsonLoadersField = DefinitionManager.class.getDeclaredField("stockLoaders");
         * jsonLoadersField.setAccessible(true);
         * ↓ this should be null
         * Map<String, StockLoader> stockLoaders = (Map<String, StockLoader>)
         * jsonLoadersField.get(DefinitionManager.class);
         * stockLoaders.put("tunnel_bore", TunnelBoreDefinition::new);
         * } catch (NoSuchFieldException e) {
         * SusyLog.logger.error("Failed to reflect definition manager json loaders", e);
         * } catch (IllegalAccessException e) {
         * SusyLog.logger.error("Failed to access definition manager json loaders", e);
         * }
         */
        /*
         * try {
         * Class<?> definitionManagerClass = Class.forName("cam72cam.immersiverailroading.registry.DefinitionManager");
         * Method addStockLoaderMethod = definitionManagerClass.getMethod("addStockLoader", String.class,
         * StockLoader.class);
         * String loaderName = "tunnel_bore";
         * StockLoader loader = TunnelBoreDefinition::new;
         * try {
         * addStockLoaderMethod.invoke(null, loaderName, loader);
         * } catch (java.lang.reflect.InvocationTargetException e) {
         * // Print the actual exception that occurred within addStockLoader method
         * Throwable actualException = e.getTargetException();
         * actualException.printStackTrace();
         * }
         * }
         * catch (Exception e) {
         * e.printStackTrace();
         * }
         */

        try {
            Field jsonLoadersField = DefinitionManager.class.getDeclaredField("stockLoaders");
            jsonLoadersField.setAccessible(true);

            Map<String, StockLoader> stockLoaders = (Map<String, StockLoader>) jsonLoadersField.get(null);

            stockLoaders.put("tunnel_bore", TunnelBoreDefinition::new);

        } catch (NoSuchFieldException e) {
            SusyLog.logger.error("Failed to reflect definition manager json loaders", e);
        } catch (IllegalAccessException e) {
            SusyLog.logger.error("Failed to instantiate StockLoaderBridge", e);
        }
    }

    public static void initEntities() {
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

    public static void initEntityRenderers() {
        IEntityRender<EntityMoveableRollingStock> stockRender = new IEntityRender<>() {

            public void render(EntityMoveableRollingStock entity, RenderState state, float partialTicks) {
                StockModel<?, ?> renderer = entity.getDefinition().getModel();
                if (renderer != null) {
                    renderer.renderEntity(entity, state, partialTicks);
                }
            }

            public void postRender(EntityMoveableRollingStock entity, RenderState state, float partialTicks) {
                StockModel<?, ?> renderer = entity.getDefinition().getModel();
                if (renderer != null) {
                    renderer.postRenderEntity(entity, state, partialTicks);
                }
            }
        };
        EntityRenderer.register(EntityTunnelBore.class, stockRender);
    }

    @FunctionalInterface
    public interface StockLoader {

        EntityRollingStockDefinition apply(String var1, DataBlock var2) throws Exception;
    }
}
