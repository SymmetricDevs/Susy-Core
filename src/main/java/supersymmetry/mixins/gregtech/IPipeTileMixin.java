package supersymmetry.mixins.gregtech;

import org.spongepowered.asm.mixin.Mixin;

import gregtech.api.pipenet.tile.IPipeTile;
import supersymmetry.api.pipelike.ConnectablePipe;

@Mixin(value = IPipeTile.class, remap = false)
public interface IPipeTileMixin extends ConnectablePipe {
    // Interface implementation only
}
