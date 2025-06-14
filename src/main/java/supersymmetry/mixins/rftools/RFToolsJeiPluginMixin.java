package supersymmetry.mixins.rftools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mcjty.lib.varia.ItemStackList;
import mcjty.rftools.compat.jei.PacketSendRecipe;
import mcjty.rftools.compat.jei.RFToolsJeiPlugin;
import mcjty.rftools.network.RFToolsMessages;
import mezz.jei.api.gui.IGuiIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RFToolsJeiPlugin.class, remap = false)
public class RFToolsJeiPluginMixin {
  @Inject(method = "transferRecipe", at = @At("HEAD"), cancellable = true)
  private static void transferRecipe(
      Map<Integer, ? extends IGuiIngredient<ItemStack>> guiIngredients,
      BlockPos pos,
      CallbackInfo ci) {

    ItemStackList items = ItemStackList.create(10);
    for (Map.Entry<Integer, ? extends IGuiIngredient<ItemStack>> entry :
        guiIngredients.entrySet()) {
      int recipeSlot = entry.getKey();
      List<ItemStack> allIngredients = entry.getValue().getAllIngredients();
      if (!allIngredients.isEmpty()) {
        // items.set(recipeSlot, allIngredients.get(0));  // what was used originally
        // https://github.com/McJtyMods/RFTools/blob/030a495c8530183bdfe47475ed41d45386e56f6c/src/main/java/mcjty/rftools/compat/jei/RFToolsJeiPlugin.java#L27
        items.set(recipeSlot, getPreferredItem(allIngredients));
      }
    }
    RFToolsMessages.INSTANCE.sendToServer(new PacketSendRecipe(items, pos));
    ci.cancel();
  }

  // could've been better, but oh well, that function isnt called that often anyways
  private static final Map<String, Integer> PRIORITY = new HashMap<>();

  static {
    PRIORITY.put("gregtech", 3);
    PRIORITY.put("pyrotech", 2);
    PRIORITY.put("minecraft", 1);
  }

  private static ItemStack getPreferredItem(List<ItemStack> stacks) {
    ItemStack preferred = ItemStack.EMPTY;
    int bestPriority = -1;

    for (ItemStack stack : stacks) {
      if (stack.isEmpty()) continue;
      ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
      if (id == null) continue;
      String namespace = id.getNamespace();
      int prio = PRIORITY.getOrDefault(namespace, 0);
      if (prio > bestPriority) {
        preferred = stack;
        bestPriority = prio;
      }
    }
    return preferred;
  }
}
