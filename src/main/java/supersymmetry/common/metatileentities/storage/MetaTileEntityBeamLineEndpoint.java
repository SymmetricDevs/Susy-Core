package supersymmetry.common.metatileentities.storage;

import codechicken.lib.raytracer.CuboidRayTraceResult;

import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.MetaTileEntity;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import supersymmetry.api.pipenet.beamline.BeamLineType;
import supersymmetry.api.pipenet.beamline.IBeamLineEndpoint;
import supersymmetry.api.pipenet.beamline.ParticleNetwork;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class MetaTileEntityBeamLineEndpoint extends MetaTileEntity implements IBeamLineEndpoint, IDataInfoProvider {

    private final BeamLineType beamLineType;
    private IOType ioType = IOType.NONE;
    private IBeamLineEndpoint link;
    private boolean placed = false;

    public MetaTileEntityBeamLineEndpoint(ResourceLocation metaTileEntityId, BeamLineType beamLineType) {
        super(metaTileEntityId);
        this.beamLineType = beamLineType;
    }

    public void updateNetwork() {
        ParticleNetwork network = ParticleNetwork.get(getWorld(), getPos());
        if (network != null) {
            // manually remove this endpoint from the network
            network.onRemoveEndpoint(this);
        }

        // find networks on input and output face
        List<ParticleNetwork> networks = findNetworks();
        if (networks.isEmpty()) {
            // no neighbours found, create new network
            network = this.beamLineType.createNetwork(getWorld());
            network.onPlaceEndpoint(this);
            setIoType(IOType.NONE);
        } else if (networks.size() == 1) {
            // one neighbour network found, attach self to neighbour network
            networks.get(0).onPlaceEndpoint(this);
        } else {
            // two neighbour networks found, configuration invalid
            setIoType(IOType.NONE);
        }
    }

    public boolean onWrenchClick(EntityPlayer playerIn, EnumHand hand, EnumFacing wrenchSide,
                                 CuboidRayTraceResult hitResult) {
        return super.onWrenchClick(playerIn, hand, wrenchSide.getOpposite(), hitResult);
    }

    @Override
    public void setFrontFacing(EnumFacing frontFacing) {
        this.placed = true;
        super.setFrontFacing(frontFacing);
        if (getWorld() != null && !getWorld().isRemote) {
            updateNetwork();
        }
    }

    @Override
    public boolean isValidFrontFacing(EnumFacing facing) {
        return !this.hasFrontFacing() || getFrontFacing() != facing;
    }

    @Override
    public void onRemoval() {
        if (link != null) {
            // invalidate linked endpoint
            link.invalidateLink();
            invalidateLink();
        }
        setIoType(IOType.NONE);
        ParticleNetwork network = ParticleNetwork.get(getWorld(), getPos());
        // remove endpoint from network
        if (network != null) network.onRemoveEndpoint(this);
    }

    @Override
    public void onNeighborChanged() {
        if (!placed || getWorld() == null || getWorld().isRemote) return;

        List<ParticleNetwork> networks = findNetworks();
        ParticleNetwork network = ParticleNetwork.get(getWorld(), getPos());
        if (network == null) {
            // shouldn't happen
            if (networks.isEmpty()) {
                // create new network since there are no neighbouring networks
                network = this.beamLineType.createNetwork(getWorld());
                network.onPlaceEndpoint(this);
            } else if (networks.size() == 1) {
                // add to neighbour network
                networks.get(0).onPlaceEndpoint(this);
            }
        } else {
            if (networks.size() > 1) {
                // suddenly there are more than one neighbouring networks, invalidate
                onRemoval();
            }
        }
        if (networks.size() != 1) {
            setIoType(IOType.NONE);
        }
    }

    private List<ParticleNetwork> findNetworks() {
        List<ParticleNetwork> networks = new ArrayList<>();
        ParticleNetwork network;
        // only check input and output side
        network = ParticleNetwork.get(getWorld(), getPos().offset(getFrontFacing()));
        if (network != null && beamLineType == network.getBeamLineType() && beamLineType == BeamLineType.single()) {
            // found a network on the input face, therefore this is an output of the network
            networks.add(network);
            setIoType(IOType.OUTPUT);
        }
        network = ParticleNetwork.get(getWorld(), getPos().offset(getOutputFacing()));
        if (network != null && beamLineType == network.getBeamLineType() && beamLineType == BeamLineType.single()) {
            // found a network on the output face, therefore this is an input of the network
            networks.add(network);
            setIoType(IOType.INPUT);
        }
        network = ParticleNetwork.get(getWorld(), getPos().offset(getOutputFacing()));
        if (network != null && beamLineType == network.getBeamLineType() && beamLineType == BeamLineType.collider()) {
            networks.add(network);
            setIoType(IOType.DOUBLE);
        }
        return networks;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        NBTTagCompound nbt = super.writeToNBT(data);
        data.setByte("Type", (byte) ioType.ordinal());
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.ioType = IOType.values()[data.getByte("Type")];
    }

    @Override
    public @NotNull IOType getIoType() {
        return ioType;
    }

    @Override
    public void setIoType(IOType ioType) {
        this.ioType = Objects.requireNonNull(ioType);
    }

    @Override
    public IBeamLineEndpoint getLink() {
        if (link == null) {
            ParticleNetwork network = ParticleNetwork.get(getWorld(), getPos());
            if (network != null && network.isValid()) {
                this.link = network.getOtherEndpoint(this);
            }
        } else if (!this.link.isValid()) {
            this.link.invalidateLink();
            this.link = null;
            ParticleNetwork network = ParticleNetwork.get(getWorld(), getPos());
            if (network != null) {
                network.invalidateEndpoints();
                if (network.isValid()) {
                    this.link = network.getOtherEndpoint(this);
                }
            }
        }
        return this.link;
    }

    @Override
    public void invalidateLink() {
        this.link = null;
    }

    @Override
    public @NotNull EnumFacing getOutputFacing() {
        return getFrontFacing().getOpposite();
    }

    @Override
    public @NotNull BeamLineType getBeamLineType() {
        return beamLineType;
    }

    @Override
    public boolean getIsWeatherOrTerrainResistant() {
        return true;
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.endpoint.tooltip.1"));
        tooltip.add(I18n.format("gregtech.machine.endpoint.tooltip.2"));
        tooltip.add(I18n.format("gregtech.machine.endpoint.tooltip.3"));
        if (beamLineType.getMinLength() > 0) {
            tooltip.add(I18n.format("gregtech.machine.endpoint.tooltip.min_length", beamLineType.getMinLength()));
        }
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    @NotNull
    @Override
    public List<ITextComponent> getDataInfo() {
        List<ITextComponent> textComponents = new ArrayList<>();
        ParticleNetwork network = ParticleNetwork.get(getWorld(), getPos());
        if (network == null) {
            textComponents.add(new TextComponentString("No network found"));
        } else {
            textComponents.add(new TextComponentString("Network:"));
            textComponents.add(new TextComponentString(" - " + network.getTotalSize() + " pipes"));
            IBeamLineEndpoint in = network.getActiveInputIndex(), out = network.getActiveOutputIndex();
            textComponents.add(new TextComponentString(" - input: " + (in == null ? "none" : in.pos())));
            textComponents.add(new TextComponentString(" - output: " + (out == null ? "none" : out.pos())));
        }
        if (isInput()) {
            textComponents.add(new TextComponentString("Input endpoint"));
        }
        if (isOutput()) {
            textComponents.add(new TextComponentString("Output endpoint"));
        }
        if (isCollider()) {
            textComponents.add(new TextComponentString("Collider endpoint"));
        }
        return textComponents;
    }

    @Override
    public World world() {
        return getWorld();
    }

    @Override
    public BlockPos pos() {
        return getPos();
    }

    @Override
    public void onNeighborChanged(@NotNull EnumFacing facing) {}

    @Override
    public void markAsDirty() {}


}
