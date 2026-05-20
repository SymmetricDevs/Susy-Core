package supersymmetry.client.renderer.block;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;

import java.util.Collection;
import java.util.function.Function;

//glue
public enum RadicalAirModel implements IModel {
    INSTANCE;

    public static final ResourceLocation TEXTURE =
            new ResourceLocation("gregtech", "blocks/air/radical_air");

    @Override
    public Collection<ResourceLocation> getTextures() {
        return ImmutableSet.of(TEXTURE);
    }

    @Override
    public net.minecraft.client.renderer.block.model.IBakedModel bake(
            IModelState state,
            VertexFormat format,
            Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {

        TextureAtlasSprite sprite = bakedTextureGetter.apply(TEXTURE);
        return new RadicalAirBakedModel(sprite);
    }
}
