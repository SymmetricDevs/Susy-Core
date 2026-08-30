package supersymmetry.mixins.visualores;

import java.io.File;
import java.io.IOException;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import lombok.SneakyThrows;
import lombok.val;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import dev.tianmi.sussypatches.api.annotation.Implemented;
import hellfall.visualores.database.gregtech.ore.OreCacheRetrogenerator;
import hellfall.visualores.lib.io.xol.enklume.MinecraftRegion;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Implemented(in = "https://github.com/kumquat-ir/VisualOres/pull/27")
@Mixin(value = OreCacheRetrogenerator.class, remap = false)
public abstract class OreCacheRetrogeneratorMixin {

    @WrapOperation(method = { "doRetrogenV1", "doRetrogenV2" },
              at = @At(value = "NEW",
                       target = "hellfall/visualores/lib/io/xol/enklume/MinecraftRegion"))
    private static MinecraftRegion susy$trackRegion(File regionFile, Operation<MinecraftRegion> constructor,
                                                    @Share("mcRegin") LocalRef<MinecraftRegion> mcRegionRef) {
        val mcRegion = constructor.call(regionFile);
        mcRegionRef.set(mcRegion);
        return mcRegion;
    }

    @SneakyThrows
    @Inject(method = { "doRetrogenV1", "doRetrogenV2" },
              at = @At(value = "INVOKE", target = "Ljava/util/Map;isEmpty()Z"))
    private static void susy$closeTrackedRegion(World world, CallbackInfo ci,
                                                @Share("mcRegin") LocalRef<MinecraftRegion> mcRegionRef) {
        val mcRegion = mcRegionRef.get();
        if (mcRegion != null) mcRegion.close();
    }
}
