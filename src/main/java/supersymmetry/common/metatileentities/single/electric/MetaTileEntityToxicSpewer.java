package supersymmetry.common.metatileentities.single.electric;

import java.util.List;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
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
import net.minecraft.network.PacketBuffer;
import supersymmetry.client.renderer.particles.SusyParticleFlareSmoke;
import supersymmetry.client.renderer.particles.SusyParticleToxicPlume;

public class MetaTileEntityToxicSpewer extends TieredMetaTileEntity {

    private static final int PARTICLE_BURST = 1;

    private int currentRadius = 0;

    private final OrientedOverlayRenderer overlay;

    public MetaTileEntityToxicSpewer(ResourceLocation metaTileEntityId, OrientedOverlayRenderer overlay, int tier) {
        super(metaTileEntityId, tier);
        this.overlay = overlay;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityToxicSpewer(this.metaTileEntityId, this.overlay, this.getTier());
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

        this.energyContainer.changeEnergy(GTValues.VH[getTier() - 1]);

        if (this.energyContainer.getEnergyStored() < this.energyContainer.getEnergyCapacity()) return;
        if (getWorld().isRemote) return;

        this.energyContainer.removeEnergy(this.energyContainer.getEnergyCapacity());

        applyEffectsInRadius(getWorld(), getPos(), currentRadius);

        if (currentRadius < GTValues.VH[getTier()]) currentRadius++;

        writeCustomData(PARTICLE_BURST, buf -> {});
    }

    @Override
    public void receiveCustomData(int discriminator, PacketBuffer buf) {
        if (discriminator == PARTICLE_BURST) {
            spawnGreenSmokeBurst();
        } else {
            super.receiveCustomData(discriminator, buf);
        }
    }

    @SideOnly(Side.CLIENT)
    private void spawnGreenSmokeBurst() {
        World world = getWorld();
        BlockPos pos = getPos();
        Minecraft.getMinecraft().effectRenderer.addEffect(
                new SusyParticleToxicPlume(world,
                        pos.getX() + 0.5,
                        pos.getY() + 1.05,
                        pos.getZ() + 0.5));
    }

    private void applyEffectsInRadius(World world, BlockPos center, int radius) {
        if (radius == 0) return;

        net.minecraft.util.math.AxisAlignedBB searchBox = new net.minecraft.util.math.AxisAlignedBB(
                center.getX() - radius, 0,
                center.getZ() - radius,
                center.getX() + radius, 256,
                center.getZ() + radius);

        List<net.minecraft.entity.EntityLivingBase> entities = world
                .getEntitiesWithinAABB(net.minecraft.entity.EntityLivingBase.class, searchBox);

        for (net.minecraft.entity.EntityLivingBase entity : entities) {
            double dx = entity.posX - center.getX();
            double dz = entity.posZ - center.getZ();
            if (dx * dx + dz * dz > (double) radius * radius) continue;
            BlockPos entityPos = new BlockPos(entity.posX, entity.posY, entity.posZ);
            if (!world.canSeeSky(entityPos)) continue;
            entity.addPotionEffect(new net.minecraft.potion.PotionEffect(
                    MobEffects.POISON, (int) (GTValues.V[getTier()] * 20), getTier() - 1, false,
                    true));
            entity.addPotionEffect(new net.minecraft.potion.PotionEffect(
                    MobEffects.WITHER, (int) (GTValues.V[getTier()] * 20), getTier() - 1, false,
                    true));
            entity.addPotionEffect(new net.minecraft.potion.PotionEffect(
                    MobEffects.HUNGER, (int) (GTValues.V[getTier()] * 20), getTier() - 1, false,
                    true));
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
    public boolean getIsWeatherOrTerrainResistant() {
        return true;
    }
}
