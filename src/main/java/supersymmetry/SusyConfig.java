package supersymmetry;

import net.minecraftforge.common.config.Config;

@Config(modid = Supersymmetry.MODID, name = Supersymmetry.MODID + "/" + Supersymmetry.MODID)
public class SusyConfig {

    @Config.Name("worldgen")
    @Config.Comment("World generation and structure placement fixes.")
    @Config.RequiresMcRestart
    public static WorldGeneration worldGeneration = new WorldGeneration();

    public static class WorldGeneration {

        @Config.Comment("Enable world generation fixes.")
        @Config.RequiresMcRestart
        public boolean enabled = true;

        @Config.Comment("Read saved ore-grid heights without loading the corresponding chunks.")
        @Config.RequiresMcRestart
        public boolean gridMetadata = true;

        @Config.Comment("Defer cross-chunk ore indicators until their destination is loaded and populated.")
        @Config.RequiresMcRestart
        public boolean deferIndicators = true;

        @Config.Comment("Use transformed destination coordinates for Recurrent Complex placement checks.")
        @Config.RequiresMcRestart
        public boolean recurrentComplex = true;

        @Config.Comment("Allocate structure-cache columns lazily and promote dense workloads to flat storage.")
        @Config.RequiresMcRestart
        public boolean adaptiveCache = true;
    }

    @Config.Comment({ "Whether or not to disable Long-Distance Item Pipe recipes.", "Default: true" })
    @Config.RequiresMcRestart
    public static boolean disableLdItemPipes = true;

    @Config.Comment({ "Ban certain items from space." })
    @Config.RequiresMcRestart
    public static String[] bannedSpaceItems = {};

    @Config.Comment({ "Minimum sample rate for speaker audio playback in Hz.", "Default: 128" })
    @Config.RangeInt(min = 0)
    public static int speakerMinRate = 128;

    @Config.Comment({ "Maximum sample rate for speaker audio playback in Hz.", "Default: 22050" })
    @Config.RangeInt(min = 1)
    public static int speakerMaxRate = 22050;

    @Config.Comment({ "Maximum duration of a single speaker audio chunk in seconds.", "Default: 2.0" })
    @Config.RangeDouble(min = 0.05)
    public static double speakerMaxDuration = 2.0;

    @Config.Comment({ "Minimum duration of a single speaker audio chunk in seconds.", "Default: 0.03" })
    @Config.RangeDouble(min = 0.0)
    public static double speakerMinDuration = 0.03;

    @Config.Comment({
            "Enable missing model generation. Disable vintage fix and UT fancy missing models for this to work.",
            "Default: false" })
    @Config.RequiresMcRestart
    public static boolean enableMissingModelGen = false;
}
