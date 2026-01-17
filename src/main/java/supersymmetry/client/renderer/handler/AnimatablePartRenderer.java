package supersymmetry.client.renderer.handler;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import software.bernie.geckolib3.model.AnimatedGeoModel;
import supersymmetry.common.tileentities.AnimatablePartTileEntity;

@SideOnly(Side.CLIENT)
public class AnimatablePartRenderer extends FixedGeoBlockRenderer<AnimatablePartTileEntity> {

    public AnimatablePartRenderer() {
        super(new ModelDispatcher());
    }

    public static class ModelDispatcher extends AnimatedGeoModel<AnimatablePartTileEntity> {

        @Override
        public ResourceLocation getModelLocation(AnimatablePartTileEntity animatablePart) {
            return animatablePart.getPartBlock().modelRL();
        }

        @Override
        public ResourceLocation getTextureLocation(AnimatablePartTileEntity animatablePart) {
            return animatablePart.getPartBlock().textureRL();
        }

        @Override
        public ResourceLocation getAnimationFileLocation(AnimatablePartTileEntity animatablePart) {
            return animatablePart.getPartBlock().animationRL();
        }
    }
}
