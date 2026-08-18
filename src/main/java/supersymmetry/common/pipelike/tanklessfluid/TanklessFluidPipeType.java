package supersymmetry.common.pipelike.tanklessfluid;

import org.jspecify.annotations.NullMarked;

import gregtech.api.pipenet.block.material.IMaterialPipeType;
import gregtech.api.unification.ore.OrePrefix;
import lombok.Getter;
import lombok.val;
import supersymmetry.api.unification.material.properties.TanklessFluidPipeProperties;
import supersymmetry.api.unification.ore.SusyOrePrefix;

@NullMarked
public enum TanklessFluidPipeType implements IMaterialPipeType<TanklessFluidPipeProperties> {

    TINY("tiny", 0.25f, SusyOrePrefix.pipeTinyTanklessFluid, 0.25f, 2f),
    SMALL("small", 0.375f, SusyOrePrefix.pipeSmallTanklessFluid, 0.5f, 1.5f),
    NORMAL("normal", 0.5f, SusyOrePrefix.pipeNormalTanklessFluid, 1f, 1f),
    LARGE("large", 0.75f, SusyOrePrefix.pipeLargeTanklessFluid, 2f, 0.75f),
    HUGE("huge", 0.875f, SusyOrePrefix.pipeHugeTanklessFluid, 4f, 0.5f),

    RESTRICTIVE_TINY("tiny_restrictive", 0.25f, SusyOrePrefix.pipeTinyRestrictiveTanklessFluid, 0.25f, 200f),
    RESTRICTIVE_SMALL("small_restrictive", 0.375f, SusyOrePrefix.pipeSmallRestrictiveTanklessFluid, 0.5f, 150f),
    RESTRICTIVE_NORMAL("normal_restrictive", 0.5f, SusyOrePrefix.pipeNormalRestrictiveTanklessFluid, 1f, 100f),
    RESTRICTIVE_LARGE("large_restrictive", 0.75f, SusyOrePrefix.pipeLargeRestrictiveTanklessFluid, 2f, 75f),
    RESTRICTIVE_HUGE("huge_restrictive", 0.875f, SusyOrePrefix.pipeHugeRestrictiveTanklessFluid, 4f, 50f);

    public static final TanklessFluidPipeType[] VALUES = values();

    @Getter(onMethod_ = @Override)
    public final String name;
    @Getter(onMethod_ = @Override)
    private final float thickness;
    @Getter
    private final float rateMultiplier;
    private final float resistanceMultiplier;
    @Getter(onMethod_ = @Override)
    private final OrePrefix orePrefix;

    TanklessFluidPipeType(String name, float thickness, OrePrefix orePrefix, float rateMultiplier,
                          float resistanceMultiplier) {
        this.name = name;
        this.thickness = thickness;
        this.orePrefix = orePrefix;
        this.rateMultiplier = rateMultiplier;
        this.resistanceMultiplier = resistanceMultiplier;
    }

    public boolean isRestrictive() {
        return ordinal() > 4;
    }

    @Override
    public TanklessFluidPipeProperties modifyProperties(TanklessFluidPipeProperties baseProperties) {
        val modified = new TanklessFluidPipeProperties(
                baseProperties.getMaxFluidTemperature(),
                (int) (baseProperties.getThroughput() * rateMultiplier),
                baseProperties.isGasProof(),
                baseProperties.isAcidProof(),
                baseProperties.isCryoProof(),
                baseProperties.isPlasmaProof(),
                (int) ((baseProperties.getResistance() * resistanceMultiplier) + 0.5));
        for (val attribute : baseProperties.getContainedAttributes()) {
            modified.setCanContain(attribute, baseProperties.canContain(attribute));
        }
        return modified;
    }

    @Override
    public boolean isPaintable() {
        return true;
    }
}
