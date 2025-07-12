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

    static {
        AnimationController.addModelFetcher(object -> {
            if (object instanceof TileEntity tile) {
                @SuppressWarnings("rawtypes") TileEntitySpecialRenderer renderer = TileEntityRendererDispatcher.instance.getRenderer(tile);
                if (renderer instanceof FixedGeoBlockRenderer) {
                    //noinspection unchecked,rawtypes
                    return ((FixedGeoBlockRenderer) renderer).getGeoModelProvider();
                }
            }

            return null;
        });
    }

    public static void setupLight(int light) {
        int lx = light % 65536;
        int ly = light / 65536;
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        OpenGlHelper.setLightmapTextureCoords(3553, (float) lx, (float) ly);
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    public static void rotateBlock(EnumFacing facing) {
        switch (facing) {
            case SOUTH -> GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
            case WEST -> GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
            case EAST -> GlStateManager.rotate(270.0F, 0.0F, 1.0F, 0.0F);
            case UP -> GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            case DOWN -> GlStateManager.rotate(90.0F, -1.0F, 0.0F, 0.0F);
            default -> {
                // No rotation needed for north
            }
        }
    }

    private EnumFacing getFacing(T tile) {
        IBlockState blockState = tile.getWorld().getBlockState(tile.getPos());
        if (blockState.getPropertyKeys().contains(BlockHorizontal.FACING)) {
            return blockState.getValue(BlockHorizontal.FACING);
        } else {
            return blockState.getPropertyKeys().contains(BlockDirectional.FACING)
                    ? blockState.getValue(BlockDirectional.FACING)
                    : EnumFacing.NORTH;
        }
    }

    @SuppressWarnings("DuplicatedCode")
    public void render(T tile, double x, double y, double z, float partialTicks, int destroyStage) {
        GeoModel model = this.modelProvider.getModel(this.modelProvider.getModelLocation(tile));
        this.modelProvider.setLivingAnimations(tile, this.getUniqueID(tile));
        setupLight(tile.getWorld().getCombinedLight(tile.getPos(), 0));
        GlStateManager.pushMatrix();
        {
            GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
            rotateBlock(this.getFacing(tile));
            Minecraft.getMinecraft().getTextureManager().bindTexture(this.getTextureLocation(tile));
            Color renderColor = this.getRenderColor(tile, partialTicks);
            this.render(
                    model,
                    tile,
                    partialTicks,
                    (float) renderColor.getRed() / 255.0F,
                    (float) renderColor.getGreen() / 255.0F,
                    (float) renderColor.getBlue() / 255.0F,
                    (float) renderColor.getAlpha() / 255.0F);
        }
        GlStateManager.popMatrix();
    }

    @Override
    public AnimatedGeoModel<T> getGeoModelProvider() {
        return this.modelProvider;
    }

    @Override
    public ResourceLocation getTextureLocation(T instance) {
        return this.modelProvider.getTextureLocation(instance);
    }
}
