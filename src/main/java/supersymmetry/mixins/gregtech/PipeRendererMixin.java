package supersymmetry.mixins.gregtech;

import codechicken.lib.render.BlockRenderer;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Translation;
import codechicken.lib.vec.uv.IconTransformation;
import codechicken.lib.vec.uv.UV;
import codechicken.lib.vec.uv.UVTransformation;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.unification.material.Material;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.pipe.PipeRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import supersymmetry.api.blocks.IForcedStates;
import supersymmetry.api.unification.material.info.SuSyMaterialIconType;
import supersymmetry.common.blocks.BlockSheetedFrame;

@Mixin(value = PipeRenderer.class, remap = false)
public abstract class PipeRendererMixin {

    @Shadow
    @Final @NotNull
    protected static ThreadLocal<BlockRenderer.BlockFace> blockFaces;

    @Inject(method = "renderFrame(Lgregtech/api/pipenet/tile/IPipeTile;Lnet/minecraft/util/math/BlockPos;Lcodechicken/lib/render/CCRenderState;I)V",
            at = @At("HEAD"), cancellable = true)
    private static void renderFrame(IPipeTile<?, ?> pipeTile, BlockPos pos, CCRenderState renderState, int connections, CallbackInfo callbackInfo) {
        // assumes pipeTile is not null
        Material frameMaterial = pipeTile.getFrameMaterial();
        if (frameMaterial == null || ((IForcedStates) pipeTile).getForcedState() == 0) return;

        int rotationOrdinal = ((IForcedStates) pipeTile).getForcedState() - 1;
        EnumFacing.Axis axis = BlockSheetedFrame.FrameEnumAxis.values()[rotationOrdinal].getAxis();

        ResourceLocation rl = axis == null ? SuSyMaterialIconType.sheetedFrameAll.getBlockTexturePath(frameMaterial.getMaterialIconSet()) :
                SuSyMaterialIconType.sheetedFrame.getBlockTexturePath(frameMaterial.getMaterialIconSet());

        // if an array initializer is used for pipeline, modifying elements causes color to disappear
        final TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(rl.toString());

        // rotate standing model over towards positive direction for Z and negative for X
        final boolean isX = EnumFacing.Axis.X.equals(axis);
        final boolean isZ = EnumFacing.Axis.Z.equals(axis);
        final boolean isHorizontal = isX || isZ;

        final boolean isY = EnumFacing.Axis.Y.equals(axis);
        final boolean isAll = axis == null;

        final UVTransformation rotCW = new UVTransformation(){
            @Override
            public void apply(UV uv) {
                // translate center to middle of texture
                uv.u -= 0.5;
                uv.v -= 0.5;

                // do rotation with matrix [[cos-90, -sin-90],[sin-90, cos-90]] -> [[0, 1],[-1, 0]]; <u, v> * A = <v, -u>
                double tempStorage = uv.u;
                uv.u = uv.v;
                uv.v = -tempStorage;

                // undo translation
                uv.u += 0.5;
                uv.v += 0.5;

                uv.u = sprite.getInterpolatedU(uv.u * 16.0);
                uv.v = sprite.getInterpolatedV(uv.v * 16.0);
            }

            // coordinate swap undoes itself
            @Override
            public UVTransformation inverse() {
                return this;
            }
        };

        final UVTransformation rotCCW = new UVTransformation() {
            @Override
            public void apply(UV uv) {
                // translate center to middle of texture
                uv.u -= 0.5;
                uv.v -= 0.5;

                // do rotation with matrix [[cos90, -sin90],[sin90, cos90]] -> [[0, -1],[1, 0]]; <u, v> * A = <-v, u>
                double tempStorage = uv.u;
                uv.u = -uv.v;
                uv.v = tempStorage;

                // undo translation
                uv.u += 0.5;
                uv.v += 0.5;

                uv.u = sprite.getInterpolatedU(uv.u * 16.0);
                uv.v = sprite.getInterpolatedV(uv.v * 16.0);
            }

            @Override
            public UVTransformation inverse() {
                return this;
            }
        };

        IVertexOperation[] pipeline = {
                new Translation(pos),
                renderState.lightMatrix,
                new IconTransformation(sprite),
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(frameMaterial.getMaterialRGB()))
        };

