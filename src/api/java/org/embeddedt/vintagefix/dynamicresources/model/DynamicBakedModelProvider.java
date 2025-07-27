package org.embeddedt.vintagefix.dynamicresources.model;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.registry.RegistrySimple;
import org.jetbrains.annotations.NotNull;

/// Adapted and minimized from
/// [VintageFix](https://github.com/embeddedt/VintageFix/blob/main/src/main/java/org/embeddedt/vintagefix/dynamicresources/model/DynamicBakedModelProvider.java)
public class DynamicBakedModelProvider extends RegistrySimple<ModelResourceLocation, IBakedModel> {
    public static DynamicBakedModelProvider instance;

    @Override
    public void putObject(@NotNull ModelResourceLocation key, @NotNull IBakedModel value) {
        /* Method Body */
    }
}
