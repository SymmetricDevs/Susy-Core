package supersymmetry.common.tileentities;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import supersymmetry.api.blocks.IAnimatablePartBlock;

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

    public IAnimatablePartBlock getPartBlock() {
        if (getBlockType() instanceof IAnimatablePartBlock) {
            return (IAnimatablePartBlock) getBlockType();
        }
        throw new IllegalStateException("Block should implement IAnimatablePart!");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public @NotNull AxisAlignedBB getRenderBoundingBox() {
        return getPartBlock().getRenderBoundingBox(getWorld(), getPos(), getBlockMetadata());
    }
}
