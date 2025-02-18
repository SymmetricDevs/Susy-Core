package supersymmetry.common.tileentities;

import net.minecraft.tileentity.TileEntity;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class TileEntityEccentricRoll extends TileEntity implements IAnimatable {

    private final AnimationFactory factory = new AnimationFactory(this);

    public TileEntityEccentricRoll() {
    }

    @SuppressWarnings("deprecation")
    private <E extends TileEntity & IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        event.getController().transitionLengthTicks = 0.0;
        event.getController().setAnimation((new AnimationBuilder()).addAnimation("eccentric_roll.anim.rotate", true));
        return PlayState.CONTINUE;
    }

    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "controller", 0.0F, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }
}
