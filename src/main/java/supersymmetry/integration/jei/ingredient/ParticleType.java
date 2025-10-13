package supersymmetry.integration.jei.ingredient;

import mezz.jei.api.recipe.IIngredientType;
import supersymmetry.api.particle.ParticleBeam;

public final class ParticleType {
    public static final IIngredientType<ParticleBeam> Particle = () -> ParticleBeam.class;
    private ParticleType() {

    }
}
