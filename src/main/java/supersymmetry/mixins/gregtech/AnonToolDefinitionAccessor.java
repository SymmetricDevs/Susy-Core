package supersymmetry.mixins.gregtech;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import gregtech.api.items.toolitem.behavior.IToolBehavior;

/// @see ToolDefinitionBuilder
@Mixin(targets = "gregtech.api.items.toolitem.ToolDefinitionBuilder$1", remap = false)
public interface AnonToolDefinitionAccessor {

    @Mutable
    @Accessor("behaviors")
    void setBehaviors(List<IToolBehavior> behaviors);
}
