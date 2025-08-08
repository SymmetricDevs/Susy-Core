package supersymmetry.api.items;

import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.core.IAnimatable;

import static supersymmetry.api.util.SuSyUtility.susyId;

public interface IGeoMetaArmor extends IAnimatable {

    String getGeoName();

    default ResourceLocation modelRL() {
        return susyId("geo/" + getGeoName() + ".geo.json");
    }

    default ResourceLocation textureRL() {
        return susyId("textures/geo/" + getGeoName() + "/all.png");
    }

    default ResourceLocation animationRL() {
        return susyId("animations/" + getGeoName() + ".animation.json");
    }
}
