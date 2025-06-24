package supersymmetry.mixins.gregtech;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.stack.ItemMaterialInfo;
import gregtech.api.unification.stack.MaterialStack;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import supersymmetry.loaders.recipes.handlers.RecyclingManager;

@Mixin(value = OreDictUnifier.class, remap = false)
public abstract class OreDictUnifierMixin {


    /**
     * @author Tian_mi
     * @reason Replace it with our own implementation.
     */
    @Overwrite
    public static void registerOre(ItemStack itemStack, ItemMaterialInfo materialInfo) {
        RecyclingManager.registerOre(itemStack, materialInfo);
    }

    @WrapOperation(method = "getMaterial(Lnet/minecraft/item/ItemStack;)Lgregtech/api/unification/stack/MaterialStack;",
            at = @At(target = "Lgregtech/api/unification/stack/MaterialStack;copy()Lgregtech/api/unification/stack/MaterialStack;",
                    value = "INVOKE"))
    private static MaterialStack requireNonNull(MaterialStack ms, Operation<MaterialStack> original) {
        return ms == null ? null : original.call(ms);
    }
}
