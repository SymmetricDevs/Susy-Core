package supersymmetry.common.metatileentities.multiblockpart;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.*;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.IFastRenderMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.ICleanroomProvider;
import gregtech.api.metatileentity.multiblock.ICleanroomReceiver;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.metatileentity.multiblock.MultiblockDisplayText;
import gregtech.api.util.TextComponentUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.RenderBufferHelper;
import gregtech.client.utils.RenderUtil;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;
import supersymmetry.api.capability.SuSyDataCodes;
import supersymmetry.api.capability.impl.ScannerLogic;
import supersymmetry.api.rocketry.components.AbstractComponent;
import supersymmetry.api.util.DataStorageLoader;
import supersymmetry.api.util.StructAnalysis;
import supersymmetry.api.util.StructAnalysis.BuildStat;
import supersymmetry.common.item.SuSyMetaItems;
import supersymmetry.common.metatileentities.multi.rocket.MetaTileEntityBuildingCleanroom;

public class MetaTileEntityComponentScanner extends MetaTileEntityMultiblockPart
                                            implements
                                            ICleanroomReceiver,
                                            IFastRenderMetaTileEntity,
                                            IWorkable {

    private final ScannerLogic scannerLogic;
    private float scanDuration = 0;
    private MetaTileEntityBuildingCleanroom linkedCleanroom;
    private BuildStat shownStatus;
    private BlockPos errorPos = null;

    // If the current bounding box should be rendered
    protected boolean renderBoundingBox = false;
    protected boolean highlightSelectedStock = false;

    public StructAnalysis struct;

    public MetaTileEntityComponentScanner(ResourceLocation mteId) {
        super(mteId, 0); // it kind of is and isn't
        shownStatus = BuildStat.UNSCANNED;

        struct = new StructAnalysis(getWorld());
        importItems = new DataStorageLoader(this, is -> {
            int metaV = SuSyMetaItems.isMetaItem(is);
            return metaV == SuSyMetaItems.DATA_CARD.metaValue || metaV == SuSyMetaItems.DATA_CARD_ACTIVE.metaValue;
        });
        if (importItems.getStackInSlot(0).isItemEqual(ItemStack.EMPTY)) {
            shownStatus = BuildStat.NO_CARD;
        }
        scannerLogic = new ScannerLogic(this);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityComponentScanner(this.metaTileEntityId);
    }

    public DataStorageLoader getInventory() {
        return (DataStorageLoader) importItems;
    }

    @Override
    public boolean canPartShare() {
        return false;
    }

    public void scanPart() {
        getInventory().clearNBT();
        struct = new StructAnalysis(getWorld());
        if (linkedCleanroom == null || !linkedCleanroom.isClean()) {
            struct.status = BuildStat.UNCLEAN;
            return;
        }
        AxisAlignedBB interior = linkedCleanroom.getInteriorBB();
        int solidBlocks = 0;
        ArrayList<BlockPos> blockList = struct.getBlocks(getWorld(), interior, true);

        if (blockList == null) { // error propagated
            return;
        } else if (blockList.isEmpty()) {
            this.struct.status = BuildStat.EMPTY;
            return;
        }

        scanDuration = (int) (blockList.size() / (Math.pow(2, linkedCleanroom.getEnergyTier() - 1))) + 4; // 5 being the
        // minimum
        // value
        scannerLogic.setGoalTime(scanDuration);

        Set<BlockPos> blocksConnected = struct.getBlockConn(interior, blockList.get(0));
        struct.status = BuildStat.SCANNING;

        if (blocksConnected.size() != blockList.size()) {
            this.struct.status = BuildStat.DISCONNECTED;
            return;
        }

        detectComponents(blockList);
        // if it wasnt changed after scanning, nothing matched
        if (struct.status == BuildStat.SCANNING) {
            struct.status = BuildStat.UNRECOGNIZED;
        }
    }

    public void detectComponents(ArrayList<BlockPos> blockList) {
        for (AbstractComponent<?> component : AbstractComponent.getRegistry()) {
            if (component.getDetectionPredicate().test(new Tuple<>(struct, blockList))) {
                Optional<NBTTagCompound> scanResult = component.analyzePattern(struct, linkedCleanroom.getInteriorBB());
                if (scanResult.isPresent()) {
                    if (scanResult.get().hasKey("errorPos")) {
                        this.errorPos = BlockPos.fromLong(scanResult.get().getLong("errorPos"));
                        continue;
                    }
                    getInventory().setNBT(t -> {
                        NBTTagCompound tag = scanResult.get();
                        tag.setInteger("dataid", (int) (Math.random() * Integer.MAX_VALUE));
                        component.writeToNBT(tag);
                        return tag;
                    });
                    updateErrorPos(null);
                    break;
                } else {
                    updateErrorPos(null);
                }
            }
        }
    }

    public void handleScan(Widget.ClickData click) {
        updateErrorPos(null);
        if (linkedCleanroom == null || !linkedCleanroom.isClean()) {
            struct.status = BuildStat.UNCLEAN;
        }
        if (this.isWorkingEnabled()) {
            if (importItems.getStackInSlot(0).isEmpty()) {
                struct.status = BuildStat.NO_CARD;
                finishScan();
                return;
            }
            scannerLogic.setWorkingEnabled(true);
            scannerLogic.setActive(true);

            getInventory().setLocked(true);
            struct.status = BuildStat.SCANNING;
            shownStatus = struct.status;
            scanPart();
            if (struct.status != BuildStat.SUCCESS) {
                getInventory().clearNBT();
            }
        }
    }

    @Override
    public @Nullable ICleanroomProvider getCleanroom() {
        return linkedCleanroom;
    }

    @Override
    public void setCleanroom(ICleanroomProvider iCleanroomProvider) {
        if (iCleanroomProvider instanceof MetaTileEntityBuildingCleanroom)
            linkedCleanroom = (MetaTileEntityBuildingCleanroom) iCleanroomProvider;
    }

    @Override
    public boolean isWorkingEnabled() {
        return scannerLogic.isWorkingEnabled() && linkedCleanroom != null && linkedCleanroom.isWorkingEnabled();
    }

    public void setWorkingEnabled(boolean isActivationAllowed) {
        this.scannerLogic.setWorkingEnabled(isActivationAllowed);
    }

    public boolean drainEnergy(boolean simulate) {
        if (linkedCleanroom == null) {
            return false;
        }
        IEnergyContainer energyContainer = linkedCleanroom.getEnergyContainer();
        long resultEnergy = energyContainer.getEnergyStored() - scannerLogic.getInfoProviderEUt();
        if (resultEnergy >= 0L && resultEnergy <= energyContainer.getEnergyCapacity()) {
            if (!simulate)
                energyContainer.changeEnergy(-scannerLogic.getInfoProviderEUt());
            return true;
        }
        return false;
    }

    public void update() {
        if (!getWorld().isRemote) {
            this.scannerLogic.updateLogic();
        }
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.LOCK_OBJECT_HOLDER) {
            getInventory().setLocked(buf.readBoolean());
        }
        if (dataId == GregtechDataCodes.WORKABLE_ACTIVE) {
            scannerLogic.setActive(buf.readBoolean());
        }
        if (dataId == SuSyDataCodes.UPDATE_HIGHLIGHT_POS) {
            this.errorPos = buf.readBlockPos();
        }
        if (dataId == SuSyDataCodes.REMOVE_HIGHLIGHT_POS) {
            this.errorPos = null;
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_WORKABLE) {
            return GregtechTileCapabilities.CAPABILITY_WORKABLE.cast(this);
        } else {
            return capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE ?
                    GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this) : super.getCapability(capability, side);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, this.getFrontFacing(),
                this.isActive(), this.isWorkingEnabled());
    }

    public void renderMetaTileEntity(double x, double y, double z, float partialTicks) {
        if (this.errorPos != null) {
            BlockPos realPos = this.getHolder().pos();
            float resultDist = (float) new Vec3d(errorPos).distanceTo(Minecraft.getMinecraft().player.getPositionEyes(partialTicks));
            GlStateManager.pushMatrix();
            GlStateManager.disableDepth();
            GlStateManager.enableBlend();
            GlStateManager.disableLighting();
            GlStateManager.disableTexture2D();
            GlStateManager.blendFunc(770, 771);
            GlStateManager.glLineWidth(10/resultDist);

            GlStateManager.translate(x, y, z);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

            RenderBufferHelper.renderCubeFrame(buffer, errorPos.getX() - realPos.getX(),
                    errorPos.getY() - realPos.getY(),
                    errorPos.getZ() - realPos.getZ(),
                    errorPos.getX() - realPos.getX()+1, errorPos.getY()-realPos.getY()+1,
                    errorPos.getZ()-realPos.getZ()+1, 1, 0, 0, 0.8F);
            tessellator.draw();

            GlStateManager.disableBlend();
            GlStateManager.enableDepth();
            GlStateManager.enableLighting();
            GlStateManager.enableTexture2D();

            GlStateManager.popMatrix();
        }
    }

    @Override
    public ICubeRenderer getBaseTexture() {
        MultiblockControllerBase controller = getController();
        if (controller != null) {
            return this.hatchTexture = controller.getBaseTexture(this);
        } else {
            return Textures.VOLTAGE_CASINGS[4];
        }
    }

    public ICubeRenderer getFrontOverlay() {
        return Textures.RESEARCH_STATION_OVERLAY;
    }

    public void finishScan() {
        if (struct.status == BuildStat.SUCCESS) {
            getInventory().setImageType(SuSyMetaItems.DATA_CARD_ACTIVE.metaValue); // is this cursed? yes
        }
        updateErrorPos(this.errorPos);
        getInventory().setLocked(false);
        shownStatus = struct.status;
    }

    @Override
    public int getProgress() {
        return (int) scannerLogic.getProgress();
    }

    @Override
    public int getMaxProgress() {
        return (int) scannerLogic.getMaxProgress();
    }

    @Override
    public boolean isActive() {
        return scannerLogic.isActive();
    }

    protected void modifyItem(String key, String value) {
        getInventory().mutateItem(key, value);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return this.createGUITemplate(entityPlayer).build(this.getHolder(), entityPlayer);
    }

    protected void addDisplayText(List<ITextComponent> textList) {
        MultiblockDisplayText.builder(textList, this.getCleanroom() != null)
                .setWorkingStatus(this.isWorkingEnabled(), this.isActive())
                .addEnergyUsageLine(linkedCleanroom.getEnergyContainer()).addCustom((tl) -> {
                    if (linkedCleanroom != null) {
                        TextComponentTranslation cleanState;
                        if (scannerLogic.isActive() || struct.status == BuildStat.SCANNING) {
                            tl.add(TextComponentUtil.translationWithColor(TextFormatting.YELLOW,
                                    "susy.machine.component_scanner.scanning"));
                        } else if (shownStatus == BuildStat.SUCCESS) {
                            tl.add(TextComponentUtil.translationWithColor(TextFormatting.GREEN,
                                    "susy.machine.component_scanner.success"));

                        } else if (shownStatus == BuildStat.UNSCANNED) {
                            tl.add(TextComponentUtil.translationWithColor(TextFormatting.GRAY,
                                    "susy.machine.component_scanner.unscanned"));
                        } else {
                            tl.add(TextComponentUtil.translationWithColor(TextFormatting.DARK_RED,
                                    shownStatus.getCode()));
                        }
                    }
                }).addCustom((tl) -> {
                    if (errorPos != null && !(scannerLogic.isActive() || struct.status == BuildStat.SCANNING)) {
                        tl.add(TextComponentUtil.translationWithColor(TextFormatting.GRAY,
                                "susy.machine.component_scanner.pos",
                                "(" + errorPos.getX() + ", " + errorPos.getY() + ", " + errorPos.getZ() + ")"));
                    }
                }).addProgressLine(this.scannerLogic.getProgressPercent());
    }

    private ModularUI.Builder createGUITemplate(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 198, 208);
        // Spliced from MultiblockWithDisplayBase
        // Display
        builder.image(4, 4, 190, 109, GuiTextures.DISPLAY);

        // single bar
        ProgressWidget progressBar = new ProgressWidget(scannerLogic::getProgressPercent, 4, 115, 190, 7,
                GuiTextures.PROGRESS_BAR_MULTI_ENERGY_YELLOW, ProgressWidget.MoveType.HORIZONTAL)
                .setHoverTextConsumer(list -> addBarHoverText(list, 0));
        builder.widget(progressBar);

        builder.widget(new IndicatorImageWidget(174, 93, 17, 17, GuiTextures.GREGTECH_LOGO_DARK)
                .setWarningStatus(GuiTextures.GREGTECH_LOGO_BLINKING_YELLOW, this::addWarningText)
                .setErrorStatus(GuiTextures.GREGTECH_LOGO_BLINKING_RED, this::addErrorText));

        builder.label(9, 9, getMetaFullName(), 0xFFFFFF);
        builder.widget(new AdvancedTextWidget(9, 20, this::addDisplayText, 0xFFFFFF).setMaxWidthLimit(181)
                .setClickHandler(this::handleDisplayClick));

        // Scan Button
        builder.widget(new ClickButtonWidget(68, 75, 54, 18,
                new TextComponentTranslation("susy.machine.component_scanner.scan_button")
                        .getUnformattedComponentText(),
                this::handleScan)).slot(importItems, 0, 90, 95, GuiTextures.SLOT);

        builder.bindPlayerInventory(entityPlayer.inventory, 125);
        return builder;
    }

    private void handleDisplayClick(String s, Widget.ClickData clickData) {}

    private void addBarHoverText(List<ITextComponent> list, int i) {}

    private void addWarningText(List<ITextComponent> iTextComponents) {
        if (struct.status == BuildStat.UNSCANNED) {
            iTextComponents.add(new TextComponentTranslation(BuildStat.UNSCANNED.getCode()));
        }
    }

    private void addErrorText(List<ITextComponent> iTextComponents) {
        if (struct.status != BuildStat.SUCCESS && struct.status != BuildStat.SCANNING &&
                struct.status != BuildStat.UNSCANNED) {
            iTextComponents.add(new TextComponentTranslation(BuildStat.UNSCANNED.getCode()));
        }
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getPos()).grow(32);
    }

    protected void updateErrorPos(BlockPos bp) {
        this.errorPos = bp;
        if (bp != null)
            writeCustomData(SuSyDataCodes.UPDATE_HIGHLIGHT_POS, packetBuffer -> packetBuffer.writeBlockPos(errorPos));
        else
            writeCustomData(SuSyDataCodes.REMOVE_HIGHLIGHT_POS, packetBuffer -> {});
        scheduleRenderUpdate();
    }
}
