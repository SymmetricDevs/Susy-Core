package supersymmetry.mixins.gregtech;

import java.util.List;

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

import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.common.metatileentities.electric.MetaTileEntityMiner;

@Mixin(value = MetaTileEntityMiner.class, remap = false)
public abstract class MetaTileEntityMinerMixin extends TieredMetaTileEntity {

    public MetaTileEntityMinerMixin(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
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
}
