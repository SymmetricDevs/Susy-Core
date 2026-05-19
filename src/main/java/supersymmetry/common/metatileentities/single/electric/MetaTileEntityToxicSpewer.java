package supersymmetry.common.metatileentities.single.electric;

import gregtech.api.GTValues;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MetaTileEntityToxicSpewer extends TieredMetaTileEntity {

    private int currentRadius = 0;

    public MetaTileEntityToxicSpewer(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityToxicSpewer(this.metaTileEntityId, this.getTier());
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

        applyEffectsInRadius(getWorld(), getPos(), currentRadius);

        if (currentRadius < GTValues.VH[getTier()]) currentRadius++;
    }

    private void applyEffectsInRadius(World world, BlockPos center, int radius) {
        if (radius == 0) return;

        net.minecraft.util.math.AxisAlignedBB searchBox = new net.minecraft.util.math.AxisAlignedBB(
                center.getX() - radius, 0,
                center.getZ() - radius,
                center.getX() + radius, 256,
                center.getZ() + radius
        );

        List<net.minecraft.entity.EntityLivingBase> entities =
                world.getEntitiesWithinAABB(net.minecraft.entity.EntityLivingBase.class, searchBox);

        for (net.minecraft.entity.EntityLivingBase entity : entities) {
            double dx = entity.posX - center.getX();
            double dz = entity.posZ - center.getZ();
            if (dx * dx + dz * dz > (double) radius * radius) continue;
            BlockPos entityPos = new BlockPos(entity.posX, entity.posY, entity.posZ);
            if (!world.canSeeSky(entityPos)) continue;
            System.out.println(GTValues.VH[getTier()]);
            entity.addPotionEffect(new net.minecraft.potion.PotionEffect(
                    net.minecraft.init.MobEffects.POISON, (int) (GTValues.V[getTier()]*20), getTier()-1, false, true));
            entity.addPotionEffect(new net.minecraft.potion.PotionEffect(
                    net.minecraft.init.MobEffects.WITHER, (int) (GTValues.V[getTier()]*20), getTier()-1, false, true));
            entity.addPotionEffect(new net.minecraft.potion.PotionEffect(
                    net.minecraft.init.MobEffects.NAUSEA, (int) (GTValues.V[getTier()]*20), getTier()-1, false, true));
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        tooltip.add(I18n.format("susy.machine.toxic_spewer.tooltip.info"));
        tooltip.add(I18n.format("susy.machine.toxic_spewer.tooltip.description"));
        tooltip.add(I18n.format("susy.machine.generic.tooltip.radius_warning"));
    }

    @Override
    public boolean getIsWeatherOrTerrainResistant() { return true; }
}
