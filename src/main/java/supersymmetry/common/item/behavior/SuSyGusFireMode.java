package supersymmetry.common.item.behavior;

public enum GunFireMode {

    BOLT_ACTION(false),
    SEMI_AUTO(false),
    FULL_AUTO(true),
    BURST(false);

    private final boolean repeat;

    GunFireMode(boolean repeat) {
        this.repeat = repeat;
    }

    public boolean repeat() {
        return repeat;
    }
}