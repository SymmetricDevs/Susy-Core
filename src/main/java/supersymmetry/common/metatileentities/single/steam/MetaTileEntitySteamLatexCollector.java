package supersymmetry.common.metatileentities.single.steam;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.impl.FilteredFluidHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.ModHandler;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer.RenderSide;
import java.util.List;

import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import supersymmetry.common.materials.SusyMaterials;

public class MetaTileEntitySteamLatexCollector extends MetaTileEntity {
    private boolean isWorkingEnabled = true;
    private boolean needsVenting = false;
    private boolean ventingStuck = false;
    private final int energyPerTick = 16;
    private final int tankSize = 16000;
    private final long latexCollectionAmount = 3L;
    private int numberRubberLogs;

    public MetaTileEntitySteamLatexCollector(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        this.initializeInventory();
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntitySteamLatexCollector(this.metaTileEntityId);
    }

    public FluidTankList createImportFluidHandler() {
        return new FluidTankList(false, new IFluidTank[]{(new FilteredFluidHandler(16000)).setFillPredicate(ModHandler::isSteam)});
    }

    protected FluidTankList createExportFluidHandler() {
        return new FluidTankList(false, new IFluidTank[]{new FluidTank(this.tankSize)});
    }

    protected IItemHandlerModifiable createImportItemHandler() {
        return new NotifiableItemStackHandler(1, this, false);
    }

    protected IItemHandlerModifiable createExportItemHandler() {
        return new NotifiableItemStackHandler(1, this, true);
    }

    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        ColourMultiplier multiplier = new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(this.getPaintingColorForRendering()));
        IVertexOperation[] coloredPipeline = (IVertexOperation[])ArrayUtils.add(pipeline, multiplier);
        Textures.STEAM_CASING_BRONZE.render(renderState, translation, coloredPipeline);
        EnumFacing[] var6 = EnumFacing.HORIZONTALS;
        int var7 = var6.length;

        for(int var8 = 0; var8 < var7; ++var8) {
            EnumFacing renderSide = var6[var8];
            if (renderSide == this.getFrontFacing()) {
                Textures.PIPE_OUT_OVERLAY.renderSided(renderSide, renderState, translation, pipeline);
            } else {
                Textures.STEAM_MINER_OVERLAY.renderSided(renderSide, renderState, translation, coloredPipeline);
            }
        }

        Textures.STEAM_VENT_OVERLAY.renderSided(EnumFacing.UP, renderState, translation, pipeline);
        Textures.PIPE_IN_OVERLAY.renderSided(EnumFacing.DOWN, renderState, translation, pipeline);
    }

    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND_STEAM.get(false), 175, 176);
        builder.image(7, 16, 81, 55, GuiTextures.DISPLAY);
        TankWidget tankWidget = (new TankWidget(this.exportFluids.getTankAt(0), 69, 52, 18, 18)).setHideTooltip(true).setAlwaysShowFull(true);
        builder.widget(tankWidget);
        builder.label(11, 20, "gregtech.gui.fluid_amount", 16777215);
        builder.dynamicLabel(11, 30, tankWidget::getFormattedFluidAmount, 16777215);
        builder.dynamicLabel(11, 40, tankWidget::getFluidLocalizedName, 16777215);
        builder.widget((new AdvancedTextWidget(10, 19, this::addDisplayText, 16777215)).setMaxWidthLimit(84));
        return builder.label(6, 6, this.getMetaFullName()).widget((new FluidContainerSlotWidget(this.importItems, 0, 90, 17, false)).setBackgroundTexture(new IGuiTexture[]{GuiTextures.SLOT_STEAM.get(false), GuiTextures.IN_SLOT_OVERLAY})).widget(new ImageWidget(91, 36, 14, 15, GuiTextures.TANK_ICON)).widget((new SlotWidget(this.exportItems, 0, 90, 54, true, false)).setBackgroundTexture(new IGuiTexture[]{GuiTextures.SLOT_STEAM.get(false), GuiTextures.OUT_SLOT_OVERLAY})).bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT_STEAM.get(false), 10).build(this.getHolder(), entityPlayer);

    }

    void addDisplayText(List<ITextComponent> textList) {
        if (!this.drainEnergy(true)) {
            textList.add((new TextComponentTranslation("gregtech.multiblock.large_miner.steam", new Object[0])).setStyle((new Style()).setColor(TextFormatting.RED)));
        }

    }

    public boolean drainEnergy(boolean simulate) {
        int resultSteam = this.importFluids.getTankAt(0).getFluidAmount() - this.energyPerTick;
        if (!this.ventingStuck && (long)resultSteam >= 0L && resultSteam <= this.importFluids.getTankAt(0).getCapacity()) {
            if (!simulate) {
                this.importFluids.getTankAt(0).drain(this.energyPerTick, true);
            }

            return true;
        } else {
            return false;
        }
    }

    public void update() {
        super.update();
        if (!this.getWorld().isRemote && this.numberRubberLogs != 0 && this.isWorkingEnabled) {
            if(this.drainEnergy(true)){
                FluidStack latexStack = SusyMaterials.Latex.getFluid((int) this.latexCollectionAmount * this.numberRubberLogs);
                NonNullList<FluidStack> fluidStacks = NonNullList.create();
                fluidStacks.add(latexStack);
                if (GTTransferUtils.addFluidsToFluidHandler(this.exportFluids, true, fluidStacks)) {
                    GTTransferUtils.addFluidsToFluidHandler(this.exportFluids, false, fluidStacks);
                    this.drainEnergy(false);
                }
            }
        }


        if (!this.getWorld().isRemote && this.getOffsetTimer() % 5L == 0L) {
            this.pushItemsIntoNearbyHandlers(new EnumFacing[]{this.getFrontFacing()});
            this.fillContainerFromInternalTank();
        }

    }

    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("numberRubberLogs", this.numberRubberLogs);
        return data;
    }

    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.hasKey("numberRubberLogs")) {
            this.numberRubberLogs = data.getInteger("numberRubberLogs");
        }
    }

    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(this.needsVenting);
        buf.writeBoolean(this.ventingStuck);
    }
    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(Textures.STEAM_CASING_BRONZE.getSpriteOnSide(RenderSide.TOP), this.getPaintingColorForRendering());
    }


    public void onNeighborChanged() {
        super.onNeighborChanged();
        this.checkAdjacentBlocks();
    }

    public void checkAdjacentBlocks(){
        if(this.getWorld() != null){
            this.numberRubberLogs = 0;
            if(!this.getWorld().isRemote) {
                EnumFacing[] facings = EnumFacing.VALUES;
                int numFacings = facings.length;

                for (int i = 0; i < numFacings; ++i) {
                    EnumFacing side = facings[i];

                    if (side != this.frontFacing && !side.getAxis().isVertical()) {
                        Block block = this.getWorld().getBlockState(this.getPos().offset(side)).getBlock();
                        if (block == MetaBlocks.RUBBER_LOG) {
                            ++this.numberRubberLogs;
                        }
                    }
                }
            }
        }
    }

    public <T> void addNotifiedInput(T input) {
        super.addNotifiedInput(input);
        this.onNeighborChanged();
    }
}