        IVertexOperation[] pipelineRotateCCW = {
                new Translation(pos),
                renderState.lightMatrix,
                rotCCW,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(frameMaterial.getMaterialRGB()))
        };

        IVertexOperation[] pipelineRotateCW = {
                new Translation(pos),
                renderState.lightMatrix,
                rotCW,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(frameMaterial.getMaterialRGB()))
        };

        // stops z-fighting for pipes on axis
        final Cuboid6 FULL_SHEETED_CUBOID = new Cuboid6(isX || isAll ? 0.00009 : 0, isY || isAll ? 0.00009 : 0,
                isZ || isAll ? 0.00009 : 0, isX || isAll ? 0.99996 : 1, isY || isAll ? 0.99996 : 1, isZ || isAll ? 0.99996 : 1);

        // renders interior
        final Cuboid6 INTERIOR_SHEETED_CUBOID = new Cuboid6(isX || isAll ? 0.9999 : 0.9375, isY || isAll ? 0.9999 : 0.9375,
                isZ || isAll ? 0.9999 : 0.9375, isX || isAll ? 0.0001 : 0.0625, isY || isAll ? 0.0001 : 0.0625, isZ || isAll ? 0.0001 :0.0625);

        EnumFacing[] skippedFacings = new EnumFacing[2];
        int index = 0;

        for (EnumFacing side : EnumFacing.VALUES) {
            EnumFacing.Axis sideAxis = side.getAxis();

            // skips sides "on axis" with sheeted frame
            if (sideAxis.equals(axis)) {
                skippedFacings[index++] = side;
                continue;
            }

            final boolean isSideY = side.getAxis().equals(EnumFacing.Axis.Y);
            final boolean shouldRot = isHorizontal && !(isSideY && !isX);

            // only render frame if it doesn't have a cover
            if ((connections & 1 << (12 + side.getIndex())) == 0) {
                BlockRenderer.BlockFace blockFace = blockFaces.get();
                blockFace.loadCuboidFace(FULL_SHEETED_CUBOID, side.getIndex());
                renderState.setPipeline(blockFace, 0, blockFace.verts.length, shouldRot ?
                        (side.getAxisDirection().getOffset() < 0 ? pipelineRotateCW : pipelineRotateCCW) : pipeline);
                renderState.render();

                // render interior for off axis sides
                blockFace.loadCuboidFace(INTERIOR_SHEETED_CUBOID, side.getIndex());
                renderState.setPipeline(blockFace, 0, blockFace.verts.length, (isX || isZ) && !(isSideY && !isX) ?
                        (side.getAxisDirection().getOffset() > 0 ? pipelineRotateCW : pipelineRotateCCW) : pipeline);
                renderState.render();
            }
        }

        // will be done, but not used, if orientation is none
        // must be included in a model at some point to be included into the texture atlas
        rl = SuSyMaterialIconType.sheetedFrameEnd.getBlockTexturePath(frameMaterial.getMaterialIconSet());
        TextureAtlasSprite endSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(rl.toString());
        //pipeline[3] = new IconTransformation(sprite); this does not work for some reason, the entire thing must be re-initialized
        pipeline = new IVertexOperation[] {
                new Translation(pos),
                renderState.lightMatrix,
                new IconTransformation(endSprite),
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(frameMaterial.getMaterialRGB())),
        };

        for (int i = 0; i < index; ++i) {
            // only render frame if it doesn't have a cover
            if ((connections & 1 << (12 + skippedFacings[i].getIndex())) == 0) {
                BlockRenderer.BlockFace blockFace = blockFaces.get();
                blockFace.loadCuboidFace(FULL_SHEETED_CUBOID, skippedFacings[i].getIndex());
                renderState.setPipeline(blockFace, 0, blockFace.verts.length, pipeline);
                renderState.render();

                // render interior for off axis sides
                blockFace.loadCuboidFace(INTERIOR_SHEETED_CUBOID, skippedFacings[i].getIndex());
                renderState.setPipeline(blockFace, 0, blockFace.verts.length, pipeline);
                renderState.render();
            }
        }

        callbackInfo.cancel();
    }
}
