package supersymmetry.client.moldraw.data;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import gregtech.api.GregTechAPI;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.fluids.GTFluid;
import supersymmetry.Supersymmetry;

/** Loads alloy component lists, deriving unspecified compositions from registered materials. */
public final class AlloyData implements IResourceManagerReloadListener {

    private static final AlloyData INSTANCE = new AlloyData();
    private static final Logger LOGGER = LogManager.getLogger("SuSy Alloy Drawings");
    private static volatile Map<Material, List<Component>> alloys = new HashMap<Material, List<Component>>();
    private static IResourceManager cachedManager;
    private static IReloadableResourceManager registeredManager;

    private AlloyData() {}

    /** Returns component records for a material, or {@code null} when it is not indexed. */
    public static List<Component> get(Material material) {
        if (material == null) return null;
        synchronized (INSTANCE) {
            IResourceManager manager = Minecraft.getMinecraft().getResourceManager();
            if (manager != cachedManager) {
                if (manager instanceof IReloadableResourceManager && manager != registeredManager) {
                    ((IReloadableResourceManager) manager).registerReloadListener(INSTANCE);
                    registeredManager = (IReloadableResourceManager) manager;
                }
                INSTANCE.load(manager);
                cachedManager = manager;
            }
            return alloys.get(material);
        }
    }

    /** Resolves a GT material fluid stack, including HEI/JEI fluid ingredients. */
    public static List<Component> get(FluidStack stack) {
        if (stack == null || !(stack.getFluid() instanceof GTFluid.GTMaterialFluid)) return null;
        return get(((GTFluid.GTMaterialFluid) stack.getFluid()).getMaterial());
    }

    @Override
    public void onResourceManagerReload(IResourceManager manager) {
        synchronized (INSTANCE) {
            INSTANCE.load(manager);
            cachedManager = manager;
        }
    }

    private void load(IResourceManager manager) {
        Map<Material, List<Component>> loaded = new HashMap<Material, List<Component>>();
        readIndex(manager, "index.json", loaded);
        readIndex(manager, "custom-index.json", loaded);
        alloys = loaded;
    }

    private void readIndex(IResourceManager manager, String indexFile, Map<Material, List<Component>> loaded) {
        ResourceLocation indexLocation = new ResourceLocation(Supersymmetry.MODID, "alloys/" + indexFile);
        try (IResource index = manager.getResource(indexLocation);
                InputStreamReader reader = new InputStreamReader(index.getInputStream(), StandardCharsets.UTF_8)) {
            JsonObject root = new JsonParser().parse(reader).getAsJsonObject();
            if (!root.has("alloys") || !root.get("alloys").isJsonArray()) {
                LOGGER.warn("Invalid alloy index at {}: missing alloys array", indexLocation);
                return;
            }
            JsonArray entries = root.getAsJsonArray("alloys");
            for (JsonElement element : entries) {
                if (!element.isJsonObject()) continue;
                JsonObject entry = element.getAsJsonObject();
                if (!entry.has("material") || !entry.has("file")) continue;
                String materialId = entry.get("material").getAsString();
                String file = entry.get("file").getAsString();
                if (!file.matches("[a-z0-9_.-]+(/[a-z0-9_.-]+)*\\.json") || file.contains("..")) {
                    LOGGER.warn("Skipping unsafe alloy resource path in {}: {}", indexLocation, file);
                    continue;
                }

                Material material = GregTechAPI.materialManager.getMaterial(materialId);
                if (material == null) continue;
                ResourceLocation alloyLocation = new ResourceLocation(Supersymmetry.MODID, "alloys/" + file);
                try (IResource alloyResource = manager.getResource(alloyLocation);
                        InputStreamReader alloyReader = new InputStreamReader(alloyResource.getInputStream(),
                                StandardCharsets.UTF_8)) {
                    JsonObject alloy = new JsonParser().parse(alloyReader).getAsJsonObject();
                    List<Component> components = parse(alloy, material);
                    if (components != null && !components.isEmpty()) loaded.put(material, components);
                } catch (Exception exception) {
                    LOGGER.warn("Unable to load alloy data {} for {}", alloyLocation, materialId, exception);
                }
            }
        } catch (Exception exception) {
            LOGGER.warn("Unable to load alloy index {}", indexLocation, exception);
        }
    }

    private List<Component> parse(JsonObject object, Material material) {
        if (object.has("derive") && object.get("derive").getAsBoolean()) {
            Map<Material, Long> derived = new LinkedHashMap<Material, Long>();
            derive(material, 1L, new HashSet<Material>(), derived);
            int minimum = object.has("min_components") ? object.get("min_components").getAsInt() : 1;
            int maximum = object.has("max_components") ? object.get("max_components").getAsInt() : Integer.MAX_VALUE;
            if (derived.size() < minimum || derived.size() > maximum) return null;
            return toComponents(derived);
        }
        if (!object.has("components") || !object.get("components").isJsonArray()) return null;

        Map<Material, Long> components = new LinkedHashMap<Material, Long>();
        for (JsonElement element : object.getAsJsonArray("components")) {
            String materialId;
            long amount = 1L;
            if (element.isJsonPrimitive()) {
                materialId = element.getAsString();
            } else if (element.isJsonArray()) {
                JsonArray pair = element.getAsJsonArray();
                if (pair.size() < 1 || pair.size() > 2) continue;
                materialId = pair.get(0).getAsString();
                if (pair.size() == 2) amount = pair.get(1).getAsLong();
            } else {
                continue;
            }
            Material component = GregTechAPI.materialManager.getMaterial(materialId);
            if (component != null && amount > 0) components.merge(component, amount, Long::sum);
        }
        return toComponents(components);
    }

    private void derive(Material material, long multiplier, Set<Material> path, Map<Material, Long> result) {
        if (!path.add(material)) return;
        List<MaterialStack> materialComponents = material.getMaterialComponents();
        if (materialComponents == null || materialComponents.isEmpty()) {
            result.merge(material, multiplier, Long::sum);
        } else {
            for (MaterialStack component : materialComponents) {
                if (component != null && component.material != null && component.amount > 0) {
                    long amount = multiplier * component.amount;
                    derive(component.material, amount, path, result);
                }
            }
        }
        path.remove(material);
    }

    private List<Component> toComponents(Map<Material, Long> components) {
        List<Component> result = new ArrayList<Component>(components.size());
        for (Map.Entry<Material, Long> entry : components.entrySet()) {
            result.add(new Component(entry.getKey(), entry.getValue()));
        }
        return Collections.unmodifiableList(result);
    }

    public static final class Component {

        private final Material material;
        private final long amount;

        private Component(Material material, long amount) {
            this.material = material;
            this.amount = amount;
        }

        public Material getMaterial() {
            return material;
        }

        public long getAmount() {
            return amount;
        }
    }
}
