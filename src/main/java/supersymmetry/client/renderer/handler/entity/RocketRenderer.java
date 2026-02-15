package supersymmetry.client.renderer.handler.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import supersymmetry.SuSyValues;
import supersymmetry.common.entities.EntityRocket;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.List;

@SideOnly(Side.CLIENT)
public class RocketRenderer<T extends EntityRocket> extends Render<T> {

    private ModelManager manager = null;

    public RocketRenderer(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks) {
        IBakedModel model;

        // Get modelManager from Minecraft.
        // Don't forget to save it to a field or somewhere because reflection is a slow task.
        // No it's fast enough to do it once
        if (this.manager == null) {
            try {
                Field mm_f = ObfuscationReflectionHelper.findField(Minecraft.class, "field_175617_aL");
                this.manager = (ModelManager) mm_f.get(Minecraft.getMinecraft());
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        // This shouldn't occur but better safe than sorry
        if (this.manager == null) {
            System.out.println("manager is null");
            return;
        }

        GlStateManager.pushMatrix(); // pushMatrix because we'll translate and rotate stuff
        GlStateManager.disableCull();
        // GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.translate((float) x, (float) y, (float) z); // You shouldn't forget to translate to x, y, z
                                                                   // before rendering. Other specific are made so
                                                                   // Rubik's cube renders at the middle.

        // Rotate the rocket based on its yaw and pitch (for troll mode and other flight dynamics)
        GlStateManager.rotate(-entity.rotationYaw, 0.0F, 1.0F, 0.0F); // Rotate around Y axis for yaw
        GlStateManager.rotate(entity.rotationPitch, 1.0F, 0.0F, 0.0F); // Rotate around X axis for pitch

        GlStateManager.scale(1F, 1F, 1F); // Yep models are too ginormous for buffer builder.
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        if (this.renderOutlines) { // Render outline thingy, mostly useless.
            GlStateManager.enableColorMaterial();
            GlStateManager.enableOutlineMode(this.getTeamColor(entity));
        }

        model = this.manager.getModel(SuSyValues.modelRocket); // Get the model
        this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE); // Bind the blocks texture. See
                                                              // Test#stitchTexture(TextureStitchEvent.Pre) for more
                                                              // information.

        bufferbuilder.begin(7, DefaultVertexFormats.ITEM); // I guess DefaultVertexFormats#ITEM would work too. Needs
                                                           // some test.

        // Gets model quads for rendering
        // State can be null. Because it checks if state is an instance of IExtendedState which it uses it to get
        // unlisted properties for some data. We don't use it and need it.
        // Side needs to be null. Obj models don't care about sides.
        // Rand doesn't get used in Obj models.
        List<BakedQuad> quads = model.getQuads(null, null, 0L);
        quads.forEach(q -> bufferbuilder.addVertexData(q.getVertexData())); // Add quad data to buffer builder
        tessellator.draw(); // Then draw it

        GlStateManager.enableCull();
        GlStateManager.popMatrix();
    }

    // We don't use this.
    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(T entity) {
        return null;
    }
}
