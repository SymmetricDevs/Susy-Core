package supersymmetry.common.blocks.active;

import static gregtech.api.util.GTUtility.gregtechId;
import static supersymmetry.api.util.SuSyUtility.susyId;

import net.minecraft.util.ResourceLocation;

public class BlockInvertedActiveEccentricRoll extends BlockActiveEccentricRoll {

    public BlockInvertedActiveEccentricRoll() {
        super(true);
        setTranslationKey("eccentric_roll_active_inverted");
    }

    @Override
    public String getGeoName() {
        return "eccentric_roll_active";
    }

    @Override
    public ResourceLocation modelRL() {
        return susyId("geo/eccentric_roll_active.geo.json");
    }

    @Override
    public ResourceLocation textureRL() {
        return gregtechId("textures/blocks/casings/eccentric_roll_active/all.png");
    }

    @Override
    public ResourceLocation animationRL() {
        return susyId("animations/eccentric_roll_active.animation.json");
    }
}
