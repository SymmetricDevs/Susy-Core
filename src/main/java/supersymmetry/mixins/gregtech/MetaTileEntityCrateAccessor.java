package supersymmetry.mixins.gregtech;

import gregtech.api.unification.material.Material;
import gregtech.common.metatileentities.storage.MetaTileEntityCrate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = MetaTileEntityCrate.class, remap = false)
public interface MetaTileEntityCrateAccessor {

    @Final
    @Accessor("material")
    Material getMaterial();

    @Final
    @Accessor("inventorySize")
    int getInventorySize();

    @Accessor("isTaped")
    void setTaped(boolean taped);
}
