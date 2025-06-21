package supersymmetry.api.damagesources;

import net.minecraft.util.DamageSource;

public class DamageSources {

    public static final DamageSource PRESSURE = new DamageSource("pressure");
    public static final DamageSource DEPRESSURIZATION = new DamageSource("depressurization");
    public static final DamageSource IMPACT = new DamageSource("impact");
    public static final DamageSource VAPORIZATION = new DamageSource("vaporization").setFireDamage();
    public static final DamageSource ALPHA_RADIATION = new DamageSource("alpha_radiation");
    public static final DamageSource BETA_RADIATION = new DamageSource("beta_radiation");
    public static final DamageSource NEUTRON_RADIATION = new DamageSource("neutron_radiation");
    public static final DamageSource UV_RADIATION = new DamageSource("un_radiation");
    public static final DamageSource X_RADIATION = new DamageSource("x_radiation");
    public static final DamageSource GAMMA_RADIATION = new DamageSource("gamma_radiation");
    public static final DamageSource SPAGHETTIFICATION = new DamageSource("spaghettification");
    public static final DamageSource AGE = new DamageSource("age");
    public static final DamageSource CHRONOERASURE = new DamageSource("chronoerasure");

}
