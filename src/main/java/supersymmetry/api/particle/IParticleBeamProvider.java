package supersymmetry.api.particle;

public interface IParticleBeamProvider {

    ParticleBeam getParticleBeam();

    ParticleBeam insertBeam(ParticleBeam beam);

}
