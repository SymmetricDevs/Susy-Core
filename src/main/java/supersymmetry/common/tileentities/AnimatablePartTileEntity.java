package supersymmetry.common.tileentities;

import net.minecraft.tileentity.TileEntity;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import supersymmetry.api.blocks.IAnimatablePart;

public class AnimatablePartTileEntity extends TileEntity implements IAnimatable {

    private final AnimationFactory factory;

    public AnimatablePartTileEntity() {
        this.factory = new AnimationFactory(this);
    }

    private <E extends TileEntity & IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        event.getController().transitionLengthTicks = 0.0;
        event.getController().setAnimation((new AnimationBuilder())
                .addAnimation("default_loop", ILoopType.EDefaultLoopTypes.LOOP));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this,
                "controller", 0.0F, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    public IAnimatablePart<?> getAnimatablePart() {
        if (getBlockType() instanceof IAnimatablePart<?>) {
            return (IAnimatablePart<?>) getBlockType();
        }
        throw new IllegalStateException("Block should implement IAnimatablePart!");
    }
}
