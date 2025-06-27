package supersymmetry.api.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.util.ResourceLocation;

import static gregtech.api.util.GTUtility.gregtechId;
import static supersymmetry.api.util.SuSyUtility.susyId;

public interface IAnimatablePart<T extends Block & ITileEntityProvider> extends ITileEntityProvider {

    PropertyBool ACTIVE = PropertyBool.create("active");

    @SuppressWarnings("unchecked")
    default T thisObject() {
        return (T) this;
    }

    default String getGeoName() {
        return thisObject().translationKey;
    }

    default ResourceLocation modelRL() {
        return susyId("geo/" + getGeoName() + ".geo.json");
    }

    default ResourceLocation textureRL() {
        return gregtechId("textures/blocks/casings/" + getGeoName() + "/all.png");
    }

    default ResourceLocation animationRL() {
        return susyId("animations/" + getGeoName() + ".animation.json");
    }
}
