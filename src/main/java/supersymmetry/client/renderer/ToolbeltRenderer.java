package supersymmetry.client.renderer;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.render.item.IItemRenderer;
import codechicken.lib.util.TransformUtils;
import supersymmetry.api.SusyLog;
import supersymmetry.common.item.ItemToolbelt;

/**
 * Draws the selected tool on top of the belt model.
 * <p>
 * Ported from GTCEu-Reloaded 43140b63f (LGPL-3.0), adapted to this project's package layout.
 */
@SideOnly(Side.CLIENT)
public class ToolbeltRenderer implements IItemRenderer {

    private static final Set<String> REPORTED = new HashSet<>();

    private final IBakedModel toolbeltModel;

    public ToolbeltRenderer(IBakedModel toolbeltModel) {
        this.toolbeltModel = toolbeltModel;
    }

    @Override
    public void renderItem(ItemStack stack, ItemCameraTransforms.TransformType transformType) {
        if (!(stack.getItem() instanceof ItemToolbelt toolbelt)) return;

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.5F, 0.5F, 0.5F);

        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
        ItemStack selected = toolbelt.getSelectedTool(stack);
        if (!selected.isEmpty()) {
            IBakedModel selectedModel = renderItem.getItemModelWithOverrides(selected, null, null);
            if (selectedModel == renderItem.getItemModelMesher().getModelManager().getMissingModel()) {
                // Drawing the missing model would paint the magenta cube over the belt; skip it and
                // log once so a stack whose model cannot be resolved can be identified.
                reportMissingModel(selected);
            } else {
                renderItem.renderItem(selected, selectedModel);
            }
        }
        renderItem.renderItem(stack, toolbeltModel);

        GlStateManager.popMatrix();
    }

    private static void reportMissingModel(ItemStack stack) {
        String key = String.valueOf(stack.getItem().getRegistryName()) + '@' + stack.getMetadata();
        if (REPORTED.add(key)) {
            SusyLog.logger.warn("Toolbelt: no baked model for {} (meta {}), tag {}; the belt renders without it",
                    stack.getItem().getRegistryName(), stack.getMetadata(), stack.getTagCompound());
        }
    }

    @Override
    public IModelState getTransforms() {
        return TransformUtils.DEFAULT_TOOL;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }
}
