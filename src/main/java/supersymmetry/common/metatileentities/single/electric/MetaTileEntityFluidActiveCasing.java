package supersymmetry.common.metatileentities.single.electric;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.Nullable;

import codechicken.lib.render.BlockRenderer.BlockFace;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.TransformationList;
import codechicken.lib.vec.Vertex5;
import codechicken.lib.vec.uv.IconTransformation;
import codechicken.lib.vec.uv.UVTransformationList;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.NotifiableFluidTank;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.ImageCycleButtonWidget;
import gregtech.api.gui.widgets.SimpleTextWidget;
import gregtech.api.gui.widgets.PhantomFluidWidget;
import gregtech.api.gui.widgets.PhantomSlotWidget;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.CubeRendererState;
import gregtech.client.renderer.texture.Textures;
import supersymmetry.client.event.ActiveFluidVisualHandler;
import supersymmetry.client.renderer.handler.BlockSkinRenderer;
import supersymmetry.client.renderer.particles.SusyParticleFrothBubble;
import supersymmetry.client.renderer.textures.custom.VatCasingRenderer;

public class MetaTileEntityFluidActiveCasing extends MetaTileEntity implements IControllable {

    public enum EffectMode implements IStringSerializable {

        CLARIFIER("clarifier"),
        FROTH_FLOTATION("froth_flotation");

        private final String name;

        EffectMode(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    private static final Cuboid6 FLUID_RENDER_CUBOID_THIN = new Cuboid6(0, 1, 0, 1, 1 + 3 / 16F, 1);

    private Cuboid6 getFluidRenderCuboid() {
        if (fluidDisplaySize <= 0) return FLUID_RENDER_CUBOID_THIN;
        return new Cuboid6(0, 1, 0, 1, 1 + fluidDisplaySize, 1);
    }

    public static final int UPDATE_FLUID_INFO = GregtechDataCodes.assignId();
    public static final int CHANGE_FLUID_RENDER_STATUS = GregtechDataCodes.assignId();
    public static final int UPDATE_EFFECT_MODE = GregtechDataCodes.assignId();
    public static final int UPDATE_STORED_BLOCK = GregtechDataCodes.assignId();
    public static final int UPDATE_FLUID_DISPLAY_SIZE = GregtechDataCodes.assignId();

    private static final int MIN_DISPLAY_SIZE = 0;

    private final VatCasingRenderer defaultRenderer;
    private final EffectMode defaultMode;
    private final int tankSize;

    private NotifiableFluidTank fluidTank;
    private IItemHandlerModifiable blockSlot;
    private EffectMode effectMode;
    private boolean isWorkingEnabled = true;
    private boolean isActive;
    private int fluidDisplaySize;
    private FluidStack lastSyncedFluid;
    private Block lastSyncedBlock;
    private int lastSyncedMeta;

    @SideOnly(Side.CLIENT)
    private TextureAtlasSprite fluidTexture;
    private int fluidColor;
    private boolean renderFluid = false;

    public MetaTileEntityFluidActiveCasing(ResourceLocation metaTileEntityId,
                                           VatCasingRenderer defaultRenderer,
                                           EffectMode defaultMode,
                                           int tankSize) {
        super(metaTileEntityId);
        this.defaultRenderer = defaultRenderer;
        this.defaultMode = defaultMode;
        this.effectMode = defaultMode;
        this.tankSize = tankSize;
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityFluidActiveCasing(metaTileEntityId, defaultRenderer, defaultMode, tankSize);
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        this.fluidTank = new NotifiableFluidTank(tankSize, this, false);
        return new FluidTankList(false, fluidTank);
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        this.blockSlot = new GTItemStackHandler(this, 1) {

            @Override
            public boolean isItemValid(int slot, net.minecraft.item.ItemStack stack) {
                return !stack.isEmpty() && stack.getItem() instanceof ItemBlock;
            }
        };
        return blockSlot;
    }

    @Override
    public boolean isWorkingEnabled() {
        return isWorkingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean b) {
        this.isWorkingEnabled = b;
        this.writeCustomData(GregtechDataCodes.WORKING_ENABLED, buf -> buf.writeBoolean(b));
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public boolean canRenderInLayer(BlockRenderLayer layer) {
        return true;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE)
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        return super.getCapability(capability, side);
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            boolean hasFluid = fluidTank.getFluidAmount() > 0;
            boolean newActive = hasFluid && isWorkingEnabled;
            if (newActive != isActive) {
                isActive = newActive;
                writeCustomData(GregtechDataCodes.IS_WORKING, buf -> buf.writeBoolean(newActive));
            }

            FluidStack fluid = fluidTank.getFluid();
            boolean fluidChanged = lastSyncedFluid == null ? fluid != null :
                    (fluid == null || !fluid.isFluidStackIdentical(lastSyncedFluid));
            if (fluidChanged) {
                lastSyncedFluid = fluid == null ? null : fluid.copy();
                if (fluid != null && fluid.amount > 0) {
                    writeCustomData(UPDATE_FLUID_INFO, buf -> {
                        buf.writeInt(fluid.getFluid().getColor(fluid));
                        buf.writeResourceLocation(fluid.getFluid().getStill(fluid));
                    });
                    writeCustomData(CHANGE_FLUID_RENDER_STATUS, buf -> buf.writeBoolean(true));
                } else {
                    writeCustomData(CHANGE_FLUID_RENDER_STATUS, buf -> buf.writeBoolean(false));
                }
            }

            syncBlockSlot();
        }

        if (getWorld().isRemote && isActive && isWorkingEnabled && effectMode == EffectMode.FROTH_FLOTATION && renderFluid) {
            renderParticles();
        }
    }

