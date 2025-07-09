package supersymmetry.mixins.gregtech;

import com.google.common.collect.Lists;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import gregtech.api.items.toolitem.ItemGTTool;
import gregtech.api.items.toolitem.ToolBuilder;
import gregtech.common.items.ToolItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import supersymmetry.common.item.ToolBehaviorExtender;

@Mixin(value = ToolItems.class, remap = false)
public abstract class ToolItemsMixin {

    @WrapOperation(
            method = "init",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lgregtech/api/items/toolitem/ToolBuilder;toolClasses([Ljava/lang/String;)Lgregtech/api/items/toolitem/ToolBuilder;"))
    private static ToolBuilder<ItemGTTool> addPipeNetWalkerBehavior(
            ToolBuilder<ItemGTTool> toolBuilder, String[] tools, Operation<ToolBuilder<ItemGTTool>> method) {
        var definition = ((ToolBuilderAccessor) toolBuilder).getToolStats();
        var newBehaviors = Lists.newArrayList(definition.getBehaviors());

        ToolBehaviorExtender.registerExtra(newBehaviors, tools);
        ((AnonToolDefinitionAccessor) definition).setBehaviors(newBehaviors);

        return method.call(toolBuilder, tools);
    }
}
