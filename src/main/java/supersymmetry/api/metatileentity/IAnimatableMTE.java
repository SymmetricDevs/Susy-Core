package supersymmetry.api.metatileentity;

import gregtech.api.metatileentity.IFastRenderMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import software.bernie.geckolib3.core.IAnimatable;
import supersymmetry.client.renderer.handler.GeoMTERenderer;

import static supersymmetry.api.util.SuSyUtility.susyId;

public interface IAnimatableMTE extends IFastRenderMetaTileEntity, IAnimatable {

    @SuppressWarnings("unchecked")
    default <T extends MetaTileEntity> T thisObject() {
        return (T) this;
    }

    default String getGeoName() {
        return thisObject().metaTileEntityId.getPath();
    }

    default ResourceLocation modelRL() {
        return susyId("geo/" + getGeoName() + ".geo.json");
    }

    default ResourceLocation textureRL() {
        return susyId("textures/geo/" + getGeoName() + "/all.png");
    }

    default ResourceLocation animationRL() {
        return susyId("animations/" + getGeoName() + ".animation.json");
    }

    default Vec3i getTransformation() {
        return new Vec3i(0, 0, 0);
    }

    default BlockPos getLightPos() {
        return thisObject().getPos();
    }

    // If this returns true, the TESR will keep rendering even when the chunk is culled.
    @Override
    default boolean isGlobalRenderer() {
        return true;
    }

    @Override
    default void renderMetaTileEntity(double x, double y, double z, float partialTicks) {
        if (thisObject().getWorld() == Minecraft.getMinecraft().world) {
            GeoMTERenderer.INSTANCE.render(thisObject(), x, y, z, partialTicks);
        }
    }
}
