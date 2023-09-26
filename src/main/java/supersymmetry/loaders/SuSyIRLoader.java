package supersymmetry.loaders;

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

import java.lang.reflect.Field;
import java.util.Map;

public class SuSyIRLoader {
    public static void initDefinitions() {
        try {
            Field jsonLoadersField = DefinitionManager.class.getDeclaredField("stockLoaders");
            jsonLoadersField.setAccessible(true);
            Map<String, StockLoader> stockLoaders = (Map<String, StockLoader>) jsonLoadersField.get(DefinitionManager.class);
            stockLoaders.put("tunnel_bore", TunnelBoreDefinition::new);
        } catch (NoSuchFieldException e) {
            SusyLog.logger.error("Failed to reflect definition manager json loaders", e);
        } catch (IllegalAccessException e) {
            SusyLog.logger.error("Failed to access definition manager json loaders", e);
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
                    renderer.render(entity, state, partialTicks);
                }

            }

            public void postRender(EntityMoveableRollingStock entity, RenderState state, float partialTicks) {
                StockModel<?, ?> renderer = entity.getDefinition().getModel();
                if (renderer != null) {
                    renderer.postRender(entity, state, partialTicks);
                }

            }
        };
        EntityRenderer.register(EntityTunnelBore.class, stockRender);
    }

    @FunctionalInterface
    private interface StockLoader {
        EntityRollingStockDefinition apply(String var1, DataBlock var2) throws Exception;
    }
}
