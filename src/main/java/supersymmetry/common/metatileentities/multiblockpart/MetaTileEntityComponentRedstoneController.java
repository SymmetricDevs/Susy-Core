package supersymmetry.common.metatileentities.multiblockpart;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.DynamicLabelWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.IncrementButtonWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;
import supersymmetry.api.metatileentity.multiblock.IRedstoneControllable;
import supersymmetry.client.renderer.textures.SusyTextures;

public class MetaTileEntityComponentRedstoneController extends MetaTileEntityMultiblockPart {

    public static TraceabilityPredicate controllerPredicate() {
        return (new TraceabilityPredicate(
                (blockWorldState -> {
                    TileEntity tile = blockWorldState.getTileEntity();
                    if (tile instanceof MetaTileEntityHolder) {
                        MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) tile).getMetaTileEntity();
                        if (metaTileEntity instanceof MetaTileEntityComponentRedstoneController) {
                            Set<IMultiblockPart> partsFound = blockWorldState.getMatchContext()
                                    .getOrCreate("MultiblockParts", HashSet::new);
                            partsFound.add((IMultiblockPart) metaTileEntity);

                            return true;
                        }
                    }
                    return false;
                })));
        // return MultiblockControllerBase.tilePredicate((state,mte) -> {return true;}, () -> { return
        // })
    }

    // //decided to not do that yet
    // public static enum RedstoneControllerMode {
    // Pulse,
    // Continious
    // }

    // public RedstoneControllerMode mode = RedstoneControllerMode.Pulse;
    public int signal = 0;

    boolean pulledUp = false;

    public MetaTileEntityComponentRedstoneController(ResourceLocation mteId) {
        super(mteId, GTValues.HV);
    }

    public void changeSignal(int delta) {
        if (this.getController() != null && this.getController() instanceof IRedstoneControllable controllable) {

            int newsig = Math.floorMod(this.signal + delta, controllable.getSignalCeiling() + 1);
            if (newsig != this.signal && newsig >= 0) {
                this.writeCustomData(
                        102,
                        (buf) -> {
                            buf.writeInt(newsig);
                            this.signal = newsig;
                        });
            }
        }
    }

    // ran after the value is reset
    public void pulse() {
        MultiblockControllerBase controller = this.getController();
        if (controller != null && controller instanceof IRedstoneControllable receiver) {
            if (receiver.redstoneControlEnabled() && receiver.getSignalCeiling() >= this.signal) {
                receiver.pulse(this.signal);
            }
        }
    }

    @Override
    public void renderMetaTileEntity(
                                     CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        getOverlay().renderSided(getFrontFacing(), renderState, translation, pipeline);
    }

    private SimpleOverlayRenderer getOverlay() {
        return SusyTextures.REDSTONE_CONTROLLER_OVERLAY;
    }

    @Override
    public boolean canPlaceCoverOnSide(@NotNull EnumFacing side) {
        return false;
    }

    @Override
    public boolean canPartShare() {
        return false;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityComponentRedstoneController(this.metaTileEntityId);
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == 102) {
            this.signal = buf.readInt();
        }
        if (dataId == 103) {
            this.pulledUp = true;
        }
        if (dataId == 104) {
            this.pulledUp = false;
        }
    }

    @Override
    public void updateInputRedstoneSignals() {
        super.updateInputRedstoneSignals();
        int val = this.getInputRedstoneSignal(this.frontFacing, true);
        if (pulledUp) {
            if (val == 0) {
                pulledUp = false;
                writeCustomData(104, buf -> {});
            }
        } else {
            if (val != 0) {
                pulse();
                pulledUp = true;
                writeCustomData(103, buf -> {});
            }
        }
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return this.createGUITemplate(entityPlayer).build(this.getHolder(), entityPlayer);
    }

    @Override
    protected boolean canMachineConnectRedstone(EnumFacing side) {
        return this.frontFacing == side;
    }

    // @Override
    // public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation,
    // IVertexOperation[] pipeline) {
    // super.renderMetaTileEntity(renderState, translation, pipeline);
    // this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline,
    // this.getFrontFacing(),
    // this.isActive(), this.isWorkingEnabled());
    // }
    // public ICubeRenderer getFrontOverlay() {
    // return Textures.RESEARCH_STATION_OVERLAY;
    // }

    private ModularUI.Builder createGUITemplate(EntityPlayer entityPlayer) {
        MultiblockControllerBase controller = this.getController();
        if (controller != null && controller instanceof IRedstoneControllable receiver) {
            int limit = receiver.getSignalCeiling();
            ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 190, 130);
            ImageWidget screen = new ImageWidget(5, 5, 180, 120, GuiTextures.DISPLAY);

            IncrementButtonWidget up = new IncrementButtonWidget(7, 7, 20, 20, 1, 4, 16, 64, this::changeSignal);
            IncrementButtonWidget down = new IncrementButtonWidget(163, 7, 20, 20, -1, -4, -16, -64,
                    this::changeSignal);
            DynamicLabelWidget sig = new DynamicLabelWidget(
                    27,
                    11,
                    () -> {
                        return I18n.format(
                                this.getMetaName() + ".signal_label",
                                Integer.toString(signal),
                                Integer.toString(limit));
                    },
                    0xffffff);

            DynamicLabelWidget name = new DynamicLabelWidget(
                    8,
                    27,
                    () -> {
                        int n = 34;
                        String s = I18n.format(
                                this.getMetaName() + ".signal_desc",
                                I18n.format(
                                        controller.getMetaName() + ".signal." + receiver.getSignalName(this.signal)));

                        return IntStream.range(0, (s.length() + n - 1) / n)
                                .mapToObj(i -> s.substring(i * n, Math.min(s.length(), (i + 1) * n)))
                                .collect(Collectors.joining("\n"));
                    },
                    0xffffff);
            DynamicLabelWidget status = new DynamicLabelWidget(
                    8,
                    110,
                    () -> {
                        return I18n.format(
                                this.getMetaName() + ".pulled." + Boolean.toString(this.pulledUp));
                    });
            builder.widget(screen);
            builder.widget(sig);
            builder.widget(up);
            builder.widget(down);
            builder.widget(name);
            builder.widget(status);
            return builder;
        } else {
            ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 165, 35);
            builder.widget(new ImageWidget(4, 4, 157, 27, GuiTextures.DISPLAY));
            builder.label(9, 12, this.getMetaName() + ".not_connected", 0xAE5421);
            return builder;
        }
    }

    @Override
    protected boolean shouldSerializeInventories() {
        return false;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        this.signal = data.getInteger("signal");
        this.pulledUp = data.getBoolean("state");
        super.readFromNBT(data);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data = super.writeToNBT(data);
        data.setInteger("signal", this.signal);
        data.setBoolean("state", this.pulledUp);
        return super.writeToNBT(data);
    }
}
