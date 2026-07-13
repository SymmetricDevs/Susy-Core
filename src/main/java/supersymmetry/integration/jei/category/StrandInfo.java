package supersymmetry.integration.jei.category;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import gregicality.multiblocks.api.fluids.GCYMFluidStorageKeys;
import gregtech.api.fluids.store.FluidStorageKeys;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import supersymmetry.api.capability.StrandConversion;

public class StrandInfo implements IRecipeWrapper {

    public FluidStack fluidStack;

    public List<ItemStack> conversions = new ObjectArrayList<>();

    public StrandInfo(Material material) {
        Fluid fluid;
        if (material.getFluid(GCYMFluidStorageKeys.MOLTEN) != null) {
            fluid = material.getFluid(GCYMFluidStorageKeys.MOLTEN);
        } else {
            fluid = material.getFluid(FluidStorageKeys.LIQUID);
        }
        fluidStack = new FluidStack(fluid, 2592);

        for (StrandConversion conv : StrandConversion.CONVERSIONS) {
            ItemStack item = OreDictUnifier.get(conv.prefix, material, conv.amount);
            if (item != null && !item.isEmpty()) {
                conversions.add(item);
            }
        }
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInput(VanillaTypes.FLUID, fluidStack);
        ingredients.setOutputs(VanillaTypes.ITEM, conversions);
    }
}
