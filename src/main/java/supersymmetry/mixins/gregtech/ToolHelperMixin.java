package supersymmetry.mixins.gregtech;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import gregtech.api.items.toolitem.ToolHelper;
import supersymmetry.common.item.ItemToolbelt;

/**
 * Substitutes the selected tool for a toolbelt stack at the head of GT's static tool helpers,
 * the same way GTCEu-Reloaded's ToolHelper.toolbeltPassthrough does. This is what makes AoE
 * work: getMaxAoEDefinition reads the behaviours tag of the stack it is given.
 */
@Mixin(value = ToolHelper.class, remap = false)
public class ToolHelperMixin {

    private static final ThreadLocal<ItemStack> SUSY_BELT = new ThreadLocal<>();

    @ModifyVariable(method = "getBehaviorsTag", at = @At("HEAD"), argsOnly = true, index = 0)
    private static ItemStack susy$behaviorsTag(ItemStack stack) {
        return ItemToolbelt.passthrough(stack);
    }

    @ModifyVariable(method = "getToolTag", at = @At("HEAD"), argsOnly = true, index = 0)
    private static ItemStack susy$toolTag(ItemStack stack) {
        return ItemToolbelt.passthrough(stack);
    }

    @ModifyVariable(method = "areaOfEffectBlockBreakRoutine", at = @At("HEAD"), argsOnly = true, index = 0)
    private static ItemStack susy$aoeStack(ItemStack stack) {
        if (stack.getItem() instanceof ItemToolbelt belt) {
            ItemStack selected = belt.getSelectedTool(stack);
            if (!selected.isEmpty()) {
                SUSY_BELT.set(stack);
                return selected;
            }
        }
        return stack;
    }

    @Inject(method = "areaOfEffectBlockBreakRoutine", at = @At("RETURN"))
    private static void susy$persistAfterAoe(ItemStack stack, EntityPlayerMP player,
                                             CallbackInfoReturnable<Boolean> cir) {
        ItemStack beltStack = SUSY_BELT.get();
        if (beltStack != null) {
            if (beltStack.getItem() instanceof ItemToolbelt belt) belt.getHandler(beltStack).persist();
            SUSY_BELT.remove();
        }
    }
}
