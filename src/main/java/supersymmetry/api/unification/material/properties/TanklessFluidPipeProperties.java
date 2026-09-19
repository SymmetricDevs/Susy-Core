package supersymmetry.api.unification.material.properties;

import java.util.Collection;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import gregtech.api.capability.IPropertyFluidFilter;
import gregtech.api.fluids.FluidState;
import gregtech.api.fluids.attribute.FluidAttribute;
import gregtech.api.fluids.attribute.FluidAttributes;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import gregtech.api.unification.material.properties.IMaterialProperty;
import gregtech.api.unification.material.properties.MaterialProperties;
import gregtech.api.unification.material.properties.PropertyKey;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import lombok.Getter;
import lombok.val;

public class TanklessFluidPipeProperties implements IMaterialProperty, IPropertyFluidFilter {

    private final Object2BooleanMap<FluidAttribute> containmentPredicate = new Object2BooleanOpenHashMap<>();

    /// rate in stacks per tick
    @Getter
    private int throughput;
    @Getter
    private int maxFluidTemperature;
    @Getter
    private boolean gasProof;
    @Getter
    private boolean cryoProof;
    @Getter
    private boolean plasmaProof;

    /// fluids will try to take the path with the lowest resistance
    @Getter
    private int resistance;

    public TanklessFluidPipeProperties(int maxFluidTemperature, int throughput, boolean gasProof, boolean acidProof,
                                       boolean cryoProof, boolean plasmaProof) {
        this(maxFluidTemperature, throughput, gasProof, acidProof, cryoProof, plasmaProof, 1);
    }

    public TanklessFluidPipeProperties(int maxFluidTemperature, int throughput, boolean gasProof, boolean acidProof,
                                       boolean cryoProof, boolean plasmaProof, int resistance) {
        this.maxFluidTemperature = maxFluidTemperature;
        this.throughput = throughput;
        this.gasProof = gasProof;
        if (acidProof) setCanContain(FluidAttributes.ACID, true);
        this.cryoProof = cryoProof;
        this.plasmaProof = plasmaProof;
        this.resistance = resistance;
    }

    public TanklessFluidPipeProperties() {
        this(300, 1, false, false, false, false);
    }

    public static TanklessFluidPipeProperties from(FluidPipeProperties fluidPipeProperties) {
        TanklessFluidPipeProperties properties = new TanklessFluidPipeProperties(
                fluidPipeProperties.getMaxFluidTemperature(),
                fluidPipeProperties.getThroughput(),
                fluidPipeProperties.isGasProof(),
                fluidPipeProperties.isAcidProof(),
                fluidPipeProperties.isCryoProof(),
                fluidPipeProperties.isPlasmaProof(),
                1);

        for (val attribute : fluidPipeProperties.getContainedAttributes()) {
            properties.setCanContain(attribute, true);
        }
        return properties;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        if (!properties.hasProperty(PropertyKey.WOOD)) {
            properties.ensureSet(PropertyKey.INGOT, true);
        }
    }

    public TanklessFluidPipeProperties setThroughput(int throughput) {
        this.throughput = throughput;
        return this;
    }

    public TanklessFluidPipeProperties setMaxFluidTemperature(int maxFluidTemperature) {
        this.maxFluidTemperature = maxFluidTemperature;
        return this;
    }

    @Override
    public boolean canContain(@NotNull FluidState state) {
        return switch (state) {
            case LIQUID -> true;
            case GAS -> gasProof;
            case PLASMA -> plasmaProof;
        };
    }

    @Override
    public boolean canContain(@NotNull FluidAttribute attribute) {
        return containmentPredicate.getBoolean(attribute);
    }

    @Override
    public void setCanContain(@NotNull FluidAttribute attribute, boolean canContain) {
        this.containmentPredicate.put(attribute, canContain);
    }

    @Override
    public @NotNull @UnmodifiableView Collection<@NotNull FluidAttribute> getContainedAttributes() {
        return containmentPredicate.keySet();
    }

    public TanklessFluidPipeProperties setGasProof(boolean gasProof) {
        this.gasProof = gasProof;
        return this;
    }

    public boolean isAcidProof() {
        return canContain(FluidAttributes.ACID);
    }

    public TanklessFluidPipeProperties setCryoProof(boolean cryoProof) {
        this.cryoProof = cryoProof;
        return this;
    }

    public TanklessFluidPipeProperties setPlasmaProof(boolean plasmaProof) {
        this.plasmaProof = plasmaProof;
        return this;
    }

    public TanklessFluidPipeProperties setResistance(int resistance) {
        this.resistance = resistance;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TanklessFluidPipeProperties that)) return false;
        return getThroughput() == that.getThroughput() &&
                getMaxFluidTemperature() == that.getMaxFluidTemperature() &&
                isGasProof() == that.isGasProof() &&
                isCryoProof() == that.isCryoProof() &&
                isPlasmaProof() == that.isPlasmaProof() &&
                getResistance() == that.getResistance() &&
                containmentPredicate.equals(that.containmentPredicate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getThroughput(), getMaxFluidTemperature(), gasProof, cryoProof, plasmaProof,
                resistance, containmentPredicate);
    }

    @Override
    public String toString() {
        return "TanklessFluidPipeProperties{" +
                "throughput=" + throughput +
                ", maxFluidTemperature=" + maxFluidTemperature +
                ", gasProof=" + gasProof +
                ", cryoProof=" + cryoProof +
                ", plasmaProof=" + plasmaProof +
                ", resistance=" + resistance +
                ", containmentPredicate=" + containmentPredicate +
                '}';
    }
}
