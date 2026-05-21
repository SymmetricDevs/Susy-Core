package supersymmetry.client.renderer.block;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;

// code blatantly stolen from Mojang water rendering implementation
// who the fuck was the funny guy that removed setRenderFromInside() after 1.7? do you see the kind of bullshit you've
// caused?
// sincerely, Polska Spółka Gazownictwa (PSG)

public class RadicalAirBakedModel implements IBakedModel {

    private static final float[] CORNER_X = { 0, 0, 1, 1 };
    private static final float[] CORNER_Y = { 0, 1, 1, 0 };
    private static final float EPS = 1e-3f;

    private final TextureAtlasSprite sprite;
    private final VertexFormat format;

    public RadicalAirBakedModel(TextureAtlasSprite sprite) {
        this.sprite = sprite;
        this.format = DefaultVertexFormats.BLOCK;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        if (side == null) return ImmutableList.of();

        List<BakedQuad> quads = new ArrayList<>(2);
        quads.add(buildFaceQuad(side, false, false));
        quads.add(buildFaceQuad(side, true, true));
        return quads;
    }

    private BakedQuad buildFaceQuad(EnumFacing side, boolean flip, boolean offset) {
        UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
        builder.setQuadOrientation(side);
        builder.setTexture(sprite);

        for (int i = 0; i < 4; i++) {
            int vi = flip ? (3 - i) : i;
            float[] pos = cornerPosition(side, vi, offset);
            float u = sprite.getInterpolatedU(CORNER_X[vi] * 16f);
            float v = sprite.getInterpolatedV(CORNER_Y[vi] * 16f);
            putVertex(builder, side, pos[0], pos[1], pos[2], u, v);
        }

        return builder.build();
    }

    private float[] cornerPosition(EnumFacing side, int vi, boolean offset) {
        float cx = CORNER_X[vi];
        float cy = CORNER_Y[vi];

        float x, y, z;
        switch (side) {
            case UP:
                x = cx;
                y = 1f;
                z = cy;
                break;
            case DOWN:
                x = cx;
                y = 0f;
                z = 1f - cy;
                break;
            case NORTH:
                x = 1f - cx;
                y = cy;
                z = 0f;
                break;
            case SOUTH:
                x = cx;
                y = cy;
                z = 1f;
                break;
            case WEST:
                x = 0f;
                y = cy;
                z = cx;
                break;
            case EAST:
            default:
                x = 1f;
                y = cy;
                z = 1f - cx;
                break;
        }

        if (offset) {
            float nx = side.getDirectionVec().getX();
            float ny = side.getDirectionVec().getY();
            float nz = side.getDirectionVec().getZ();

            x -= nx * EPS;
            y -= ny * EPS;
            z -= nz * EPS;
        }

        return new float[] { x, y, z };
    }

    private void putVertex(UnpackedBakedQuad.Builder builder, EnumFacing side,
                           float x, float y, float z, float u, float v) {
        for (int e = 0; e < format.getElementCount(); e++) {
            switch (format.getElement(e).getUsage()) {
                case POSITION:
                    builder.put(e, x, y, z, 1f);
                    break;
                case COLOR:
                    builder.put(e, 1f, 1f, 1f, 1f);
                    break;
                case UV:
                    if (format.getElement(e).getIndex() == 0) {
                        builder.put(e, u, v, 0f, 1f);
                    } else {
                        builder.put(e);
                    }
                    break;
                case NORMAL:
                    builder.put(e,
                            (float) side.getXOffset(),
                            (float) side.getYOffset(),
                            (float) side.getZOffset(), 0f);
                    break;
                default:
                    builder.put(e);
                    break;
            }
        }
    }

    @Override
    public boolean isAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return sprite;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(
                                                                   ItemCameraTransforms.TransformType type) {
        return Pair.of(this, null);
    }
}
