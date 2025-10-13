package supersymmetry.integration.jei.ingredient;

import supersymmetry.api.particle.Particle;
import supersymmetry.api.particle.ParticleBeam;
import supersymmetry.api.particle.Particles;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ParticleBeamListFactory {
    private ParticleBeamListFactory() {}

    public static List<ParticleBeam> createList() {
        List<ParticleBeam> particleBeams = new ArrayList<>();
        Map<String, Particle> registeredParticles = Particles.particleRegistry;
        for (Particle particle : registeredParticles.values()) {
            ParticleBeam particleBeam = new ParticleBeam(particle, 0., 0., 0., 0, 0, 0., 0.);
            particleBeams.add(particleBeam);
        }
        return particleBeams;
    }
}
