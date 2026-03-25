package supersymmetry.client.renderer.handler;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

import codechicken.lib.texture.TextureUtils;
import codechicken.lib.util.TransformUtils;
import supersymmetry.api.SusyLog;

/**
 * A wrapped OBJ IBakedModel
 * Mostly copied from Draconic Evolution, with minor modifications.
 *
 * @author brandon3055
 *         <a href="https://github.com/Draconic-Inc/Draconic-Evolution">Link</a>
 */
@SideOnly(Side.CLIENT)
public class OBJModelRender extends ModelRenderer {

    public float scale = 0;
    private int displayList;
    private boolean compiled = false;
    private IBakedModel objModel;
    private float scaleMultiplier = 1F;

    public OBJModelRender(ModelBase baseModel, ResourceLocation customModel, float scaleMultiplier) {
        this(baseModel, customModel);
        this.scaleMultiplier = scaleMultiplier;
    }

    public OBJModelRender(ModelBase baseModel, ResourceLocation customModel) {
        super(baseModel);

        try {
            objModel = OBJLoader.INSTANCE.loadModel(customModel).bake(TransformUtils.DEFAULT_TOOL,
                    DefaultVertexFormats.ITEM, TextureUtils::getTexture);
        } catch (Exception e) {
            SusyLog.logger.error(e);
        }
    }

    public static void renderQuads(List<BakedQuad> listQuads) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        int i = 0;
        vertexbuffer.begin(7, DefaultVertexFormats.ITEM);
        for (int j = listQuads.size(); i < j; ++i) {
            BakedQuad bakedquad = listQuads.get(i);

            vertexbuffer.addVertexData(bakedquad.getVertexData());

            vertexbuffer.putColorRGB_F4(1, 1, 1);

            Vec3i vec3i = bakedquad.getFace().getDirectionVec();
            vertexbuffer.putNormal((float) vec3i.getX(), (float) vec3i.getY(), (float) vec3i.getZ());

        }
        tessellator.draw();
    }

    @Override
    public void render(float scale) {
        if (!this.isHidden && this.showModel) {
            if (!this.compiled) {
                this.compileDisplayList(scale);
            }

            GlStateManager.translate(this.offsetX, this.offsetY, this.offsetZ);

            if (this.rotateAngleX == 0.0F && this.rotateAngleY == 0.0F && this.rotateAngleZ == 0.0F) {
                if (this.rotationPointX == 0.0F && this.rotationPointY == 0.0F && this.rotationPointZ == 0.0F) {
                    GlStateManager.callList(this.displayList);
                } else {
                    GlStateManager.translate(this.rotationPointX * scale, this.rotationPointY * scale,
                            this.rotationPointZ * scale);
                    GlStateManager.callList(this.displayList);
                    GlStateManager.translate(-this.rotationPointX * scale, -this.rotationPointY * scale,
                            -this.rotationPointZ * scale);
                }
            } else {
                GlStateManager.pushMatrix();
                GlStateManager.translate(this.rotationPointX * scale, this.rotationPointY * scale,
                        this.rotationPointZ * scale);
                if (this.rotateAngleZ != 0.0F) {
                    GlStateManager.rotate(this.rotateAngleZ * 57.295776F, 0.0F, 0.0F, 1.0F);
                }

                if (this.rotateAngleY != 0.0F) {
                    GlStateManager.rotate(this.rotateAngleY * 57.295776F, 0.0F, 1.0F, 0.0F);
                }

                if (this.rotateAngleX != 0.0F) {
                    GlStateManager.rotate(this.rotateAngleX * 57.295776F, 1.0F, 0.0F, 0.0F);
                }

                GlStateManager.callList(this.displayList);
                GlStateManager.popMatrix();
            }

            GlStateManager.translate(-this.offsetX, -this.offsetY, -this.offsetZ);
        }
    }

    private void compileDisplayList(float scale) {
        if (this.scale == 0) {
            this.scale = scale * scaleMultiplier;
        }

        if (objModel == null) {
            compiled = true;
            SusyLog.logger.error("Failed to compile OBJ model!");
            return;
        }

        scale = this.scale;
        this.displayList = GLAllocation.generateDisplayLists(1);
        GlStateManager.glNewList(this.displayList, GL11.GL_COMPILE);

        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.rotate(180, -1, 0, 1);

        GlStateManager.bindTexture(Minecraft.getMinecraft().getTextureMapBlocks().getGlTextureId());
        renderQuads(objModel.getQuads(null, null, 0));

        GlStateManager.popMatrix();

        GlStateManager.glEndList();
        this.compiled = true;
    }

    @Override
    public void renderWithRotation(float scale) {
        if (!this.isHidden && this.showModel) {
            if (!this.compiled) {
                this.compileDisplayList(scale);
            }

            GlStateManager.pushMatrix();
            GlStateManager.translate(this.rotationPointX * scale, this.rotationPointY * scale,
                    this.rotationPointZ * scale);
            if (this.rotateAngleY != 0.0F) {
                GlStateManager.rotate(this.rotateAngleY * 57.295776F, 0.0F, 1.0F, 0.0F);
            }

            if (this.rotateAngleX != 0.0F) {
                GlStateManager.rotate(this.rotateAngleX * 57.295776F, 1.0F, 0.0F, 0.0F);
            }

            if (this.rotateAngleZ != 0.0F) {
                GlStateManager.rotate(this.rotateAngleZ * 57.295776F, 0.0F, 0.0F, 1.0F);
            }

            GlStateManager.callList(this.displayList);
            GlStateManager.popMatrix();
        }
    }

    @Override
    public void postRender(float scale) {
        if (!this.isHidden && this.showModel) {

            if (this.rotateAngleX == 0.0F && this.rotateAngleY == 0.0F && this.rotateAngleZ == 0.0F) {
                if (this.rotationPointX != 0.0F || this.rotationPointY != 0.0F || this.rotationPointZ != 0.0F) {
                    GlStateManager.translate(this.rotationPointX * scale, this.rotationPointY * scale,
                            this.rotationPointZ * scale);
                }
            } else {
                GlStateManager.translate(this.rotationPointX * scale, this.rotationPointY * scale,
                        this.rotationPointZ * scale);
                if (this.rotateAngleZ != 0.0F) {
                    GlStateManager.rotate(this.rotateAngleZ * 57.295776F, 0.0F, 0.0F, 1.0F);
                }

                if (this.rotateAngleY != 0.0F) {
                    GlStateManager.rotate(this.rotateAngleY * 57.295776F, 0.0F, 1.0F, 0.0F);
                }

                if (this.rotateAngleX != 0.0F) {
                    GlStateManager.rotate(this.rotateAngleX * 57.295776F, 1.0F, 0.0F, 0.0F);
                }
            }
        }
    }
}
