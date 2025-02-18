package supersymmetry.client.renderer.handler;

import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.util.Color;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

public abstract class FixedGeoBlockRenderer<T extends TileEntity & IAnimatable> extends TileEntitySpecialRenderer<T> implements IGeoRenderer<T> {

    private final AnimatedGeoModel<T> modelProvider;

    public FixedGeoBlockRenderer(AnimatedGeoModel<T> modelProvider) {
        this.modelProvider = modelProvider;
    }

    @Override
    public void render(@NotNull T te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        this.render(te, x, y, z, partialTicks, destroyStage);
    }

    public void render(T tile, double x, double y, double z, float partialTicks, int destroyStage) {
        GeoModel model = this.modelProvider.getModel(this.modelProvider.getModelLocation(tile));
        this.modelProvider.setLivingAnimations(tile, this.getUniqueID(tile));
        int light = tile.getWorld().getCombinedLight(tile.getPos(), 0);
        int lx = light % 65536;
        int ly = light / 65536;
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        OpenGlHelper.setLightmapTextureCoords(3553, (float) lx, (float) ly);
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.translate(0.5, 0.5, 0.5);
        this.rotateBlock(this.getFacing(tile));
        Minecraft.getMinecraft().renderEngine.bindTexture(this.getTextureLocation(tile));
        Color renderColor = this.getRenderColor(tile, partialTicks);
        this.render(model, tile, partialTicks, (float) renderColor.getRed() / 255.0F, (float) renderColor.getGreen() / 255.0F, (float) renderColor.getBlue() / 255.0F, (float) renderColor.getAlpha() / 255.0F);
        GlStateManager.popMatrix();
    }

    public AnimatedGeoModel<T> getGeoModelProvider() {
        return this.modelProvider;
    }

    protected void rotateBlock(EnumFacing facing) {
        switch (facing) {
            case SOUTH:
                GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                break;
            case WEST:
                GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
            case NORTH:
            default:
                break;
            case EAST:
                GlStateManager.rotate(270.0F, 0.0F, 1.0F, 0.0F);
                break;
            case UP:
                GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                break;
            case DOWN:
                GlStateManager.rotate(90.0F, -1.0F, 0.0F, 0.0F);
        }

    }

    private EnumFacing getFacing(T tile) {
        IBlockState blockState = tile.getWorld().getBlockState(tile.getPos());
        if (blockState.getPropertyKeys().contains(BlockHorizontal.FACING)) {
            return blockState.getValue(BlockHorizontal.FACING);
        } else {
            return blockState.getPropertyKeys().contains(BlockDirectional.FACING) ? blockState.getValue(BlockDirectional.FACING) : EnumFacing.NORTH;
        }
    }

    public ResourceLocation getTextureLocation(T instance) {
        return this.modelProvider.getTextureLocation(instance);
    }

    static {
        AnimationController.addModelFetcher((object) -> {
            if (object instanceof TileEntity tile) {
                @SuppressWarnings("rawtypes") TileEntitySpecialRenderer renderer = TileEntityRendererDispatcher.instance.getRenderer(tile);
                if (renderer instanceof FixedGeoBlockRenderer) {
                    return ((FixedGeoBlockRenderer) renderer).getGeoModelProvider();
                }
            }

            return null;
        });
    }
}
