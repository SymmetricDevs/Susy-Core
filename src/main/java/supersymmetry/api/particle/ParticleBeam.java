package supersymmetry.api.particle;

import net.minecraft.nbt.NBTTagCompound;

public class ParticleBeam {

    private Particle particle;
    private double energy;
    private double bunchSpacing;
    private double bunchLength;
    private int nBunches;
    private int nParticlesPerBunch;
    private double emittance;
    private double beamSize;

    /**
     * @param particle           Particles in the beam
     * @param energy             The mean energy of the beam in MeV
     * @param bunchSpacing       Distance in nanoseconds between particle bunches
     * @param bunchLength        The length of a particle bunch in the lab frame
     * @param nBunches           Number of bunches in the beam
     * @param nParticlesPerBunch Number of particles in each bunch
     * @param emittance          Emittance of the beam
     * @param beamSize           Transversal cross-section of the beam in micrometers
     */
    public ParticleBeam(Particle particle, double energy, double bunchSpacing, double bunchLength, int nBunches,
                        int nParticlesPerBunch, double emittance, double beamSize) {
        this.particle = particle;
        this.energy = energy;
        this.bunchSpacing = bunchSpacing;
        this.bunchLength = bunchLength;
        this.nBunches = nBunches;
        this.nParticlesPerBunch = nParticlesPerBunch;
        this.emittance = emittance;
        this.beamSize = beamSize;
    }

    public ParticleBeam copy() {
        return new ParticleBeam(particle, energy, bunchSpacing, bunchLength, nBunches, nParticlesPerBunch, emittance,
                beamSize);
    }

    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        if (particle != null) {
            data.setString("particle", particle.getName());
        }
        data.setDouble("energy", energy);
        data.setDouble("bunchSpacing", bunchSpacing);
        data.setDouble("bunchLength", bunchLength);
        data.setInteger("nBunches", nBunches);
        data.setInteger("nParticlesPerBunch", nParticlesPerBunch);
        data.setDouble("emittance", emittance);
        data.setDouble("beamSize", beamSize);
        return data;
    }

    public void readFromNBT(NBTTagCompound data) {
        if (data.hasKey("particle")) {
            this.particle = Particles.getByName(data.getString("particle"));
        } else {
            this.particle = null;
        }
        this.energy = data.getDouble("energy");
        this.bunchSpacing = data.getDouble("bunchSpacing");
        this.bunchLength = data.getDouble("bunchLength");
        this.nBunches = data.getInteger("nBunches");
        this.nParticlesPerBunch = data.getInteger("nParticlesPerBunch");
        this.emittance = data.getDouble("emittance");
        this.beamSize = data.getDouble("beamSize");
    }

    public Particle getParticle() {
        return particle;
    }

    public void setParticle(Particle particle) {
        this.particle = particle;
    }

    public double getEnergy() {
        return energy;
    }

    public void setEnergy(double energy) {
        this.energy = energy;
    }

    public double getBunchSpacing() {
        return bunchSpacing;
    }

    public void setBunchSpacing(double bunchSpacing) {
        this.bunchSpacing = bunchSpacing;
    }

    public double getBunchLength() {
        return bunchLength;
    }

    public void setBunchLength(double bunchLength) {
        this.bunchLength = bunchLength;
    }

    public int getNBunches() {
        return nBunches;
    }

    public void setNBunches(int nBunches) {
        this.nBunches = nBunches;
    }

    public int getNParticlesPerBunch() {
        return nParticlesPerBunch;
    }

    public void setNParticlesPerBunch(int nParticlesPerBunch) {
        this.nParticlesPerBunch = nParticlesPerBunch;
    }

    public double getEmittance() {
        return emittance;
    }

    public void setEmittance(double emittance) {
        this.emittance = emittance;
    }

    public double getBeamSize() {
        return beamSize;
    }

    public void setBeamSize(double beamSize) {
        this.beamSize = beamSize;
    }
}
