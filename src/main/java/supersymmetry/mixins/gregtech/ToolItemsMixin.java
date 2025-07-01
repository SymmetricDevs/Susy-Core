package supersymmetry.mixins.gregtech;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import gregtech.api.items.toolitem.ItemGTTool;
import gregtech.api.items.toolitem.ToolBuilder;
import gregtech.common.items.ToolItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import supersymmetry.common.item.ToolBehaviorExtender;

@Mixin(value = ToolItems.class, remap = false)
public abstract class ToolItemsMixin {

    @ModifyReceiver(method = "init", at = @At(value = "INVOKE", target = "Lgregtech/api/items/toolitem/ToolBuilder;toolClasses([Ljava/lang/String;)Lgregtech/api/items/toolitem/ToolBuilder;"))
    private static ToolBuilder<ItemGTTool> addPipeNetWalkerBehavior(ToolBuilder<ItemGTTool> toolBuilder, String[] tools) {
        return toolBuilder.toolStats(b -> ToolBehaviorExtender.enqueueBehaviors(b, tools));
    }
}
