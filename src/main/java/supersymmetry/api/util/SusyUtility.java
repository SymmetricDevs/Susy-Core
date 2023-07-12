package supersymmetry.api.util;

import net.minecraft.util.ResourceLocation;
import supersymmetry.Supersymmetry;

import javax.annotation.Nonnull;

public class SusyUtility {

    @Nonnull
    public static ResourceLocation susyId(@Nonnull String path) {
        return new ResourceLocation(Supersymmetry.MODID, path);
    }

}
