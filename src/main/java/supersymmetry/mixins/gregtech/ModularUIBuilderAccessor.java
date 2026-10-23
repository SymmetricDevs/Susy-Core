package supersymmetry.mixins.gregtech;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import gregtech.api.gui.ModularUI;

@Mixin(value = ModularUI.Builder.class, remap = false)
public interface ModularUIBuilderAccessor {

    @Mutable
    @Accessor("height")
    void susy$setHeight(int height);
}
