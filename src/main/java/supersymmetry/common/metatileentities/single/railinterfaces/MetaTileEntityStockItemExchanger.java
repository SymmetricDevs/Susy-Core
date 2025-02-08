package supersymmetry.common.metatileentities.single.railinterfaces;

import cam72cam.immersiverailroading.entity.Freight;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import supersymmetry.client.renderer.textures.SusyTextures;

import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityStockItemExchanger extends MetaTileEntityStockInteractor {

    public MetaTileEntityStockItemExchanger(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SusyTextures.STOCK_ITEM_EXCHANGER);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityStockItemExchanger(this.metaTileEntityId);
    }

    // TODO: cache this?
    @Override
    protected <T> T getStockCapability(Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            IItemHandler itemHandler = null;
            if (this.stock instanceof Freight itemStock) {
                itemHandler = itemStock.cargoItems.internal;
            } // TODO: add more if-else arguments if there's more kinds of stocks. Or maybe a utility method
            if (itemHandler != null && itemHandler.getSlots() > 0) {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemHandler);
            }
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("susy.stock_interfaces.item_exchanger.description"));
        tooltip.add(I18n.format("susy.stock_interfaces.screwdriver_cycle"));
        tooltip.add(I18n.format("susy.stock_interfaces.wrench_toggle"));
    }
}
