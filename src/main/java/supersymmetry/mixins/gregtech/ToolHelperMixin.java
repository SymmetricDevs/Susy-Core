package supersymmetry.mixins.gregtech;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import gregtech.api.items.toolitem.ToolHelper;
import supersymmetry.common.item.ItemToolbelt;

/**
 * Substitutes the selected tool for a toolbelt stack at the head of GT's static tool helpers,
 * the same way GTCEu-Reloaded's ToolHelper.toolbeltPassthrough does. This is what makes AoE
 * work: getMaxAoEDefinition reads the behaviours tag of the stack it is given.
 * <p>
 * The AoE routine also writes the belt's inventory back into its NBT when it returns, since the
 * damage it applies goes to the tool stack inside the belt. Belt methods that delegate to the
 * selected tool do the same; see ItemToolbelt.onBlockDestroyed.
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
        if (beltStack == null) return;
        SUSY_BELT.remove();
        if (beltStack.getItem() instanceof ItemToolbelt belt) belt.getHandler(beltStack).persist();
    }

    /**
     * The routine stops as soon as the item in hand is no longer the stack it is breaking with, so
     * that a tool that breaks mid-AoE ends the area. A belt in hand is never item-equal to the tool
     * it stands for, which ended every area after the first block - a 3x3 dug two blocks. The fork
     * resolves the held stack through its passthrough before comparing; so do we.
     */
    @Redirect(method = "areaOfEffectBlockBreakRoutine",
              remap = true,
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/item/ItemStack;isItemEqualIgnoreDurability(Lnet/minecraft/item/ItemStack;)Z"))
    private static boolean susy$heldToolMatches(ItemStack held, ItemStack tool) {
        return ItemToolbelt.passthrough(held).isItemEqualIgnoreDurability(tool);
    }
}
