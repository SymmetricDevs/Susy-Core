package supersymmetry.common.metatileentities.single.electric;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MetaTileEntityInterferenceDynamo extends TieredMetaTileEntity {

    private int currentRadius = 0;
    private long drainPerCycle = GTValues.V[getTier()+1];

    public MetaTileEntityInterferenceDynamo(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityInterferenceDynamo(this.metaTileEntityId, this.getTier());
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) { return null; }

    @Override
    protected boolean openGUIOnRightClick() { return false; }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("harmRadius", currentRadius);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        currentRadius = data.getInteger("harmRadius");
    }

    @Override
    public void update() {
        super.update();

        this.energyContainer.changeEnergy(GTValues.VH[getTier() - 1]);

        if (this.energyContainer.getEnergyStored() < this.energyContainer.getEnergyCapacity()) return;
        if (getWorld().isRemote) return;

        this.energyContainer.removeEnergy(this.energyContainer.getEnergyCapacity());

        drainInRadius(getWorld(), getPos(), currentRadius);

        if (currentRadius < 32) currentRadius++;
    }

    private void drainInRadius(World world, BlockPos center, int radius) {
        if (radius == 0) return;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos target = center.add(dx, dy, dz);
                    if (!world.isValid(target)) continue;

                    if (target.equals(center)) continue;

                    TileEntity te = world.getTileEntity(target);
                    if (te == null) continue;

                    IEnergyContainer energy = te.getCapability(
                            GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, null);

                    if (energy == null) continue;

                    long available = energy.getEnergyStored();
                    if (available <= 0) continue;

                    energy.removeEnergy(Math.min(drainPerCycle, available));
                }
            }
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        tooltip.add(I18n.format("susy.machine.interference_dynamo.tooltip.info"));
        tooltip.add(I18n.format("susy.machine.interference_dynamo.tooltip.description"));
        tooltip.add(I18n.format("susy.machine.interference_dynamo.tooltip.description2"));
        tooltip.add(I18n.format("susy.machine.generic.tooltip.radius_warning"));
        tooltip.add(I18n.format("susy.machine.interference_dynamo.tooltip.drain",
                drainPerCycle));
    }

    @Override
    public boolean getIsWeatherOrTerrainResistant() { return true; }
}
