package supersymmetry.common.metatileentities.single.railinterfaces;

import cam72cam.immersiverailroading.entity.FreightTank;
import codechicken.lib.raytracer.CuboidRayTraceResult;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import supersymmetry.client.renderer.textures.SusyTextures;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

//#fix# can hold plasma acid gas and lava, maybe change that
public class MetaTileEntityStockFluidExchanger extends MetaTileEntityStockInteractor
{

    private static FluidTank internalTank = new FluidTank(0);

    //locomotive, tank
    public static List<String> subFilter = new ArrayList<>();
    static{
        subFilter.add("locomotive");
        subFilter.add("freight_tank");
    }


    public MetaTileEntityStockFluidExchanger(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SusyTextures.STOCK_FLUID_EXCHANGER);
    }



    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityStockFluidExchanger(this.metaTileEntityId);
    }

    public String getHarvestTool() {
        return "wrench";
    }

    public boolean hasFrontFacing() {
        return true;
    }

    protected void initializeInventory() {
        super.initializeInventory();
        this.fluidInventory = this.internalTank;
    }
    public void update() {
        super.update();

        if(this.getWorld().isRemote)
            return;

        if (this.stocks.size() == 0 || !this.isWorkingEnabled()) {
            this.fluidInventory = this.internalTank;
        }

        if(this.isWorkingEnabled() && this.getOffsetTimer() % 20 == 0 && this.stocks.size() > 0) {
            FreightTank tankStock = (FreightTank)stocks.get(0);
            cam72cam.mod.fluid.FluidTank umodStockTank = tankStock.theTank;
            this.fluidInventory = umodStockTank.internal;
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            IFluidHandler fluidHandler = this.fluidInventory;
            return fluidHandler != null && fluidHandler.getTankProperties().length > 0 ? CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidHandler) : null;
        }
        return super.getCapability(capability, side);
    }

    public boolean needsSneakToRotate() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("susy.stock_interfaces.fluid_exchanger.description"));
        tooltip.add(I18n.format("susy.stock_interfaces.right_click_for_gui"));
    }
    public boolean isOpaqueCube() {
        return false;
    }

    public int getLightOpacity() {
        return 0;
    }

    @Override
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        if (!playerIn.getHeldItem(hand).hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            return super.onRightClick(playerIn, hand, facing, hitResult);
        } else {
            return this.getWorld().isRemote || !playerIn.isSneaking() && FluidUtil.interactWithFluidHandler(playerIn, hand, this.fluidInventory);
        }
    }
}
