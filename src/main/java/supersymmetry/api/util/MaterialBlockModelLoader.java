/*
 * DISCLAIMER:
 * Full credit goes to tictim for most of the code here.
 * I simply copied over and Frankensteined the code from his fork with other things that are private or
 * non-existent currently.
 */
package supersymmetry.api.util;

import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import gregtech.api.GTValues;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.api.util.GTLog;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import supersymmetry.Supersymmetry;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID, value = Side.CLIENT)
public class MaterialBlockModelLoader {

    private static final Table<MaterialIconType, MaterialIconSet, ResourceLocation> BLOCKSTATES_CACHE = HashBasedTable
            .create();

    private static final Object2ObjectOpenHashMap<Entry, ModelResourceLocation> ENTRIES = new Object2ObjectOpenHashMap<>();

    @Nonnull
    public static ModelResourceLocation loadBlockModel(@Nonnull MaterialIconType iconType,
                                                       @Nonnull MaterialIconSet iconSet) {
        return loadBlockModel(iconType, iconSet, null);
    }

    @Nonnull
    public static ModelResourceLocation loadBlockModel(@Nonnull MaterialIconType iconType,
                                                       @Nonnull MaterialIconSet iconSet,
                                                       @Nullable String variant) {
        return ENTRIES.computeIfAbsent(new Entry(iconType, iconSet, variant == null ? "" : variant),
                entry -> createModelId(entry, "normal"));
    }

    @Nonnull
    public static ModelResourceLocation loadItemModel(@Nonnull MaterialIconType iconType,
                                                      @Nonnull MaterialIconSet iconSet) {
        return ENTRIES.computeIfAbsent(new Entry(iconType, iconSet, null),
                entry -> createModelId(entry, "inventory"));
    }

    private static ModelResourceLocation createModelId(@Nonnull Entry entry, @Nonnull String variant) {
        StringBuilder stb = new StringBuilder();
        stb.append("material_").append(entry.iconType.name).append("_").append(entry.iconSet.name);
        if (entry.variant != null && !entry.variant.equals("") && !entry.variant.equals("normal")) {
            stb.append("_").append(entry.variant.replace('=', '_').replace(',', '_'));
        }
        return new ModelResourceLocation(new ResourceLocation(GTValues.MODID, stb.toString()), variant);
    }

    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        for (Entry e : ENTRIES.keySet()) {
            e.modelCache = loadModel(event, e);
        }
    }

    @Nullable
    private static IModel loadModel(TextureStitchEvent.Pre event, Entry entry) {
        IModel model;
        try {
            model = ModelLoaderRegistry.getModel(entry.getModelLocation());
        } catch (Exception e) {
            GTLog.logger.error("Failed to load material model {}:", entry, e);
            return null;
        }
        for (ResourceLocation texture : model.getTextures()) {
            event.getMap().registerSprite(texture);
        }
        return model;
    }

    @SuppressWarnings("deprecation")
    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        Map<ModelResourceLocation, IModel> stateModels = Loader.isModLoaded(GTValues.MODID_CTM) ?
                ReflectionHelper.getPrivateValue(ModelLoader.class, event.getModelLoader(), "stateModels", null) :
                null;

        for (var e : ENTRIES.entrySet()) {
            bakeAndRegister(event.getModelRegistry(), e.getKey().modelCache, e.getValue(), stateModels);
        }
    }

    private static void bakeAndRegister(@Nonnull IRegistry<ModelResourceLocation, IBakedModel> registry,
                                        @Nullable IModel model,
                                        @Nonnull ModelResourceLocation modelId,
                                        @Nullable Map<ModelResourceLocation, IModel> stateModels) {
        if (model == null) {
            // insert missing model to prevent cluttering logs with useless model loading error messages
            registry.putObject(modelId, bake(ModelLoaderRegistry.getMissingModel()));
            return;
        }
        registry.putObject(modelId, bake(model));

        if (stateModels != null) { // CTM needs the model to be present on this field to properly replace the model
            stateModels.put(modelId, model);
        }
    }

    private static IBakedModel bake(IModel model) {
        return model.bake(
                model.getDefaultState(),
                DefaultVertexFormats.ITEM,
                t -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(t.toString()));
    }

    private static final class Entry {

        private final MaterialIconType iconType;
        private final MaterialIconSet iconSet;
        private final String variant;

        @Nullable
        private IModel modelCache;

        private Entry(@Nonnull MaterialIconType iconType,
                      @Nonnull MaterialIconSet iconSet,
                      @Nullable String variant) {
            this.iconType = iconType;
            this.iconSet = iconSet;
            this.variant = variant;
        }

        public ResourceLocation getModelLocation() {
            if (this.variant != null) {
                return new ModelResourceLocation(iconType.recurseIconsetPath(iconSet, BLOCKSTATES_CACHE,
                        "blockstates/material_sets/%s/%s.json", "material_sets/%s/%s"), variant);
            } else {
                ResourceLocation itemModelPath = iconType.getItemModelPath(iconSet);
                return new ResourceLocation(itemModelPath.getNamespace(), "item/" + itemModelPath.getPath());
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Entry entry = (Entry) o;
            return Objects.equals(variant, entry.variant) && iconType.equals(entry.iconType) &&
                    iconSet.equals(entry.iconSet);
        }

        @Override
        public int hashCode() {
            return Objects.hash(iconType, iconSet, variant);
        }

        @Override
        public String toString() {
            return "Entry{" +
                    "iconType=" + iconType +
                    ", iconSet=" + iconSet +
                    ", variant=" + variant +
                    '}';
        }
    }
}
