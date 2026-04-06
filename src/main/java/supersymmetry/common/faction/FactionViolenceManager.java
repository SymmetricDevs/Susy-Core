package supersymmetry.common.faction;

public class FactionViolenceManager {

    private static boolean violenceEnabled = false;

    public static boolean isEnabled() {
        return violenceEnabled;
    }

    public static void setEnabled(boolean enabled) {
        violenceEnabled = enabled;
    }

    public static void toggle() {
        violenceEnabled = !violenceEnabled;
    }
}
