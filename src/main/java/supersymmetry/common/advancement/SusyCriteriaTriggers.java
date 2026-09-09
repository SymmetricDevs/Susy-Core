package supersymmetry.common.advancement;

import net.minecraft.advancements.CriteriaTriggers;

public class SusyCriteriaTriggers {

    public static final RocketLaunchTrigger ROCKET_LAUNCH = new RocketLaunchTrigger();

    public static void init() {
        CriteriaTriggers.register(ROCKET_LAUNCH);
    }
}
