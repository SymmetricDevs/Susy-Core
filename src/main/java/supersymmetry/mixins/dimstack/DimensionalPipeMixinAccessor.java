package supersymmetry.mixins.dimstack;

import net.minecraft.util.EnumFacing;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import cd4017be.dimstack.tileentity.DimensionalPipe;

@Mixin(value = DimensionalPipe.class, remap = false)
public interface DimensionalPipeMixinAccessor {

    @Accessor
    EnumFacing getSide();
}

