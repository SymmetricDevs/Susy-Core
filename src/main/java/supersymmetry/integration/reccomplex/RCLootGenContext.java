package supersymmetry.integration.reccomplex;

public class RCLootGenContext {
    public static final ThreadLocal<Boolean> STRUCTURE_GEN_RUNNING = ThreadLocal.withInitial(() -> false);
}
