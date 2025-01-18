package supersymmetry.common.metatileentities.single.railinterfaces;

import cam72cam.immersiverailroading.entity.Freight;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import gregtech.api.gui.widgets.TabGroup;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
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
import java.util.ArrayList;
import java.util.List;

public class MetaTileEntityStockItemExchanger extends MetaTileEntityStockInteractor
{
    //locomotive, freight
    public static List<String> subFilter = new ArrayList<>();
    static{
        subFilter.add("locomotive");
        subFilter.add("freight");
    }

    public MetaTileEntityStockItemExchanger(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, subFilter);
    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, GuiSyncManager guiSyncManager) {
        return null;
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityStockItemExchanger(this.metaTileEntityId);
    }

    public int getLightOpacity() {
        return 1;
    }

    public boolean isOpaqueCube() {
        return true;
    }

    public String getHarvestTool() {
        return "wrench";
    }

    public boolean hasFrontFacing() {
        return true;
    }

    protected void initializeInventory() {
        super.initializeInventory();
        this.itemInventory = null;
    }

    public void update() {
        super.update();

        if(this.getWorld().isRemote)
            return;

        this.itemInventory = null;

        if(this.isWorkingEnabled() && this.getOffsetTimer() % 20 == 0 && this.stocks.size() > 0)
        {
            Freight freightStock = (Freight) stocks.get(0);
            cam72cam.mod.item.ItemStackHandler umodStockStackHandler = freightStock.cargoItems;
            this.itemInventory = umodStockStackHandler.internal;
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            IItemHandler itemHandler = this.itemInventory;
            return itemHandler != null && itemHandler.getSlots() > 0 ? CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemHandler) : null;
        }

        return super.getCapability(capability, side);
    }

    @Override
    protected void appendDefaultTab(EntityPlayer entityPlayer, TabGroup tabGroup) {

    }

    public boolean needsSneakToRotate() {
        return true;
    }

    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        if (this.isWorkingEnabled()) {
            SusyTextures.STOCK_ITEM_EXCHANGER_PULLING_ON.renderOrientedState(renderState, translation, pipeline, Cuboid6.full, this.getFrontFacing(), true, true);
        } else {
            SusyTextures.STOCK_ITEM_EXCHANGER_PULLING_OFF.renderOrientedState(renderState, translation, pipeline, Cuboid6.full, this.getFrontFacing(), true, true);
        }
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("susy.stock_interfaces.item_exchanger.description"));
        tooltip.add(I18n.format("susy.stock_interfaces.screwdriver_cycle"));
        tooltip.add(I18n.format("susy.stock_interfaces.wrench_toggle"));
    }

    //#fix# what does this do
    public boolean showToolUsages() {
        return false;
    }

}
