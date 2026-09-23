package supersymmetry.client.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

@SideOnly(Side.CLIENT)
public class MissingModelCreator {
    public static void createModels(ModelBakeEvent event) throws IOException, NoSuchFieldException, IllegalAccessException {
        ModelLoader loader = event.getModelLoader();
        IBakedModel missingModel = event.getModelRegistry().getObject(ModelBakery.MODEL_MISSING);

        Map<ResourceLocation, Exception> loadingExceptions;
        Field exceptionsField;
        exceptionsField = ModelLoader.class.getDeclaredField("loadingExceptions");
        exceptionsField.setAccessible(true);
        loadingExceptions = (Map<ResourceLocation, Exception>) exceptionsField.get(loader);
        // IResource.getInputStream() hands back the same single-use stream on every call, so read each resource once
        String blankModel;
        byte[] blankItem;
        try (IResource resource = Minecraft.getMinecraft().getResourceManager()
                .getResource(new ResourceLocation("susy", "blankmodel.txt"))) {
            blankModel = new String(IOUtils.toByteArray(resource.getInputStream()), StandardCharsets.UTF_8);
        }
        try (IResource resource = Minecraft.getMinecraft().getResourceManager()
                .getResource(new ResourceLocation("susy", "blankitem.png"))) {
            blankItem = IOUtils.toByteArray(resource.getInputStream());
        }
        File gameDirectory = Minecraft.getMinecraft().gameDir;
        File assets = new File(gameDirectory, "resources");
        if (!assets.isDirectory()) {
            if (!assets.mkdirs()) {
                throw new IOException("Failed to create personal resources directory");
            }
        }

        Set<String> texturesToCheck = new HashSet<>();
        for (Map.Entry<ResourceLocation, Exception> entry : loadingExceptions.entrySet()) {
            // ignoring pure ResourceLocation arguments, all things we care about pass ModelResourceLocation
            if (entry.getKey() instanceof ModelResourceLocation) {
                ModelResourceLocation location = (ModelResourceLocation) entry.getKey();
                IBakedModel model = event.getModelRegistry().getObject(location);
                if (model == null || model == missingModel || // unfortunately this class is package-private
                        model.getClass().toString().equals("class net.minecraftforge.client.model.FancyMissingModel$BakedModel")) {
                    if (location.getPath().startsWith("metaitems")) {
                        // Copy blankitem.png and blankmodel.txt to assets/gregtech
                        String folderized = location.getPath().replace(".", "/");
                        texturesToCheck.add(folderized);
                        File modelLoc = new File(assets, "gregtech/models/item/" + location.getPath() + ".json");
                        File textureLoc = new File(assets, "gregtech/textures/items/" + folderized + ".png");
                        modelLoc.getParentFile().mkdirs();
                        textureLoc.getParentFile().mkdirs();
                        // Don't clobber anything that already exists (e.g. a texture someone has since drawn)
                        if (!textureLoc.exists()) {
                            Files.write(textureLoc.toPath(), blankItem);
                        }
                        if (!modelLoc.exists()) {
                            Files.write(modelLoc.toPath(),
                                    blankModel.replace("REPLACE", folderized).getBytes(StandardCharsets.UTF_8));
                        }
                    }
                }
            }
        }

        File textureCheckFile = new File(assets, "susy/texturestodo.txt");
        if (!textureCheckFile.exists()) {
            if (texturesToCheck.isEmpty()) {
                return;
            }
            textureCheckFile.getParentFile().mkdirs();
            textureCheckFile.createNewFile();
        }
        // Add in new textures to check as needed
        texturesToCheck.addAll(Files.readAllLines(textureCheckFile.toPath()));
        List<String> toRemove = new ArrayList<>();
        // Check if the texture files are different
        for (String path : texturesToCheck) {
            File textureLoc = new File(assets, "gregtech/textures/items/" + path + ".png");
            if (textureLoc.exists() && !Arrays.equals(Files.readAllBytes(textureLoc.toPath()), blankItem)) {
                toRemove.add(path);
            }
        }
        texturesToCheck.removeAll(toRemove);
        // Replace old texture check file
        Files.write(textureCheckFile.toPath(), texturesToCheck, StandardCharsets.UTF_8);
    }
}
