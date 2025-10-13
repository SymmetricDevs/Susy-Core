package supersymmetry.integration.jei.ingredient;

import com.google.common.base.MoreObjects;
import mezz.jei.api.ingredients.IIngredientHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supersymmetry.Supersymmetry;
import supersymmetry.api.particle.Particle;
import supersymmetry.api.particle.ParticleBeam;

public class ParticleBeamHelper implements IIngredientHelper<ParticleBeam> {
    @Nullable
    @Override
    public ParticleBeam getMatch(Iterable<ParticleBeam> ingredients, @NotNull ParticleBeam ingredientToMatch) {

        for (ParticleBeam particleBeam : ingredients) {
            if (particleBeam == null) continue;
            if (ingredientToMatch.getParticle() == particleBeam.getParticle()) {
                return particleBeam;
            }
        }
        return null;
    }

    @Override
    public String getDisplayName(ParticleBeam ingredient) {
        return ingredient.getParticle().getUnlocalizedName();
    }

    @Override
    public String getUniqueId(ParticleBeam ingredient) {
        return "particle:" + ingredient.getParticle().getName();
    }

    @Override
    public String getWildcardId(ParticleBeam ingredient) {
        return getUniqueId(ingredient);
    }

    @Override
    public String getModId(ParticleBeam ingredient) {
        return Supersymmetry.MODID;
    }

    @Override
    public String getResourceId(ParticleBeam ingredient) {
        return ingredient.getParticle().getTexture().getPath();
    }

    @Override
    public ParticleBeam copyIngredient(ParticleBeam ingredient) {
        return ingredient.copy();
    }

    @Override
    public String getErrorInfo(@Nullable ParticleBeam ingredient) {
        if (ingredient == null) {
            return null;
        }
        MoreObjects.ToStringHelper toStringHelper = MoreObjects.toStringHelper(ParticleBeam.class);
        Particle particle = ingredient.getParticle();
        if (particle != null) {
            toStringHelper.add("Particle", particle.getName());
        } else {
            toStringHelper.add("Particle", "null");
        }
        toStringHelper.add("energy", ingredient.getEnergy());
        toStringHelper.add("bunchSpacing", ingredient.getBunchSpacing());
        toStringHelper.add("bunchLength", ingredient.getBunchLength());
        toStringHelper.add("nBunches", ingredient.getNBunches());
        toStringHelper.add("nParticlesPerBunch", ingredient.getNParticlesPerBunch());
        toStringHelper.add("emittance", ingredient.getEmittance());
        toStringHelper.add("beamSize", ingredient.getBeamSize());
        return toStringHelper.toString();
    }
}
