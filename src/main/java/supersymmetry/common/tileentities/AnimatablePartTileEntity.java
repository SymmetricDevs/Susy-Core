package supersymmetry.common.tileentities;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.Animation;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.file.AnimationFile;
import software.bernie.geckolib3.resource.GeckoLibCache;
import supersymmetry.api.blocks.IAnimatablePartBlock;

public class AnimatablePartTileEntity extends TileEntity implements IAnimatable {

    private final AnimationFactory factory;

    public AnimatablePartTileEntity() {
        this.factory = new AnimationFactory(this);
    }

    @SuppressWarnings("unchecked")
    private <E extends TileEntity & IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        if (world == null) return PlayState.STOP;

        if (getBlockType() instanceof IAnimatablePartBlock) {
            net.minecraft.block.state.IBlockState state = world.getBlockState(pos);
            if (state.getProperties().containsKey(IAnimatablePartBlock.ACTIVE) &&
                    !state.getValue(IAnimatablePartBlock.ACTIVE)) {
                return PlayState.STOP;
            }
            if (getBlockType() instanceof supersymmetry.common.blocks.RedstoneActiveBlock) {
                if (!((supersymmetry.common.blocks.RedstoneActiveBlock<?>) getBlockType()).isEffectActive(state)) {
                    return PlayState.STOP;
                }
            }
        }

        event.getController().transitionLengthTicks = 0.0;
        event.getController()
                .setAnimation((new AnimationBuilder()).addAnimation("default_loop", ILoopType.EDefaultLoopTypes.LOOP));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "controller", 0.0F, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    public IAnimatablePartBlock getPartBlock() {
        if (getBlockType() instanceof IAnimatablePartBlock) {
            return (IAnimatablePartBlock) getBlockType();
        }
        throw new IllegalStateException("Block should implement IAnimatablePart!");
    }

    @SideOnly(Side.CLIENT)
    public void restoreAnimationPhase(Integer uniqueID, float partialTicks) {
        if (world == null) return;

        AnimationData data = factory.getOrCreateAnimationData(uniqueID);
        AnimationController<?> controller = data.getAnimationControllers().get("controller");
        if (controller == null) return;

        ResourceLocation animRL = getPartBlock().animationRL();
        if (animRL == null) return;

        AnimationFile animFile = GeckoLibCache.getInstance().getAnimations().get(animRL);
        if (animFile == null) return;

        Animation anim = animFile.getAnimation("default_loop");
        if (anim == null || anim.animationLength == null || anim.animationLength <= 0) return;

        double animLength = anim.animationLength;
        double seekTime = data.tick + partialTicks;
        double correctFrame = data.tick % animLength;

        controller.tickOffset = seekTime - correctFrame;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public @NotNull AxisAlignedBB getRenderBoundingBox() {
        return getPartBlock().getRenderBoundingBox(getWorld(), getPos(), getBlockMetadata());
    }
}
