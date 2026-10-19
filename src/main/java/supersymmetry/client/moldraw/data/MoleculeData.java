package supersymmetry.client.moldraw.data;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

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
import gregtech.api.fluids.GTFluid;
import supersymmetry.Supersymmetry;
import supersymmetry.client.molecule.Molecule;

/** Loads indexed molecule diagrams and associates them with registered GT materials. */
public final class MoleculeData implements IResourceManagerReloadListener {

    private static final MoleculeData INSTANCE = new MoleculeData();
    private static final Logger LOGGER = LogManager.getLogger("SuSy Molecule Drawings");
    private static volatile Map<Material, Molecule> molecules = new HashMap<Material, Molecule>();
    private static IResourceManager cachedManager;
    private static IReloadableResourceManager registeredManager;

    private MoleculeData() {}

    /** Returns a drawing for a material, or {@code null} if no compatible indexed drawing exists. */
    public static Molecule get(Material material) {
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
            return molecules.get(material);
        }
    }

    /** Resolves a GT material fluid stack, including HEI/JEI fluid ingredients. */
    public static Molecule get(FluidStack stack) {
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
        Map<Material, Molecule> loaded = new HashMap<Material, Molecule>();
        readIndex(manager, "molecules", "molecules", "index.json", loaded);
        readIndex(manager, "molecules", "molecules", "custom-index.json", loaded);
        molecules = loaded;
    }

    private void readIndex(IResourceManager manager, String kind, String directory, String indexFile,
                           Map<Material, Molecule> destination) {
        ResourceLocation indexLocation = new ResourceLocation(Supersymmetry.MODID, directory + "/" + indexFile);
        try (IResource index = manager.getResource(indexLocation);
                InputStreamReader reader = new InputStreamReader(index.getInputStream(), StandardCharsets.UTF_8)) {
            JsonObject root = new JsonParser().parse(reader).getAsJsonObject();
            if (!root.has(kind) || !root.get(kind).isJsonArray()) {
                LOGGER.warn("Invalid {} index at {}: missing {} array", kind, indexLocation, kind);
                return;
            }
            JsonArray entries = root.getAsJsonArray(kind);
            for (JsonElement element : entries) {
                if (!element.isJsonObject()) continue;
                JsonObject entry = element.getAsJsonObject();
                if (!entry.has("material") || !entry.has("file")) continue;

                String materialId = entry.get("material").getAsString();
                String file = entry.get("file").getAsString();
                if (!file.matches("[a-z0-9_.-]+(/[a-z0-9_.-]+)*\\.json") || file.contains("..")) {
                    LOGGER.warn("Skipping unsafe molecule resource path in {}: {}", indexLocation, file);
                    continue;
                }

                Material material = GregTechAPI.materialManager.getMaterial(materialId);
                if (material == null) continue;

                ResourceLocation geometryLocation = new ResourceLocation(Supersymmetry.MODID,
                        directory + "/" + file);
                try (IResource geometry = manager.getResource(geometryLocation);
                        InputStreamReader geometryReader = new InputStreamReader(geometry.getInputStream(),
                                StandardCharsets.UTF_8)) {
                    destination.put(material,
                            Molecule.fromJson(new JsonParser().parse(geometryReader).getAsJsonObject()));
                } catch (Exception exception) {
                    LOGGER.warn("Unable to load molecule drawing {} for {}", geometryLocation, materialId, exception);
                }
            }
        } catch (Exception exception) {
            LOGGER.warn("Unable to load molecule index {}", indexLocation, exception);
        }
    }
}
