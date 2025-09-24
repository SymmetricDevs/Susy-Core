package supersymmetry.mixins.gregtech;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityFluidDrill;

@Mixin(value = MetaTileEntityFluidDrill.class, remap = false)
public abstract class MetaTileEntityFluidDrillMixin extends MultiblockWithDisplayBase {

    public MetaTileEntityFluidDrillMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @SideOnly(Side.CLIENT)
    @Inject(method = "addInformation", at = @At("TAIL"))
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip, boolean advanced,
                               CallbackInfo ci) {
        super.addInformation(stack, player, tooltip, advanced);
    }

    @Override
    public boolean getIsWeatherOrTerrainResistant() {
        return true;
    }

    @Override
    public boolean isMultiblockPartWeatherResistant(@Nonnull IMultiblockPart part) {
        return true;
    }
}
