package supersymmetry.api.particle;

import net.minecraft.util.ResourceLocation;
import supersymmetry.api.SusyLog;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import static supersymmetry.api.util.SuSyUtility.susyId;

public class Particles {

    public static final Map<String, Particle> particleRegistry = new HashMap<String, Particle>();

    public static Particle electron;

    public static Particle positron;

    public void init() {

        electron = new Particle("electron", 0.511, -1, 0.5, 0., false, true, susyId("particles/electron.png"));

        positron = new Particle("positron", 0.511, 1, 0.5, 0., false, true, susyId("particles/positron.png"));

        electron.setAntiParticle(positron);

    }

    public static void register() {
        registerParticle(electron);
        registerParticle(positron);
    }

    public static void registerParticle(Particle particle) {
        if (!particleRegistry.containsKey(particle.getName())) {
            particleRegistry.put(particle.getName(), particle);
        } else {
            SusyLog.logger.error("Particle " + particle.getName() + " cannot be registered as it already exists");
        }
    }

    public static Particle getByName(String name) {
        if (name != null) {
            if (particleRegistry.containsKey(name)) {
                return particleRegistry.get(name);
            } else {
                SusyLog.logger.error("Particle " + name + " does not exist");
            }
        }
        return null;
    }

    /**
     * @param particle The particle you want the antiparticle of, must be a composite particle, fundamental antiparticles must be generated manually
     * @param name The name of the antiparticle you want, another method is available that will generate it automatically
     * @param location Texture location
     * @return The desired antiparticle
     */
    @Nullable
    public static Particle makeAntiParticle(Particle particle, String name, ResourceLocation location) {
        if (particle.isFundamental() || particle.getAntiParticle() == particle) return null;
        Particle antiParticle = new Particle(name, particle.getMass(), -particle.getCharge(), particle.getSpin(), particle.getWidth(), particle.isColoured(), particle.isWeakInt(), location);
        for (Map.Entry<Particle, Integer> component : particle.getComponents().entrySet()) {
            antiParticle.setComponent(component.getKey().getAntiParticle(), component.getValue());
        }
        return antiParticle;
    }

    /**
     * @param particle The particle you want the antiparticle of, must be a composite particle, fundamental antiparticles must be generated manually
     * @param location Texture location
     * @return The desired antiparticle
     */
    @Nullable
    public static Particle makeAntiParticle(Particle particle, ResourceLocation location) {
        return makeAntiParticle(particle, "anti" + particle.getName(), location);
    }

}
