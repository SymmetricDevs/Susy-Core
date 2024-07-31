package supersymmetry.mixins;

import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public class SuSyLateMixinLoader implements ILateMixinLoader {

    @Override
    public List<String> getMixinConfigs() {
        String[] configs = {"mixins.susy.late.json"};
        return Arrays.asList(configs);
    }
}
