package supersymmetry.mixins;

import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.Arrays;
import java.util.List;

public class SuSyLateMixinPlugin implements ILateMixinLoader {
    @Override
    public List<String> getMixinConfigs() {
        String[] configs = {"mixins.supersymmetry.late.json"};
        return Arrays.asList(configs);
    }
}
