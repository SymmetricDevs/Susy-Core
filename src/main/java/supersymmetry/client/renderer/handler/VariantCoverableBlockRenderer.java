package supersymmetry.client.renderer.handler;

import java.util.ArrayList;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;

import org.apache.commons.lang3.tuple.Pair;

import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.render.BlockRenderer;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.block.BlockRenderingRegistry;
import codechicken.lib.render.block.ICCBlockRenderer;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.texture.TextureUtils;
import codechicken.lib.vec.*;
import codechicken.lib.vec.uv.IconTransformation;
import gregtech.client.renderer.CubeRendererState;
import gregtech.client.renderer.texture.Textures;
import supersymmetry.api.util.SuSyUtility;
import supersymmetry.common.tileentities.TileEntityCoverable;

public class VariantCoverableBlockRenderer implements ICCBlockRenderer {

    public static final ModelResourceLocation MODEL_LOCATION = new ModelResourceLocation(
            SuSyUtility.susyId("variant_coverable_block"), "normal");
    public static final VariantCoverableBlockRenderer INSTANCE = new VariantCoverableBlockRenderer();
    public static EnumBlockRenderType BLOCK_RENDER_TYPE;

    public static void preInit() {
        BLOCK_RENDER_TYPE = BlockRenderingRegistry.createRenderType("variant_coverable_block");
        BlockRenderingRegistry.registerRenderer(BLOCK_RENDER_TYPE, INSTANCE);
        MinecraftForge.EVENT_BUS.register(INSTANCE);
        TextureUtils.addIconRegister(Textures::register);
    }

    @Override
    public boolean renderBlock(IBlockAccess world, BlockPos pos, IBlockState state, BufferBuilder buffer) {
        TileEntityCoverable tileECoverable = getTileCoverable(world, pos);
        if (tileECoverable == null) {
            return false;
        }
        TileEntityCoverable.RENDER_SWITCH = false;
        Minecraft.getMinecraft().getBlockRendererDispatcher().renderBlock(tileECoverable.getBlockState(), pos, world,
                buffer);
        TileEntityCoverable.RENDER_SWITCH = true;
        CCRenderState renderState = CCRenderState.instance();
        renderState.reset();
        renderState.bind(buffer);
        Matrix4 translation = new Matrix4().translate(pos.getX(), pos.getY(), pos.getZ());
        BlockRenderLayer renderLayer = MinecraftForgeClient.getRenderLayer();
        boolean[] sideMask = new boolean[EnumFacing.VALUES.length];
        for (EnumFacing side : EnumFacing.VALUES) {
            sideMask[side.getIndex()] = state.shouldSideBeRendered(world, pos, side);
        }
        Textures.RENDER_STATE.set(new CubeRendererState(renderLayer, sideMask, world));
        renderState.lightMatrix.locate(world, pos);
        IVertexOperation[] pipeline = new IVertexOperation[] { renderState.lightMatrix };
        if (tileECoverable.getBlockType().canRenderInLayer(world.getBlockState(pos), renderLayer))
            tileECoverable.render(renderState, translation.copy(), pipeline);

        Textures.RENDER_STATE.remove();
        return true;
    }

    @Override
    public void renderBrightness(IBlockState state, float brightness) {}

    public static TileEntityCoverable getTileCoverable(IBlockAccess world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        return te instanceof TileEntityCoverable ? (TileEntityCoverable) te : null;
    }

    @Override
    public void handleRenderBlockDamage(IBlockAccess world, BlockPos pos, IBlockState state, TextureAtlasSprite sprite,
                                        BufferBuilder buffer) {
        TileEntityCoverable tileECoverable = getTileCoverable(world, pos);
        ArrayList<IndexedCuboid6> boundingBox = new ArrayList<>();
        if (tileECoverable != null) {
            tileECoverable.addCollisionBoundingBox(boundingBox);
        }
        CCRenderState renderState = CCRenderState.instance();
        renderState.reset();
        renderState.bind(buffer);
        renderState.setPipeline(new Vector3(new Vec3d(pos)).translation(), new IconTransformation(sprite));
        for (Cuboid6 cuboid : boundingBox) {
            BlockRenderer.renderCuboid(renderState, cuboid, 0);
        }
    }

    public static Pair<TextureAtlasSprite, Integer> getParticleTexture(IBlockAccess world, BlockPos pos) {
        TileEntityCoverable tileECoverable = getTileCoverable(world, pos);
        if (tileECoverable == null) {
            return Pair.of(TextureUtils.getMissingSprite(), 0xFFFFFF);
        } else {
            return tileECoverable.getParticleTexture();
        }
    }

    @Override
    public void registerTextures(TextureMap map) {}
}
