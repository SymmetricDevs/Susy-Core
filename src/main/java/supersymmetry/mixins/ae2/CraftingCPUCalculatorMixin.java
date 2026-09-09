package supersymmetry.mixins.ae2;

import org.spongepowered.asm.mixin.Mixin;

import appeng.api.util.WorldCoord;
import appeng.me.cluster.IAEMultiBlock;
import appeng.me.cluster.MBCalculator;
import appeng.me.cluster.implementations.CraftingCPUCalculator;

@Mixin(value = CraftingCPUCalculator.class, remap = false)
public abstract class CraftingCPUCalculatorMixin extends MBCalculator {

    public CraftingCPUCalculatorMixin(IAEMultiBlock t) {
        super(t);
    }

    @Override
    public boolean checkMultiblockScale(WorldCoord min, WorldCoord max) {
        return (max.x - min.x + 1) * (max.y - min.y + 1) * (max.z - min.z + 1) <= 16;
    }
}