    private void syncBlockSlot() {
        ItemStack stack = blockSlot == null ? ItemStack.EMPTY : blockSlot.getStackInSlot(0);
        Block newBlock;
        int newMeta;
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemBlock)) {
            newBlock = null;
            newMeta = 0;
        } else {
            newBlock = Block.getBlockFromItem(stack.getItem());
            newMeta = stack.getMetadata();
        }
        if (newBlock != lastSyncedBlock || newMeta != lastSyncedMeta) {
            lastSyncedBlock = newBlock;
            lastSyncedMeta = newMeta;
            writeCustomData(UPDATE_STORED_BLOCK, buf -> {
                buf.writeBoolean(newBlock != null);
                if (newBlock != null) {
                    buf.writeResourceLocation(Block.REGISTRY.getNameForObject(newBlock));
                    buf.writeVarInt(newMeta);
                }
            });
        }
    }

    public Block getStoredBlock() {
        if (getWorld() != null && !getWorld().isRemote) {
            if (blockSlot == null) return null;
            ItemStack stack = blockSlot.getStackInSlot(0);
            if (stack.isEmpty() || !(stack.getItem() instanceof ItemBlock)) return null;
            return Block.getBlockFromItem(stack.getItem());
        }
        return lastSyncedBlock;
    }

    public int getStoredMeta() {
        if (getWorld() != null && !getWorld().isRemote) {
            if (blockSlot == null) return 0;
            ItemStack stack = blockSlot.getStackInSlot(0);
            if (stack.isEmpty() || !(stack.getItem() instanceof ItemBlock)) return 0;
            return stack.getMetadata();
        }
        return lastSyncedMeta;
    }

    @SideOnly(Side.CLIENT)
    private void renderParticles() {
        Random rand = getWorld().rand;
        BlockPos pos = getPos();
        double topY = fluidDisplaySize > 0 ? 1 + fluidDisplaySize : 1 + 3.0 / 16.0;
        Minecraft.getMinecraft().effectRenderer.addEffect(
                new SusyParticleFrothBubble(getWorld(),
                        pos.getX() + rand.nextDouble(),
                        pos.getY() + topY,
                        pos.getZ() + rand.nextDouble(),
                        0, .005, 0, fluidColor));
    }

    @SideOnly(Side.CLIENT)
    private void syncFluidVisual() {
        if (renderFluid && fluidTexture != null) {
            ActiveFluidVisualHandler.registerFluid(getPos(), fluidColor, fluidDisplaySize);
        } else {
            ActiveFluidVisualHandler.unregisterFluid(getPos());
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        Block block = getStoredBlock();
        if (block != null) {
            try {
                IBlockState state = block.getStateFromMeta(getStoredMeta());
                IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(state);
                return Pair.of(model.getParticleTexture(), getPaintingColorForRendering());
            } catch (Exception ignored) {}
        }
        return Pair.of(defaultRenderer.getTopSprite(), getPaintingColorForRendering());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        renderCasing(renderState, translation, pipeline);

        BlockRenderLayer currentLayer = MinecraftForgeClient.getRenderLayer();
        boolean shouldRenderFluid = renderFluid && fluidTexture != null
                && currentLayer == BlockRenderLayer.TRANSLUCENT;
        if (fluidDisplaySize > 0) {
            shouldRenderFluid = shouldRenderFluid && isActive && isWorkingEnabled;
        }
        if (shouldRenderFluid) {
            int fluidRGBA = GTUtility.convertRGBtoRGBA_CL(fluidColor & 0x00FFFFFF, 0xD0);
            IVertexOperation[] fluidPipeline = new IVertexOperation[]{
                    new ColourMultiplier(fluidRGBA)};
            Cuboid6 cuboid = getFluidRenderCuboid();

            CubeRendererState previousState = Textures.RENDER_STATE.get();
            Textures.RENDER_STATE.set(new CubeRendererState(
                    BlockRenderLayer.TRANSLUCENT, CubeRendererState.PASS_MASK, previousState.world));

            if (fluidDisplaySize > 0) {
                for (EnumFacing facing : new EnumFacing[]{EnumFacing.NORTH, EnumFacing.SOUTH,
                        EnumFacing.EAST, EnumFacing.WEST}) {
                    Cuboid6 clipped = getClippedCuboidForFace(facing, cuboid);
                    if (clipped != null) {
                        renderFluidFace(renderState, translation.copy(), fluidPipeline, facing, clipped, fluidTexture, false);
                        renderFluidFace(renderState, translation.copy(), fluidPipeline, facing, clipped, fluidTexture, true);
                    }
                }
            }
            Cuboid6 clippedUp = isUpFaceCovered() ? null : cuboid;
            if (clippedUp != null) {
                renderFluidFace(renderState, translation.copy(), fluidPipeline, EnumFacing.UP, clippedUp, fluidTexture, false);
                renderFluidFace(renderState, translation.copy(), fluidPipeline, EnumFacing.UP, clippedUp, fluidTexture, true);
            }

            Textures.RENDER_STATE.set(previousState);
        }
    }

    private double getEffectiveFluidHeight() {
        return fluidDisplaySize > 0 ? fluidDisplaySize : 3.0 / 16.0;
    }

    private boolean isNeighborFluidActive(MetaTileEntityFluidActiveCasing neighbor) {
        if (!neighbor.renderFluid) return false;
        if (neighbor.fluidDisplaySize > 0 && !(neighbor.isActive && neighbor.isWorkingEnabled)) return false;
        if (!hasSameFluid(neighbor)) return false;
        return true;
    }

    private boolean hasSameFluid(MetaTileEntityFluidActiveCasing other) {
        FluidStack myFluid = fluidTank.getFluid();
        FluidStack otherFluid = other.fluidTank.getFluid();
        if (myFluid == null && otherFluid == null) return true;
        if (myFluid == null || otherFluid == null) return false;
        return myFluid.getFluid() == otherFluid.getFluid();
    }

    @SideOnly(Side.CLIENT)
    private boolean isUpFaceCovered() {
        if (getWorld() == null) return false;
        double myTop = 1.0 + getEffectiveFluidHeight();
        int nx = getPos().getX();
        int nz = getPos().getZ();
        int ny = getPos().getY();

        for (int dy = -32; dy <= 32; dy++) {
            if (dy == 0) continue;
            BlockPos npos = new BlockPos(nx, ny + dy, nz);
            net.minecraft.tileentity.TileEntity te = getWorld().getTileEntity(npos);
            if (!(te instanceof IGregTechTileEntity)) continue;
            MetaTileEntity mte = ((IGregTechTileEntity) te).getMetaTileEntity();
            if (!(mte instanceof MetaTileEntityFluidActiveCasing)) continue;
            MetaTileEntityFluidActiveCasing neighbor = (MetaTileEntityFluidActiveCasing) mte;
            if (!isNeighborFluidActive(neighbor)) continue;

            double nb = 1.0 + dy;
            double nt = nb + neighbor.getEffectiveFluidHeight();
            if (nb < myTop && nt > myTop) return true;
        }
        return false;
    }

    @SideOnly(Side.CLIENT)
    private Cuboid6 getClippedCuboidForFace(EnumFacing facing, Cuboid6 baseCuboid) {
        if (getWorld() == null) return baseCuboid;

        double myBottom = baseCuboid.min.y;
        double myTop = baseCuboid.max.y;

        double coverBottom = myTop;
        double coverTop = myBottom;

        int nx = getPos().getX() + facing.getDirectionVec().getX();
        int nz = getPos().getZ() + facing.getDirectionVec().getZ();
        int ny = getPos().getY();

        for (int dy = -32; dy <= 32; dy++) {
            BlockPos npos = new BlockPos(nx, ny + dy, nz);
            net.minecraft.tileentity.TileEntity te = getWorld().getTileEntity(npos);
            if (!(te instanceof IGregTechTileEntity)) continue;
            MetaTileEntity mte = ((IGregTechTileEntity) te).getMetaTileEntity();
            if (!(mte instanceof MetaTileEntityFluidActiveCasing)) continue;
            MetaTileEntityFluidActiveCasing neighbor = (MetaTileEntityFluidActiveCasing) mte;
            if (!neighbor.renderFluid) continue;
            if (!hasSameFluid(neighbor)) continue;
            if (neighbor.fluidDisplaySize > 0 && !(neighbor.isActive && neighbor.isWorkingEnabled)) continue;

            double nb = 1.0 + dy;
            double nt = nb + neighbor.getEffectiveFluidHeight();

            double iBottom = Math.max(myBottom, nb);
            double iTop = Math.min(myTop, nt);
            if (iBottom < iTop) {
                coverBottom = Math.min(coverBottom, iBottom);
                coverTop = Math.max(coverTop, iTop);
            }
        }

        if (coverBottom >= coverTop) return baseCuboid;
        if (coverBottom <= myBottom && coverTop >= myTop) return null;

        if (coverBottom <= myBottom) {
            return new Cuboid6(baseCuboid.min.x, coverTop, baseCuboid.min.z,
                    baseCuboid.max.x, myTop, baseCuboid.max.z);
        }
        if (coverTop >= myTop) {
            return new Cuboid6(baseCuboid.min.x, myBottom, baseCuboid.min.z,
                    baseCuboid.max.x, coverBottom, baseCuboid.max.z);
        }
        return new Cuboid6(baseCuboid.min.x, myBottom, baseCuboid.min.z,
                baseCuboid.max.x, coverBottom, baseCuboid.max.z);
    }

    @SideOnly(Side.CLIENT)
    private void renderFluidFace(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline,
                                  EnumFacing face, Cuboid6 cuboid, TextureAtlasSprite sprite, boolean reversed) {
        int savedBrightness = renderState.brightness;
        renderState.brightness = 0xF000F0;
        if (face.getAxis() != EnumFacing.Axis.Y) {
            double height = cuboid.max.y - cuboid.min.y;
            int tiles = (int) Math.ceil(height);
            for (int i = 0; i < tiles; i++) {
                double tileMinY = cuboid.min.y + i;
                double tileMaxY = Math.min(cuboid.min.y + i + 1, cuboid.max.y);
                Cuboid6 tileCuboid = new Cuboid6(
                        cuboid.min.x, tileMinY, cuboid.min.z,
                        cuboid.max.x, tileMaxY, cuboid.max.z);
                BlockFace blockFace = new BlockFace();
                blockFace.loadCuboidFace(tileCuboid, face.getIndex());
                if (reversed) {
                    java.util.Collections.reverse(java.util.Arrays.asList(blockFace.verts));
                    blockFace.side = face.getOpposite().getIndex();
                    blockFace.lcComputed = false;
                }
                double vMin = 1.0 - tileCuboid.max.y;
                double vMax = 1.0 - tileCuboid.min.y;
                double vRange = vMax - vMin;
                for (Vertex5 vert : blockFace.verts) {
                    vert.uv.v = (vert.uv.v - vMin) / vRange;
                }
                UVTransformationList uvList = new UVTransformationList(new IconTransformation(sprite));
                renderState.setPipeline(blockFace, 0, blockFace.verts.length,
                        ArrayUtils.addAll(pipeline, new TransformationList(translation), uvList));
                renderState.render();
            }
        } else {
            BlockFace blockFace = new BlockFace();
            blockFace.loadCuboidFace(cuboid, face.getIndex());
            if (reversed) {
                java.util.Collections.reverse(java.util.Arrays.asList(blockFace.verts));
                blockFace.side = face.getOpposite().getIndex();
                blockFace.lcComputed = false;
            }
            UVTransformationList uvList = new UVTransformationList(new IconTransformation(sprite));
            renderState.setPipeline(blockFace, 0, blockFace.verts.length,
                    ArrayUtils.addAll(pipeline, new TransformationList(translation), uvList));
            renderState.render();
        }
        renderState.brightness = savedBrightness;
    }

    @SideOnly(Side.CLIENT)
    private void renderCasing(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        Block block = getStoredBlock();
        if (block == null) {
            defaultRenderer.render(renderState, translation, pipeline);
            return;
        }
        try {
            IBlockState state = BlockSkinRenderer.resolveBlockState(block, getStoredMeta());
            BlockSkinRenderer.renderBlockSkin(renderState, translation, pipeline, state, getWorld(), getPos());
        } catch (Exception e) {
            defaultRenderer.render(renderState, translation, pipeline);
        }
    }

    @Override
    protected ModularUI createUI(EntityPlayer player) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176, 166)
                .label(6, 6, getMetaFullName())
                .widget(new PhantomFluidWidget(52, 25, 18, 18, fluidTank)
                        .setBackgroundTexture(GuiTextures.FLUID_SLOT))
                .widget(new PhantomSlotWidget(blockSlot, 0, 80, 25)
                        .setClearSlotOnRightClick(true)
                        .setBackgroundTexture(GuiTextures.SLOT))
                .widget(new ClickButtonWidget(80, 47, 60, 18, "",
                        clickData -> cycleEffectMode())
                        .setTooltipText(getModeTooltip()))
                .widget(new SimpleTextWidget(110, 56, "", this::getModeDisplayName))
                .widget(new ImageCycleButtonWidget(152, 25, 18, 18, GuiTextures.BUTTON_POWER,
                        this::isWorkingEnabled, this::setWorkingEnabled))
                .widget(new ClickButtonWidget(6, 64, 18, 18, "-",
                        clickData -> changeDisplaySize(-1))
                        .setTooltipText("susy.machine.active_fluid_effect_maker.display_size.decrease"))
                .widget(new ClickButtonWidget(26, 64, 18, 18, "+",
                        clickData -> changeDisplaySize(1))
                        .setTooltipText("susy.machine.active_fluid_effect_maker.display_size.increase"))
                .widget(new SimpleTextWidget(48, 56, "", this::getDisplaySizeText));
        builder.bindPlayerInventory(player.inventory, GuiTextures.SLOT, 7, 84);
        return builder.build(getHolder(), player);
    }

    private void changeDisplaySize(int delta) {
        int newSize = Math.max(fluidDisplaySize + delta, MIN_DISPLAY_SIZE);
        if (newSize != fluidDisplaySize) {
            fluidDisplaySize = newSize;
            writeCustomData(UPDATE_FLUID_DISPLAY_SIZE, buf -> buf.writeInt(fluidDisplaySize));
            scheduleRenderUpdate();
        }
    }

    private String getDisplaySizeText() {
        if (fluidDisplaySize == 0) return I18n.format("susy.machine.active_fluid_effect_maker.display_size.thin");
        return fluidDisplaySize + " " + I18n.format("susy.machine.active_fluid_effect_maker.display_size.blocks");
    }

    private void cycleEffectMode() {
        EffectMode[] modes = EffectMode.values();
        effectMode = modes[(effectMode.ordinal() + 1) % modes.length];
        writeCustomData(UPDATE_EFFECT_MODE, buf -> buf.writeInt(effectMode.ordinal()));
    }

    private String getModeDisplayName() {
        switch (effectMode) {
            case CLARIFIER: return "Clarifier";
            case FROTH_FLOTATION: return "Froth Flot.";
            default: return effectMode.getName();
        }
    }

    private String getModeTooltip() {
        return "susy.machine.active_fluid_effect_maker.mode." + effectMode.getName();
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isActive);
        buf.writeBoolean(isWorkingEnabled);
        buf.writeInt(effectMode.ordinal());
        buf.writeInt(fluidDisplaySize);

        FluidStack fluid = fluidTank.getFluid();
        buf.writeBoolean(fluid != null && fluid.amount > 0);
        if (fluid != null && fluid.amount > 0) {
            buf.writeInt(fluid.getFluid().getColor(fluid));
            buf.writeResourceLocation(fluid.getFluid().getStill(fluid));
        }

        Block block = getStoredBlock();
        buf.writeBoolean(block != null);
        if (block != null) {
            buf.writeResourceLocation(Block.REGISTRY.getNameForObject(block));
            buf.writeVarInt(getStoredMeta());
        }
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.isActive = buf.readBoolean();
        this.isWorkingEnabled = buf.readBoolean();
        this.effectMode = EffectMode.values()[buf.readInt()];
        this.fluidDisplaySize = buf.readInt();

        this.renderFluid = buf.readBoolean();
        if (this.renderFluid) {
            this.fluidColor = buf.readInt();
            this.fluidTexture = Minecraft.getMinecraft().getTextureMapBlocks()
                    .getAtlasSprite(buf.readResourceLocation().toString());
        }

        boolean hasBlock = buf.readBoolean();
        if (hasBlock) {
            this.lastSyncedBlock = Block.getBlockFromName(buf.readResourceLocation().toString());
            this.lastSyncedMeta = buf.readVarInt();
        } else {
            this.lastSyncedBlock = null;
            this.lastSyncedMeta = 0;
        }
        syncFluidVisual();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == UPDATE_FLUID_INFO) {
            if (getWorld() != null && getWorld().isRemote) {
                this.fluidColor = buf.readInt();
                this.fluidTexture = Minecraft.getMinecraft().getTextureMapBlocks()
                        .getAtlasSprite(buf.readResourceLocation().toString());
                syncFluidVisual();
            }
        } else if (dataId == CHANGE_FLUID_RENDER_STATUS) {
            this.renderFluid = buf.readBoolean();
            scheduleRenderUpdate();
            if (getWorld() != null && getWorld().isRemote) syncFluidVisual();
        } else if (dataId == GregtechDataCodes.IS_WORKING) {
            this.isActive = buf.readBoolean();
            scheduleRenderUpdate();
            if (getWorld() != null && getWorld().isRemote) syncFluidVisual();
        } else if (dataId == GregtechDataCodes.WORKING_ENABLED) {
            this.isWorkingEnabled = buf.readBoolean();
            scheduleRenderUpdate();
            if (getWorld() != null && getWorld().isRemote) syncFluidVisual();
        } else if (dataId == UPDATE_EFFECT_MODE) {
            this.effectMode = EffectMode.values()[buf.readInt()];
            scheduleRenderUpdate();
        } else if (dataId == UPDATE_STORED_BLOCK) {
            boolean hasBlock = buf.readBoolean();
            if (hasBlock) {
                this.lastSyncedBlock = Block.getBlockFromName(buf.readResourceLocation().toString());
                this.lastSyncedMeta = buf.readVarInt();
            } else {
                this.lastSyncedBlock = null;
                this.lastSyncedMeta = 0;
            }
            scheduleRenderUpdate();
        } else if (dataId == UPDATE_FLUID_DISPLAY_SIZE) {
            this.fluidDisplaySize = buf.readInt();
            scheduleRenderUpdate();
            if (getWorld() != null && getWorld().isRemote) syncFluidVisual();
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("WorkingEnabled", isWorkingEnabled);
        data.setInteger("EffectMode", effectMode.ordinal());
        data.setInteger("FluidDisplaySize", fluidDisplaySize);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.isWorkingEnabled = data.getBoolean("WorkingEnabled");
        int mode = data.getInteger("EffectMode");
        if (mode >= 0 && mode < EffectMode.values().length) {
            this.effectMode = EffectMode.values()[mode];
        }
        this.fluidDisplaySize = data.getInteger("FluidDisplaySize");
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (getWorld() != null && getWorld().isRemote) {
            ActiveFluidVisualHandler.unregisterFluid(getPos());
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, java.util.List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.universal.tooltip.fluid_storage_capacity", tankSize));
        tooltip.add(I18n.format(getMetaName() + ".tooltip"));
    }
}
