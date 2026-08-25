package supersymmetry.client.renderer.handler;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.commons.lang3.ArrayUtils;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import dev.tianmi.sussypatches.client.model.QuadWrapper;

@SideOnly(Side.CLIENT)
public class BlockSkinRenderer {

    public static void renderBlockSkin(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline,
                                       IBlockState state, IBlockAccess world, BlockPos pos) {
        BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
        if (layer != null && !state.getBlock().canRenderInLayer(state, layer)) {
            return;
        }

        BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
        IBakedModel model = dispatcher.getModelForState(state);

        try {
            IBlockState extState = state.getBlock().getExtendedState(state, new SkinBlockAccess(world, pos), pos);
            if (extState != null) state = extState;
        } catch (Exception ignored) {}

        long posRand = MathHelper.getPositionRandom(pos);
        IVertexOperation[] fullPipeline = ArrayUtils.addAll(pipeline, translation);

        List<BakedQuad> quads = new ArrayList<>(model.getQuads(state, null, posRand));
        for (EnumFacing facing : EnumFacing.VALUES) {
            quads.addAll(model.getQuads(state, facing, posRand));
        }

        for (BakedQuad quad : quads) {
            QuadWrapper wrapper = new QuadWrapper(quad);
            renderState.setPipeline(wrapper, 0, wrapper.getVertices().length, fullPipeline);
            renderState.render();
        }
    }

    public static IBlockState resolveBlockState(Block block, int meta) {
        try {
            return block.getStateFromMeta(meta);
        } catch (Exception e) {
            return block.getDefaultState();
        }
    }
}
