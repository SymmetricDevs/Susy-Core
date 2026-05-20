package supersymmetry.client.renderer.block;

import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import supersymmetry.Supersymmetry;

//because of the bs implementation, texture is added like this instead of via json
public enum RadicalAirModelLoader implements ICustomModelLoader {
    INSTANCE;

    private static final String MODEL_PATH = "models/block/air/radical_air_double_face";

    @Override
    public boolean accepts(ResourceLocation modelLocation) {
        return modelLocation.getNamespace().equals(Supersymmetry.MODID)
                && modelLocation.getPath().equals(MODEL_PATH);
    }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) {
        return RadicalAirModel.INSTANCE;
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {}
}
