package supersymmetry.common.metatileentities.multiblockpart;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import supersymmetry.api.capability.IStrandProvider;
import supersymmetry.api.capability.Strand;
import supersymmetry.api.capability.SuSyCapabilities;
import supersymmetry.api.metatileentity.multiblock.SuSyMultiblockAbilities;
import supersymmetry.client.renderer.textures.SusyTextures;

import java.io.IOException;
import java.util.List;
import java.util.function.BiConsumer;

public class MetaTileEntityStrandBus extends MetaTileEntityMultiblockPart implements IStrandProvider, IMultiblockAbilityPart<IStrandProvider> {
    private Strand strand;
    private boolean isExport;

    public MetaTileEntityStrandBus(ResourceLocation metaTileEntityId, boolean isExport) {
        super(metaTileEntityId, 4);
        this.isExport = isExport;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityStrandBus(metaTileEntityId, isExport);
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
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == SuSyCapabilities.STRAND_PROVIDER) {
            return SuSyCapabilities.STRAND_PROVIDER.cast(this);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public Strand getStrand() {
        return strand;
    }

    @Override
    public Strand take() {
        Strand transfer = this.strand; // Need to keep the reference
        this.strand = null;
        return transfer;
    }

    @Override
    public Strand insertStrand(Strand strand) {
        if (this.strand != null) {
            return strand;
        }
        this.strand = strand;
        return null;
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote && getOffsetTimer() % 5 == 0) {
            if (isExport) {
                pushItemsIntoNearbyHandlers(getFrontFacing());
            } else {
                pullItemsFromNearbyHandlers(getFrontFacing());
            }
        }
    }

    public void pushItemsIntoNearbyHandlers(EnumFacing... allowedFaces) {
        this.transferToNearby(SuSyCapabilities.STRAND_PROVIDER, this::transferStrand, allowedFaces);
    }

    public void pullItemsFromNearbyHandlers(EnumFacing... allowedFaces) {
        this.transferToNearby(SuSyCapabilities.STRAND_PROVIDER, (thisCap, otherCap) -> this.transferStrand(otherCap, thisCap), allowedFaces);
    }

    public void transferStrand(IStrandProvider sender, IStrandProvider receiver) {
        if (sender.getStrand() == null || receiver.getStrand() != null) {
            return;
        }
        receiver.insertStrand(sender.take());
    }

    private <T> void transferToNearby(Capability<T> capability, BiConsumer<T, T> transfer, EnumFacing... allowedFaces) {
        for (EnumFacing nearbyFacing : allowedFaces) {
            TileEntity tileEntity = this.getNeighbor(nearbyFacing);
            if (tileEntity != null) {
                T otherCap = tileEntity.getCapability(capability, nearbyFacing.getOpposite());
                T thisCap = this.getCoverCapability(capability, nearbyFacing);
                if (otherCap != null && thisCap != null) {
                    transfer.accept(thisCap, otherCap);
                }
            }
        }
    }


    @Override
    public MultiblockAbility<IStrandProvider> getAbility() {
        return isExport ? SuSyMultiblockAbilities.STRAND_EXPORT : SuSyMultiblockAbilities.STRAND_IMPORT;
    }

    @Override
    public void registerAbilities(List<IStrandProvider> abilityList) {
        abilityList.add(this);
    }

    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (this.shouldRenderOverlay()) {
            SusyTextures.STRAND_BUS_OVERLAY.renderSided(this.getFrontFacing(), renderState, translation, pipeline);
            SimpleOverlayRenderer overlay = this.isExport ? Textures.ITEM_HATCH_OUTPUT_OVERLAY :
                    Textures.ITEM_HATCH_INPUT_OVERLAY;
            overlay.renderSided(this.getFrontFacing(), renderState, translation, pipeline);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setTag("Strand", Strand.serialize(new NBTTagCompound(), this.strand));
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.strand = Strand.deserialize(data.getCompoundTag("Strand"));
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        try {
            this.strand = Strand.deserialize(buf.readCompoundTag());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeCompoundTag(Strand.serialize(new NBTTagCompound(), strand));
    }
}
