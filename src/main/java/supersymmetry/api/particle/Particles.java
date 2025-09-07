package supersymmetry.api.particle;

import supersymmetry.api.SusyLog;

import java.util.HashMap;
import java.util.Map;

public class Particles {

    public static final Map<String, Particle> particleRegistry = new HashMap<String, Particle>();

    public static Particle electron;

    public static Particle positron;

    public void init() {

        electron = new Particle("electron", 0.511, -1, 0.5, 0., false, true);

        positron = new Particle("positron", 0.511, 1, 0.5, 0., false, true);

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

}
