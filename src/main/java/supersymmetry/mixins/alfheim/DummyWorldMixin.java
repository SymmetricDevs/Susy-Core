package supersymmetry.mixins.alfheim;

import dev.redstudio.alfheim.lighting.LightingEngine;
import gregtech.api.util.Mods;
import gregtech.api.util.world.DummyWorld;
import net.minecraftforge.fml.common.Optional;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.SoftOverride;

/// Ported from [CEu#2838](https://github.com/GregTechCEu/GregTech/pull/2838)
@Deprecated
@ApiStatus.ScheduledForRemoval(inVersion = "Next CEu update")
@Mixin(value = DummyWorld.class, remap = false)
public class DummyWorldMixin {

    @SoftOverride
    @Optional.Method(modid = Mods.Names.ALFHEIM)
    public LightingEngine getAlfheim$lightingEngine() {
        return null;
    }
}
