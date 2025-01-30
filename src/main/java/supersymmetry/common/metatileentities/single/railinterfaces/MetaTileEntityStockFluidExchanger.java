package supersymmetry.common.metatileentities.single.railinterfaces;

import cam72cam.immersiverailroading.entity.FreightTank;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import supersymmetry.client.renderer.textures.SusyTextures;

import javax.annotation.Nullable;
import java.util.List;

//#fix# can hold plasma acid gas and lava, maybe change that
public class MetaTileEntityStockFluidExchanger extends MetaTileEntityStockInteractor {

    public MetaTileEntityStockFluidExchanger(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SusyTextures.STOCK_FLUID_EXCHANGER);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityStockFluidExchanger(this.metaTileEntityId);
    }

    // TODO: cache this?
    @Override
    protected <T> T getStockCapability(Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            IFluidHandler fluidHandler = null;
            if (this.stock instanceof FreightTank tankStock) {
                fluidHandler = tankStock.theTank.internal;
            } // TODO: add more if-else arguments if there's more kinds of stocks. Or maybe a utility method
            if (fluidHandler != null && fluidHandler.getTankProperties().length > 0) {
                return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidHandler);
            }
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("susy.stock_interfaces.fluid_exchanger.description"));
        tooltip.add(I18n.format("susy.stock_interfaces.right_click_for_gui"));
    }
}
