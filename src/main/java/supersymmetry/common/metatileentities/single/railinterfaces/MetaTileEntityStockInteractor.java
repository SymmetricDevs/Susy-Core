package supersymmetry.common.metatileentities.single.railinterfaces;

import cam72cam.immersiverailroading.entity.EntityRollingStock;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.CycleButtonWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Flow;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.metatileentity.IFastRenderMetaTileEntity;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.utils.RenderBufferHelper;
import gregtech.client.utils.RenderUtil;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import supersymmetry.api.SusyLog;
import supersymmetry.api.gui.SusyGuiTextures;
import supersymmetry.api.metatileentity.Mui2MetaTileEntity;
import supersymmetry.api.stockinteraction.IStockInteractor;
import supersymmetry.api.stockinteraction.StockFilter;
import supersymmetry.api.stockinteraction.StockHelperFunctions;
import supersymmetry.client.renderer.textures.SusyTextures;

import java.io.IOException;

public abstract class MetaTileEntityStockInteractor extends Mui2MetaTileEntity implements IStockInteractor, IFastRenderMetaTileEntity, IControllable {

    AxisAlignedBB interactionBoundingBox;
    private double interactionWidth = 11.;
    private double interactionDepth = 5.;

    // This defines which stock classes can be interacted with
    protected StockFilter stockFilter;

    @Nullable
    protected EntityRollingStock stock;

    protected boolean workingEnabled = true;
    // Rendering
    protected final ICubeRenderer renderer;

    // If the current bounding box should be rendered
    protected boolean renderBoundingBox = false;


    public MetaTileEntityStockInteractor(ResourceLocation metaTileEntityId, ICubeRenderer renderer) {
        super(metaTileEntityId);
        this.renderer = renderer;
        this.stockFilter = new StockFilter(9);
    }

    @Override
    public void update() {
        super.update();

        if (this.getWorld().isRemote)
            return;

        if(this.getOffsetTimer() % 20 == 0 && this.isWorkingEnabled()) {
            // Do the filtering later?
            this.stock = StockHelperFunctions.getStockFrom(getWorld(), getInteractionBoundingBox(), stockFilter);
        }
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        this.workingEnabled = workingEnabled;
        World world = this.getWorld();
        if (world != null && !world.isRemote) {
            this.writeCustomData(GregtechDataCodes.WORKING_ENABLED, (buf) -> {
                buf.writeBoolean(workingEnabled);
            });
        }
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.workingEnabled;
    }

