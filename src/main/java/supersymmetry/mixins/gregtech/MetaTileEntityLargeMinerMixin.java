package supersymmetry.mixins.gregtech;

import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityLargeMiner;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(MetaTileEntityLargeMiner.class)
public abstract class MetaTileEntityLargeMinerMixin extends MultiblockWithDisplayBase {

    public MetaTileEntityLargeMinerMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public boolean getIsWeatherOrTerrainResistant() {
        return true;
    }

    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
    }
}
