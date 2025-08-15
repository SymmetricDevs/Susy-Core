package supersymmetry.api.util;
import net.minecraft.util.DamageSource;

public class SuSyDamageSources {
    private static final DamageSource SUFFOCATION = new DamageSource("suffocation").setDamageBypassesArmor();
    private static final DamageSource TOXIC_ATMO = new DamageSource("toxic_atmo").setDamageBypassesArmor();

    private static final DamageSource CRUSHER = new DamageSource("crusher");

    public static DamageSource getSuffocationDamage() {
        return SUFFOCATION;
    }

    public static DamageSource getToxicAtmoDamage() {
        return TOXIC_ATMO;
    }

    public static DamageSource getCrusherDamage() {
        return CRUSHER;
    }
}
