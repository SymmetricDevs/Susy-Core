package supersymmetry.mixins.gregtech;

import gregtech.common.pipelike.cable.net.EnergyRoutePath;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EnergyRoutePath.class)
public class EnergyRoutePathMixin {
    @Shadow(remap = false)
    private long maxLoss;
    /**
     * Returns the loss of the energy route.
     * @author Bruberu
     * @reason This really shouldn't be overwritten anywhere else, and also this is very particular to this pack's balancing.
     * @return The loss of the energy route
     */
    @Overwrite(remap = false)
    public long getMaxLoss() {
        return maxLoss / 10;
    }
}
