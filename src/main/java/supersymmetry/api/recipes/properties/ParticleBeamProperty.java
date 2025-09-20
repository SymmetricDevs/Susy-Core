package supersymmetry.api.recipes.properties;

import gregtech.api.recipes.recipeproperties.RecipeProperty;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.particle.ParticleBeam;

public class ParticleBeamProperty extends RecipeProperty<ParticleBeam> {

    public static final String KEY = "particle";

    private static ParticleBeamProperty INSTANCE;

    protected ParticleBeamProperty() {
        super(KEY, ParticleBeam.class);
    }

    public static ParticleBeamProperty getInstance() {
        if (INSTANCE == null)
            INSTANCE = new ParticleBeamProperty();
        return INSTANCE;
    }

    @Override
    public void drawInfo(@NotNull Minecraft minecraft, int x, int y, int color, Object value) {

    }
}
