package supersymmetry;

import net.minecraftforge.common.config.Config;

@Config(modid = Supersymmetry.MODID, name = Supersymmetry.MODID + "/" + Supersymmetry.MODID)
public class SusyConfig {

    @Config.Comment({ "Whether or not to disable Long-Distance Item Pipe recipes.",
            "Default: true" })
    @Config.RequiresMcRestart
    public static boolean disableLdItemPipes = true;
}
