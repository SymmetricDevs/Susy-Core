package supersymmetry.common.util;

public class FactionHelper {

    public static final String[] FACTIONS = new String[] {
            "Bandits",
            "Feds" //add more later, maybe techbros idk
    };

    public static String getNextFaction(String current) {
        for (int i = 0; i < FACTIONS.length; i++) {
            if (FACTIONS[i].equals(current)) {
                return FACTIONS[(i + 1) % FACTIONS.length];
            }
        }
        return FACTIONS[0];
    }
}
