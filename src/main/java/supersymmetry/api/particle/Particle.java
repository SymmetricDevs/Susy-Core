package supersymmetry.api.particle;

import net.minecraft.util.ResourceLocation;
import supersymmetry.Supersymmetry;

import java.util.HashMap;

public class Particle {

    private final String name;
    private final double mass; //Thou shall enter the mass in MeV/c^2, or thy skin shall be forfeit
    private final double charge; //Thou shall enter the charge in multiples of e, or thy teeth shall fall
    private final double spin; //Thou shall enter the spin in multiples of hbar, or thy nails shall commence growing inwards
    private final double width; //Thou shall enter the width in MeV/c^2, or thy thyroid shall wither
    private final boolean coloured; //Thou shall worship the boiling madness at the base of the universe, they are just colours...but they burn
    private final boolean weakInt; //Thou shall be saved from having to learn weak hypercharge and weak isospin, for I am merciful
    private Particle antiParticle = this;
    private HashMap<Particle, Integer> components = new HashMap<Particle, Integer>();
    private final ResourceLocation texture;

    /**
     * @param name Name of the particle
     * @param mass Mass of the particle in MeV/c^2
     * @param charge Electric charge of the particle as multiples of the charge of the proton
     * @param spin Spin of the particle in units of hbar, for more information on what spin is please complete a undergrad quantum mechanics course
     * @param width Mass width of the particle, this controls the mean lifetime, stable particles have a width of zero
     * @param coloured Whether the particle has colour charge or not
     * @param weakInt Whether the particle will have weak interactions or not
     * @param texture Texture location
     */
    public Particle(String name, double mass, double charge, double spin, double width, boolean coloured, boolean weakInt, ResourceLocation texture) {
        this.name = name;
        this.mass = mass;
        this.charge = charge;
        this.spin = spin;
        this.width = width;
        this.coloured = coloured;
        this.weakInt = weakInt;
        this.texture = texture;
    }

    public String getName() {
        return name;
    }

    public double getMass() {
        return mass;
    }

    public double getCharge() {
        return charge;
    }

    public double getSpin() {
        return spin;
    }

    public double getWidth() {
        return width;
    }

    public boolean isColoured() {
        return coloured;
    }

    public boolean isWeakInt() {
        return weakInt;
    }

    public boolean hasEMInteraction() {
        return charge != 0;
    }

    /**
     * @return Mean lifetime of the particle in nanoseconds
     */
    public double getMeanLifetime() {
        return (6.582119565e-13)/getWidth();
    }

    public void setAntiParticle(Particle particle) {
        this.antiParticle = particle;
        particle.antiParticle = this;
    }

    public Particle getAntiParticle() {
        return antiParticle;
    }

    public boolean isFundamental() {
        return components.isEmpty();
    }

    public void setComponents(HashMap<Particle, Integer> components) {
        this.components = components;
    }

    public void setComponent(Particle particle, int amount) {
        if (components.containsKey(particle)) {
            components.replace(particle, amount);
        } else {
            components.put(particle, amount);
        }
    }

    public void addComponent(Particle particle, int amount) {
        if (components.containsKey(particle)) {
            components.replace(particle, components.get(particle) + amount);
        } else {
            components.put(particle, amount);
        }
    }

    public  HashMap<Particle, Integer> getComponents() {
        return components;
    }

    public String getUnlocalizedName() {
        return Supersymmetry.MODID + ".particle." + name + ".name";
    }

    public ResourceLocation getTexture() {
        return texture;
    }

}
