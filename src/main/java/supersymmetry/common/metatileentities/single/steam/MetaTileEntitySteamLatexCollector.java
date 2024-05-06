package supersymmetry.common.metatileentities.single.steam;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.impl.CommonFluidFilters;
import gregtech.api.capability.impl.FilteredFluidHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer.RenderSide;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.tuple.Pair;
import supersymmetry.api.metatileentity.PseudoMultiSteamMachineMetaTileEntity;
import supersymmetry.api.metatileentity.steam.SuSySteamProgressIndicators;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.textures.SusyTextures;

import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntitySteamLatexCollector extends PseudoMultiSteamMachineMetaTileEntity {

    private final int energyPerTick = 16;
    private final int tankSize = 16000;
    private long latexCollectionAmount;

    public MetaTileEntitySteamLatexCollector(ResourceLocation metaTileEntityId, boolean isHighPressure) {
        super(metaTileEntityId, SuSyRecipeMaps.LATEX_COLLECTOR_RECIPES, SuSySteamProgressIndicators.EXTRACTION_STEAM, SusyTextures.LATEX_COLLECTOR_OVERLAY, false, isHighPressure);
        this.initializeInventory();
        latexCollectionAmount = isHighPressure ? 6L : 3L;
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity, boolean isHighPressure) {
        return new MetaTileEntitySteamLatexCollector(this.metaTileEntityId, isHighPressure);
    }

    @Override
    public FluidTankList createImportFluidHandler() {
        return new FluidTankList(false, new FilteredFluidHandler(16000).setFilter(CommonFluidFilters.STEAM));
    }

    @Override
    protected FluidTankList createExportFluidHandler() {
        return new FluidTankList(false, new IFluidTank[]{new FluidTank(this.tankSize)});
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new NotifiableItemStackHandler(this, 1, this, false);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new NotifiableItemStackHandler(this, 1, this, true);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        SusyTextures.LATEX_COLLECTOR_OVERLAY.renderOrientedState(renderState, translation, pipeline, this.getFrontFacing(), this.isActive(), true);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(Textures.STEAM_CASING_BRONZE.getSpriteOnSide(RenderSide.TOP), this.getPaintingColorForRendering());
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND_STEAM.get(false), 175, 176);
        builder.image(7, 16, 81, 55, GuiTextures.DISPLAY);
        TankWidget tankWidget = (new TankWidget(this.exportFluids.getTankAt(0), 69, 52, 18, 18)).setHideTooltip(true).setAlwaysShowFull(true);
        builder.widget(tankWidget);
        builder.label(11, 20, "gregtech.gui.fluid_amount", 16777215);
        builder.dynamicLabel(11, 30, tankWidget::getFormattedFluidAmount, 16777215);
        builder.dynamicLabel(11, 40, tankWidget::getFluidLocalizedName, 16777215);
        builder.widget((new AdvancedTextWidget(11, 50, this::addDisplayText, 16777215)).setMaxWidthLimit(84));
        return builder.label(6, 6, this.getMetaFullName()).widget((new FluidContainerSlotWidget(this.importItems, 0, 90, 17, false)).setBackgroundTexture(new IGuiTexture[]{GuiTextures.SLOT_STEAM.get(false), GuiTextures.IN_SLOT_OVERLAY})).widget(new ImageWidget(91, 36, 14, 15, GuiTextures.TANK_ICON)).widget((new SlotWidget(this.exportItems, 0, 90, 54, true, false)).setBackgroundTexture(new IGuiTexture[]{GuiTextures.SLOT_STEAM.get(false), GuiTextures.OUT_SLOT_OVERLAY})).bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT_STEAM.get(false), 10).build(this.getHolder(), entityPlayer);

    }

    void addDisplayText(List<ITextComponent> textList) {
        if (!this.drainEnergy(true)) {
            textList.add((new TextComponentTranslation("gregtech.machine.latex_collector.steam", new Object[0])).setStyle((new Style()).setColor(TextFormatting.RED)));
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.latex_collector.tooltip", this.latexCollectionAmount));
    }

    public boolean drainEnergy(boolean simulate) {
        int resultSteam = this.importFluids.getTankAt(0).getFluidAmount() - this.energyPerTick;
        if ((long)resultSteam >= 0L && resultSteam <= this.importFluids.getTankAt(0).getCapacity()) {
            if (!simulate) {
                this.importFluids.getTankAt(0).drain(this.energyPerTick, true);
            }
            return true;
        } else {
            return false;
        }
    }
}
