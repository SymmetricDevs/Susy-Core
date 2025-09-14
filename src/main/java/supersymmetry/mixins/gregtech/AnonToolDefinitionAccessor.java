package supersymmetry.mixins.gregtech;

import gregtech.api.items.toolitem.ToolDefinitionBuilder;
import gregtech.api.items.toolitem.behavior.IToolBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/// @see ToolDefinitionBuilder
@Mixin(targets = "gregtech.api.items.toolitem.ToolDefinitionBuilder$1", remap = false)
public interface AnonToolDefinitionAccessor {
    @Mutable
    @Accessor("behaviors")
    void setBehaviors(List<IToolBehavior> behaviors);

}
