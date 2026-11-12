package supersymmetry.mixins.gregtech;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
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

    /**
     * GT's behaviours (wrench, screwdriver, crowbar, {@code onActionDone}) damage
     * {@code player.getHeldItem(hand)}, which is the belt when a belt is held. Without this branch
     * the belt's damage path ran against the belt: it read and wrote the *selected tool's*
     * durability through the tag passthrough without ever touching the tool's charge, and once that
     * durability passed the tool's maximum it shrank the belt. The fork has the same branch at the
     * head of damageItem; this is it.
     */
    @Inject(method = "damageItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EntityLivingBase;I)V",
            at = @At("HEAD"),
            cancellable = true)
    private static void susy$damageSelectedTool(ItemStack stack, EntityLivingBase entity, int damage,
                                                CallbackInfo ci) {
        if (!(stack.getItem() instanceof ItemToolbelt belt)) return;
        ItemStack selected = belt.getSelectedTool(stack);
        if (selected.isEmpty()) return;
        ci.cancel();
        ToolHelper.damageItem(selected, entity, damage);
        if (entity == null || !entity.world.isRemote) belt.getHandler(stack).persist();
    }

    @Inject(method = "damageItemWhenCrafting(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EntityLivingBase;)V",
            at = @At("HEAD"),
            cancellable = true)
    private static void susy$damageSelectedToolWhenCrafting(ItemStack stack, EntityLivingBase entity,
                                                            CallbackInfo ci) {
        if (!(stack.getItem() instanceof ItemToolbelt belt)) return;
        ItemStack selected = belt.getSelectedTool(stack);
        if (selected.isEmpty()) return;
        ci.cancel();
        ToolHelper.damageItemWhenCrafting(selected, entity);
        belt.getHandler(stack).persist();
    }

    /**
     * The fork's {@code stack = toolbeltPassthrough(stack)} line at the head of onActionDone: the sound
     * and the wear of a rotate / plunger / rail action belong to the selected tool, not to the belt.
     * onActionDone stores the held stack into its first local, so rewriting that local is the exact
     * equivalent of the fork's assignment.
     */
    @ModifyVariable(method = "onActionDone", at = @At("STORE"), index = 3)
    private static ItemStack susy$actionDoneStack(ItemStack stack) {
        return ItemToolbelt.passthrough(stack);
    }

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
     * Tree felling breaks its blocks from {@link TreeFellingListener} on later ticks, straight
     * through this helper, so the wear never passes ItemToolbelt.onBlockDestroyed. The damage lands
     * on the tool stack inside the belt, and without this write-back the belt's NBT keeps the old
     * durability - which the client shows - and keeps the tool even after it breaks. The AoE routine
     * is skipped because it persists once when it returns.
     */
    @Inject(method = "breakBlockRoutine(Lnet/minecraft/entity/player/EntityPlayerMP;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/math/BlockPos;)Z",
            at = @At("RETURN"))
    private static void susy$persistBeltAfterBreak(EntityPlayerMP player, ItemStack tool, BlockPos pos,
                                                   CallbackInfoReturnable<Boolean> cir) {
        if (SUSY_BELT.get() != null) return;
        ItemStack held = player.getHeldItemMainhand();
        if (!(held.getItem() instanceof ItemToolbelt belt)) return;
        ItemToolbelt.ToolStackHandler handler = belt.getHandler(held);
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            if (handler.getStackInSlot(slot) == tool) {
                handler.persist();
                return;
            }
        }
    }

    /**
     * The routine stops as soon as the item in hand is no longer the stack it is breaking with, so
     * that a tool that breaks mid-AoE ends the area. Fixes AoE behaviour passthrough.
     */
    @Redirect(method = "areaOfEffectBlockBreakRoutine",
              remap = true,
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/item/ItemStack;isItemEqualIgnoreDurability(Lnet/minecraft/item/ItemStack;)Z"))
    private static boolean susy$heldToolMatches(ItemStack held, ItemStack tool) {
        return ItemToolbelt.passthrough(held).isItemEqualIgnoreDurability(tool);
    }
}
