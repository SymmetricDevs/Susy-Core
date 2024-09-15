package supersymmetry.mixins.gregtech;

import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityFluidDrill;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(MetaTileEntityFluidDrill.class)
public abstract class MetaTileEntityFluidDrillMixin extends MultiblockWithDisplayBase {

    public MetaTileEntityFluidDrillMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
    }

    @Override
    public boolean getIsWeatherOrTerrainResistant() {
        return true;
    }
}