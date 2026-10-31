package supersymmetry.mixins.gregtech;

import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import gregtech.api.worldgen.generator.CachedGridEntry;
import gregtech.api.worldgen.generator.GTWorldGenCapability;
import supersymmetry.common.worldgen.GridMasters;

@Mixin(value = CachedGridEntry.class, remap = false)
public abstract class GridMasterMixin {

    @Shadow
    @Final
    private int gridX;
    @Shadow
    @Final
    private int gridZ;

    @Inject(method = "searchMasterOrNull", at = @At("HEAD"), cancellable = true)
    private void readMetadata(World world, CallbackInfoReturnable<GTWorldGenCapability> ci) {
        GridMasters.Lookup lookup = GridMasters.find(world, gridX, gridZ);
        if (lookup.handled) ci.setReturnValue(lookup.value);
    }
}
