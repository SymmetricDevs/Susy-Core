package supersymmetry.client.renderer.block;

import java.util.Collection;
import java.util.function.Function;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;

import com.google.common.collect.ImmutableSet;

// glue
public enum FlammableAirModel implements IModel {

    INSTANCE;

    public static final ResourceLocation TEXTURE = new ResourceLocation("gregtech", "blocks/air/flammable_air");

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
        return new FlammableAirBakedModel(sprite);
    }
}
