package supersymmetry.common.faction;

import net.minecraft.world.World;

public class FactionViolenceManager {

    private static final String RULE_NAME = "factionViolence";

    public static boolean isEnabled(World world) {
        if (world == null) return false;
        return world.getGameRules().getBoolean(RULE_NAME);
    }

    public static void setEnabled(World world, boolean enabled) {
        if (world == null) return;
        world.getGameRules().setOrCreateGameRule(RULE_NAME, Boolean.toString(enabled));
    }

    public static void toggle(World world) {
        if (world == null) return;
        boolean current = isEnabled(world);
        setEnabled(world, !current);
    }
}
