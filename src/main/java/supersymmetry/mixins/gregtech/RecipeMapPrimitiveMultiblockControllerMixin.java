package supersymmetry.mixins.gregtech;

import java.util.List;

import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.metatileentity.multiblock.RecipeMapPrimitiveMultiblockController;

@Mixin(RecipeMapPrimitiveMultiblockController.class)
public abstract class RecipeMapPrimitiveMultiblockControllerMixin extends MultiblockWithDisplayBase {

    public RecipeMapPrimitiveMultiblockControllerMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }


    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("susy.general.requires_atmosphere"));
    }
}
