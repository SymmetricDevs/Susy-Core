package supersymmetry.api.pipenet.beamline;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import supersymmetry.common.pipelike.beamline.ColliderBeamLineType;
import supersymmetry.common.pipelike.beamline.SingleBeamLineType;

import java.util.Objects;

public abstract class BeamLineType {

    private static final Object2ObjectOpenHashMap<String, BeamLineType> BEAMLINE_TYPES = new Object2ObjectOpenHashMap<>();
    private final String name;

    protected BeamLineType(String name) {
        this.name = Objects.requireNonNull(name);
        if (BEAMLINE_TYPES.containsKey(name)) {
            throw new IllegalArgumentException("Beamline Type with name " + name + " already exists!");
        }
        for (BeamLineType beamLineType : BEAMLINE_TYPES.values()) {
            if (this.getClass() == beamLineType.getClass()) {
                throw new IllegalStateException("Duplicate Beamline Type " + name + " and " + beamLineType.name);
            }
        }
        BEAMLINE_TYPES.put(name, this);
    }

    public final String getName() {
        return name;
    }

    public static BeamLineType getBeamLineType(String name) {
        return BEAMLINE_TYPES.get(name);
    }

    public static SingleBeamLineType single() {
        return SingleBeamLineType.INSTANCE;
    }

    public static ColliderBeamLineType collider() {
        return ColliderBeamLineType.INSTANCE;
    }

    public boolean isValidPart(IParticleNetworkPart networkPart) {
        return networkPart != null && networkPart.getBeamLineType() == this;
    }

    public int getMinLength() {
        return 0;
    }

    public boolean satisfiesMinLength(IBeamLineEndpoint endpoint1, IBeamLineEndpoint endpoint2) {
        BlockPos p = endpoint2.pos();
        return endpoint1 != endpoint2 && endpoint1.pos().getDistance(p.getX(), p.getY(), p.getZ()) >= getMinLength();
    }

    @NotNull
    public ParticleNetwork createNetwork(ParticleNetwork.WorldData worldData) {
        return new ParticleNetwork(this, worldData);
    }

    public final ParticleNetwork createNetwork(World world) {
        return createNetwork(ParticleNetwork.WorldData.get(world));
    }

}
