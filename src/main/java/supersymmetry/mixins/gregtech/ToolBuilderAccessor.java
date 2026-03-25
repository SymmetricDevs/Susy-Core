package supersymmetry.mixins.gregtech;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import gregtech.api.items.toolitem.IGTToolDefinition;
import gregtech.api.items.toolitem.ToolBuilder;

@Mixin(value = ToolBuilder.class, remap = false)
public interface ToolBuilderAccessor {

    @Accessor("toolStats")
    IGTToolDefinition getToolStats();
}