    protected abstract <T> T getStockCapability(Capability<T> capability, EnumFacing side);

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        if (stock != null && !stock.isDead()) {
            T stockCapability = getStockCapability(capability, side);
            if (stockCapability != null) {
                return stockCapability;
            }
        }
        return super.getCapability(capability, side);
    }

    @Override
    public boolean needsSneakToRotate() {
        return true;
    }

    // UI
    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager syncManager) {

        PanelSyncHandler panel = (PanelSyncHandler) syncManager.panel("filter_panel",
                (panelSyncManager, syncHandler) -> stockFilter.createPopupPanel(panelSyncManager),
                true);

        BooleanSyncValue workingStateValue = new BooleanSyncValue(() -> workingEnabled, val -> workingEnabled = val);
        BooleanSyncValue renderBoundingBoxValue = new BooleanSyncValue(() -> renderBoundingBox, val -> renderBoundingBox = val);

        return defaultPanel(this)
                .child(IKey.lang(getMetaFullName()).asWidget().pos(5, 5))
                .child(SlotGroupWidget.playerInventory().left(7).bottom(7))
                .child(getLogo().asWidget().size(17).right(7).bottom(88))
                .child(new CycleButtonWidget()
                        .left(7).bottom(90)
                        .overlay(SusyGuiTextures.RENDER_AREA_OVERLAY.asIcon().size(16))
                        .value(workingStateValue))
                .child(Flow.column().top(18).margin(7, 0)
                        .widthRel(1f).coverChildrenHeight()
                        .child(Flow.row().coverChildrenHeight()
                                .marginBottom(2).widthRel(1f)
                                .child(new ToggleButton()
                                        .overlay(SusyGuiTextures.RENDER_AREA_OVERLAY.asIcon().size(16))
                                        .value(renderBoundingBoxValue))
                                .child(IKey.lang("Render AABB").asWidget()
                                        .align(Alignment.CenterRight).height(18))
                        )
                        .child(Flow.row().coverChildrenHeight()
                                .marginBottom(2).widthRel(1f)
                                .child(new ButtonWidget<>()
                                        .overlay(SusyGuiTextures.FILTER_SETTINGS_OVERLAY.asIcon().size(16))
                                        .onMousePressed(mouseButton -> {
                                            if (!panel.isPanelOpen()) {
                                                panel.openPanel();
                                            } else {
                                                panel.closePanel();
                                            }
                                            return true;
                                        })
                                )
                                .child(IKey.lang("Stock Filter").asWidget()
                                        .align(Alignment.CenterRight).height(18))
                        )
                );
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(double x, double y, double z, float partialTicks) {
        if(this.shouldRenderBoundingBox()) {
            GlStateManager.pushMatrix();

            RenderUtil.moveToFace(x,y,z,getFrontFacing());
            GlStateManager.translate(0, -.5, 0);
            RenderUtil.rotateToFace(getFrontFacing(), null);

            GlStateManager.enableBlend();
            GlStateManager.disableLighting();
            GlStateManager.disableTexture2D();
            GlStateManager.blendFunc(770, 771);
            GlStateManager.glLineWidth(15);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

            RenderBufferHelper.renderCubeFrame(buffer, -this.getInteractionWidth() / 2, 0, 0, this.getInteractionWidth() / 2,  (-this.getInteractionDepth() - this.getInteractionWidth()) / 2, this.getInteractionDepth(), 1, 0, 0, 0.6F);
            tessellator.draw();

            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
            GlStateManager.enableTexture2D();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            GlStateManager.popMatrix();

        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        SusyTextures.STOCK_MACHINE_CASING.render(renderState,translation,pipeline);
        this.renderer.renderOrientedState(renderState, translation, pipeline, this.getFrontFacing(), true, this.isWorkingEnabled());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(SusyTextures.STOCK_MACHINE_CASING.getParticleSprite(), getPaintingColorForRendering());
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return this.getInteractionBoundingBox();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("stockFilter", this.stockFilter.serializeNBT());
        data.setDouble("interactionWidth", this.interactionWidth);
        data.setDouble("interactionDepth", this.interactionDepth);
        data.setBoolean("renderBoundingBox", this.renderBoundingBox);
        data.setBoolean("workingEnabled", this.workingEnabled);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.stockFilter.deserializeNBT(data.getCompoundTag("stockFilter"));
        this.setInteractionWidth(data.getDouble("interactionWidth"));
        this.setInteractionDepth(data.getDouble("interactionDepth"));
        this.renderBoundingBox = data.getBoolean("renderBoundingBox");
        this.workingEnabled = data.getBoolean("workingEnabled");
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeCompoundTag(this.stockFilter.serializeNBT());
        buf.writeDouble(this.getInteractionWidth());
        buf.writeDouble(this.getInteractionDepth());
        buf.writeBoolean(this.renderBoundingBox);
        buf.writeBoolean(this.workingEnabled);
    }

    @Override
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        try {
            //noinspection DataFlowIssue
            this.stockFilter.deserializeNBT(buf.readCompoundTag());
        } catch (IOException e) {
            SusyLog.logger.info("Could not deserialize stock stockFilter in stock interactor at {}", getPos());
            SusyLog.logger.error(e);
        }
        this.setInteractionWidth(buf.readDouble());
        this.setInteractionDepth(buf.readDouble());
        this.renderBoundingBox = buf.readBoolean();
        this.workingEnabled = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);

        if(dataId == 6500) {
            this.renderBoundingBox = buf.readBoolean();
        }

        // Front facing changed
        if(dataId == -2) {
            this.recalculateBoundingBox();
        }

        if(dataId == GregtechDataCodes.WORKING_ENABLED) {
            this.workingEnabled = buf.readBoolean();
        }
        this.scheduleRenderUpdate();

    }

    @Override
    public AxisAlignedBB getInteractionBoundingBox() {
        return interactionBoundingBox == null ? interactionBoundingBox = StockHelperFunctions.getBox(this.getPos(), this.getFrontFacing(), this.getInteractionWidth(), this.getInteractionDepth()) : interactionBoundingBox;
    }

    public void recalculateBoundingBox() {
        this.interactionBoundingBox = StockHelperFunctions.getBox(this.getPos(), this.getFrontFacing(), this.getInteractionWidth(), this.getInteractionDepth());
    }

    public double getInteractionWidth() {
        return interactionWidth;
    }

    public void setInteractionWidth(double interactionWidth) {
        this.interactionWidth = interactionWidth;
        this.recalculateBoundingBox();
    }

    public double getInteractionDepth() {
        return interactionDepth;
    }

    public void setInteractionDepth(double interactionDepth) {
        this.interactionDepth = interactionDepth;
        this.recalculateBoundingBox();
    }

    public boolean shouldRenderBoundingBox() {
        return this.renderBoundingBox;
    }

    public void setRenderBoundingBox(boolean renderBoundingBox) {
        if(this.renderBoundingBox != renderBoundingBox) {
            this.renderBoundingBox = renderBoundingBox;
            if (!this.getWorld().isRemote) {
                this.writeCustomData(6500, (buf -> buf.writeBoolean(this.renderBoundingBox)));
                this.markDirty();
            }
        }
    }
}
