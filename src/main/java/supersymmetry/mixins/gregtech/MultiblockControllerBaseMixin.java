package supersymmetry.mixins.gregtech;

import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.ConfigHolder;

@Mixin(MultiblockControllerBase.class)
public abstract class MultiblockControllerBaseMixin extends MetaTileEntity {

    public MultiblockControllerBaseMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        if (ConfigHolder.machines.doTerrainExplosion && getIsWeatherOrTerrainResistant()) {
            tooltip.add(I18n.format("gregtech.universal.tooltip.terrain_resist"));
        }
    }
}
