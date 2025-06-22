package supersymmetry.common.metatileentities.single.electric;

import gregtech.api.GTValues;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MetaTileEntityRTG extends TieredMetaTileEntity {
    public MetaTileEntityRTG(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityRTG(this.metaTileEntityId, this.getTier());
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    protected void reinitializeEnergyContainer() {
        super.reinitializeEnergyContainer();

    }

    @Override
    public void update() {
        super.update();
        this.energyContainer.changeEnergy(GTValues.VH[getTier() - 1]);
    }

    @Override
    protected boolean isEnergyEmitter() {
        return true;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.rtg.tooltip.info"));
        tooltip.add(I18n.format("gregtech.machine.rtg.tooltip.description"));
        tooltip.add(I18n.format("gregtech.machine.rtg.voltage_produced", new Object[]{GTValues.VH[getTier() - 1], GTValues.VNF[this.getTier()-1]}));
        tooltip.add(I18n.format("gregtech.universal.tooltip.max_voltage_out", new Object[]{this.energyContainer.getOutputVoltage(), GTValues.VNF[this.getTier()]}));
        tooltip.add(I18n.format("gregtech.universal.tooltip.energy_storage_capacity", new Object[]{this.energyContainer.getEnergyCapacity()}));
    }

    @Override
    public boolean getIsWeatherOrTerrainResistant() {
        return true;
    }
}
