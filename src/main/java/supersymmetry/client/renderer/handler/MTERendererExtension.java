package supersymmetry.client.renderer.handler;

import codechicken.lib.render.CCQuad;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.vec.Matrix4;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.client.utils.FacadeBlockAccess;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.pipeline.VertexLighterFlat;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

import static codechicken.lib.render.CCQuad.fromArray;
import static gregtech.client.renderer.handler.FacadeRenderer.renderBlockQuads;
import static gregtech.client.renderer.handler.FacadeRenderer.setupLighter;

//TODO: Optimization
@SideOnly(Side.CLIENT)
public class MTERendererExtension {

    public static void renderBaseBlock(CCRenderState ccrs, Matrix4 translation,
                                       MetaTileEntity mte, IBlockState visualState) {

        if (visualState == null) return;

        World world = mte.getWorld();
        BlockPos pos = mte.getPos();
        BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();

        for (var facing : EnumFacing.VALUES) {
            if (visualState.shouldSideBeRendered(world, pos, facing)) {
                MTERendererExtension.renderBaseBlockSide(ccrs, translation,
                        world, pos, facing, visualState, layer);
            }
        }
    }

    private static void renderBaseBlockSide(CCRenderState ccrs, Matrix4 translation, IBlockAccess world, BlockPos pos,
                                            EnumFacing facing, IBlockState state, BlockRenderLayer layer) {

        IBlockAccess coverAccess = new FacadeBlockAccess(world, pos, facing, state);
        if (layer != null && !state.getBlock().canRenderInLayer(state, layer)) return;

        BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();

        try {
            state = state.getActualState(coverAccess, pos);
        } catch (Exception ignored) { }

        IBakedModel model = dispatcher.getModelForState(state);

        try {
            state = state.getBlock().getExtendedState(state, coverAccess, pos);
        } catch (Exception ignored) { }

        long posRand = MathHelper.getPositionRandom(pos);
        List<CCQuad> quads = fromArray(model.getQuads(state, facing, posRand));

        if (!quads.isEmpty()) {
            VertexLighterFlat lighter = setupLighter(ccrs, translation, state, coverAccess, pos, model);
            renderBlockQuads(lighter, coverAccess, state, quads, pos);
        }
    }
}
