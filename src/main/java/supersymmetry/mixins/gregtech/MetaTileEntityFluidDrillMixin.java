package supersymmetry.mixins.gregtech;

import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityFluidDrill;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(value = MetaTileEntityFluidDrill.class, remap = false)
public abstract class MetaTileEntityFluidDrillMixin extends MultiblockWithDisplayBase {

    public MetaTileEntityFluidDrillMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Inject(method = "addInformation",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
                    ordinal = 3,
                    shift = At.Shift.AFTER))
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip, boolean advanced, CallbackInfo ci) {
        super.addInformation(stack, player, tooltip, advanced);
    }

    @Override
    public boolean getIsWeatherOrTerrainResistant() {
        return true;
    }
}
