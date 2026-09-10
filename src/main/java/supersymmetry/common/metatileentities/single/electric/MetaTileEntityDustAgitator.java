package supersymmetry.common.metatileentities.single.electric;

import java.util.List;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GregTechAPI;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.GTValues;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import supersymmetry.common.network.SPacketDustFog;

public class MetaTileEntityDustAgitator extends TieredMetaTileEntity {

    private int currentRadius = 0;

    private final OrientedOverlayRenderer overlay;

    public MetaTileEntityDustAgitator(ResourceLocation metaTileEntityId,OrientedOverlayRenderer overlay, int tier) {
        super(metaTileEntityId, tier);
        this.overlay = overlay;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityDustAgitator(this.metaTileEntityId, this.overlay, this.getTier());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.overlay.renderOrientedState(
                renderState,
                translation,
                pipeline,
                Cuboid6.full,
                getFrontFacing(),
                true,
                true
        );
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
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER && side != null) {
            return null;
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void update() {
        super.update();

        if (getWorld().isRemote) return;

        this.energyContainer.changeEnergy(GTValues.VH[getTier() - 1]);

        if (this.energyContainer.getEnergyStored() < this.energyContainer.getEnergyCapacity())
            return;

        this.energyContainer.removeEnergy(this.energyContainer.getEnergyCapacity());

        if (currentRadius < 32) {
            currentRadius++;
        }

        for (EntityPlayer player : getWorld().playerEntities) {

            double distanceSq = player.getDistanceSq(getPos());

            if (distanceSq > currentRadius * currentRadius)
                continue;

            float fogStrength = 1.0f -
                    ((float) Math.sqrt(distanceSq) / currentRadius);

            fogStrength = Math.max(0.0f, Math.min(1.0f, fogStrength));

            GregTechAPI.networkHandler.sendTo(
                    new SPacketDustFog(fogStrength),
                    (EntityPlayerMP) player
            );
        }
    }



    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        tooltip.add(I18n.format("susy.machine.dust_agitator.tooltip.info"));
        tooltip.add(I18n.format("susy.machine.dust_agitator.tooltip.description"));
        tooltip.add(I18n.format("susy.machine.generic.tooltip.radius_warning"));
    }

    @Override
    public boolean getIsWeatherOrTerrainResistant() {
        return true;
    }
}
