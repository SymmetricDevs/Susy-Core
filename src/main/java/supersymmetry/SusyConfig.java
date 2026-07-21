package supersymmetry;

import net.minecraftforge.common.config.Config;

@Config(modid = Supersymmetry.MODID, name = Supersymmetry.MODID + "/" + Supersymmetry.MODID)
public class SusyConfig {

    @Config.Comment({ "Whether or not to disable Long-Distance Item Pipe recipes.",
            "Default: true" })
    @Config.RequiresMcRestart
    public static boolean disableLdItemPipes = true;

    @Config.Comment({ "Ban certain items from space." })
    @Config.RequiresMcRestart
    public static String[] bannedSpaceItems = {};

    @Config.Comment({ "Minimum sample rate for speaker audio playback in Hz.", "Default: 128" })
    @Config.RangeInt(min = 0)
    public static int speakerMinRate = 128;

    @Config.Comment({ "Maxi0um sample rate for speaker audio playback in Hz.", "Default: 22050" })
    @Config.RangeInt(min = 1)
    public static int speakerMaxRate = 22050;

    @Config.Comment({ "Maximum duration of a single speaker audio chunk in seconds.", "Default: 2.0" })
    @Config.RangeDouble(min = 0.05)
    public static double speakerMaxDuration = 2.0;

    @Config.Comment({ "Minimum duration of a single speaker audio chunk in seconds.", "Default: 0.03" })
    @Config.RangeDouble(min = 0.0)
    public static double speakerMinDuration = 0.03;
}
