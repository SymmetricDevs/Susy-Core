package supersymmetry.mixins;

import java.util.List;
import java.util.stream.Collectors;

import net.minecraftforge.fml.common.Loader;

import com.google.common.collect.ImmutableList;

import zone.rong.mixinbooter.ILateMixinLoader;

@SuppressWarnings("unused")
public class SuSyLateMixinLoader implements ILateMixinLoader {

    public static final List<String> modMixins = ImmutableList.of(
            "bdsandm",
            "gregtech",
            "xnet",
            "travelersbackpack",
            "reccomplex",
            "fluidlogged_api",
            "littletiles",
            "celeritas",
            "projectred-core");

    @Override
    public List<String> getMixinConfigs() {
        return modMixins.stream().map(mod -> "mixins.susy." + mod + ".json").collect(Collectors.toList());
    }

    @Override
    public boolean shouldMixinConfigQueue(String mixinConfig) {
        String[] parts = mixinConfig.split("\\.");
        return parts.length != 4 || shouldEnableModMixin(parts[2]);
    }

    public static boolean shouldEnableModMixin(String mod) {
        return Loader.isModLoaded(mod);
    }
}
