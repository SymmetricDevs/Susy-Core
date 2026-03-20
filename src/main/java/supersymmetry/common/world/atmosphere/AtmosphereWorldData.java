package supersymmetry.common.world.atmosphere;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import supersymmetry.Supersymmetry;

import javax.annotation.Nonnull;

public class AtmosphereWorldData extends WorldSavedData {
    private static final String DATA_NAME = Supersymmetry.MODID + "_AtmosphereData";
    private AtmosphereRegionGraph graph;
    private NBTTagCompound pendingNbt;

    public AtmosphereWorldData(int dimension) {
        this(DATA_NAME + dimension);
    }

    public AtmosphereWorldData(String name) {
        super(name);
    }

    public static AtmosphereWorldData get(World world) {
        MapStorage storage = world.getMapStorage();
        if (storage == null) {
            throw new RuntimeException("Null world storage");
        }
        AtmosphereWorldData instance = (AtmosphereWorldData) storage.getOrLoadData(AtmosphereWorldData.class, DATA_NAME + world.provider.getDimension());
        if (instance == null) {
            instance = new AtmosphereWorldData(world.provider.getDimension());
            storage.setData(DATA_NAME + world.provider.getDimension(), instance);
        }
        if (instance.graph == null) {
            instance.graph = new AtmosphereRegionGraph();
            if (instance.pendingNbt != null) {
                instance.graph.readFromNBT(instance.pendingNbt);
                instance.pendingNbt = null;
            }
        }
        return instance;
    }

    public AtmosphereRegionGraph getGraph() {
        return graph;
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound nbt) {
        if (graph != null) {
            graph.readFromNBT(nbt);
        } else {
            pendingNbt = nbt;
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound nbt) {
        if (graph != null) {
            graph.writeToNBT(nbt);
        }
        return nbt;
    }
}
