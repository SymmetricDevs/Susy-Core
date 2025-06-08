package supersymmetry.mixins.gregtech;

import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.stack.ItemAndMetadata;
import gregtech.api.unification.stack.ItemMaterialInfo;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = OreDictUnifier.class, remap = false)
public interface OreDictUnifierAccessor {

    @Accessor("materialUnificationInfo")
    @Final
    @NotNull
    static Map<ItemAndMetadata, ItemMaterialInfo> getUnificationInfo() {
        throw new AssertionError();
    }
}
