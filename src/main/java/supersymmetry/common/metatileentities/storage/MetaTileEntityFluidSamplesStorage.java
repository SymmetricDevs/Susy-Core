package supersymmetry.common.metatileentities.storage;

import java.util.List;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import org.apache.commons.lang3.tuple.Pair;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerProxy;
import gregtech.api.capability.impl.NotifiableFluidTank;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;

/**
 * Passive storage rack: 32 independent bidirectional fluid tanks (8000L each), no items, no
 * recipe processing. Meant as a low-tech alternative to juggling drums/cells for bulk fluid
 * storage.
 */
public class MetaTileEntityFluidSamplesStorage extends MetaTileEntity {

    private static final int TANK_COUNT = 32;
    private static final int TANK_CAPACITY = 8000;
    private static final int GRID_COLS = 8;
    private static final int GRID_ROWS = 4;

    private static final OrientedOverlayRenderer OVERLAY = new OrientedOverlayRenderer(
            "machines/fluid_samples_storage");

    private NotifiableFluidTank[] fluidTanks;
    private FluidTankList fluidTankList;

    public MetaTileEntityFluidSamplesStorage(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        this.fluidTanks = new NotifiableFluidTank[TANK_COUNT];
        for (int i = 0; i < TANK_COUNT; i++) {
            fluidTanks[i] = new NotifiableFluidTank(TANK_CAPACITY, this, false);
        }
        this.fluidTankList = new FluidTankList(false, fluidTanks);
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityFluidSamplesStorage(metaTileEntityId);
    }

    @Override
    protected void initializeInventory() {
        // super(ResourceLocation) calls this before our constructor body runs; fluidTankList
        // is still null at that point. Swallow that premature call and let the explicit
        // second call (after tanks are built) do the real work.
        if (this.fluidTankList == null) return;
        this.importItems = createImportItemHandler();
        this.exportItems = createExportItemHandler();
        this.itemInventory = new ItemHandlerProxy(importItems, exportItems);
        this.importFluids = createImportFluidHandler();
        this.exportFluids = createExportFluidHandler();
        // Not calling super.initializeInventory() here: it would wrap importFluids/exportFluids
        // in a FluidHandlerProxy, which concatenates both sides' getTankProperties() - since both
        // are the SAME fluidTankList here, that would double every tank in any capability-based
        // listing (pipe filter UIs, probe mod overlays, etc.). Expose the list itself instead.
        this.fluidInventory = this.fluidTankList;
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        return this.fluidTankList;
    }

    @Override
    protected FluidTankList createExportFluidHandler() {
        return this.fluidTankList;
    }

    @Override
    public boolean hasFrontFacing() {
        // purely cosmetic - lets the overlay icon face a chosen direction and be wrenchable
        // like a normal machine; all 32 tanks are still reachable from every side regardless.
        return true;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        // Some probe-info mods (e.g. TOP Addons' generic fluid-tank display) query
        // getCapability(FLUID_HANDLER_CAPABILITY, null) directly and dump every tank's contents
        // into the hover tooltip, which is unreadable at 32 tanks and isn't gated by any
        // documented per-block opt-out API. Pipes/covers always query with a specific EnumFacing
        // (never null), and the GUI reads fluidTankList directly rather than through this
        // capability, so refusing only the null-side query is safe: automation keeps working,
        // only generic capability-dump probes are blocked.
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && side == null) {
            return null;
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.VOLTAGE_CASINGS[GTValues.LV].render(renderState, translation, pipeline);
        OVERLAY.renderOrientedState(renderState, translation, pipeline, getFrontFacing(), false, false);
    }

    @Override
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(OVERLAY.getParticleSprite(), getPaintingColorForRendering());
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(TANK_COUNT + " independent tanks, " + TANK_CAPACITY + "L each.");
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        int windowHeight = 18 + 18 * GRID_ROWS + 94;
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176, windowHeight)
                .label(10, 5, getMetaFullName());

        int gridLeft = 89 - GRID_COLS * 9;
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                int index = row * GRID_COLS + col;
                builder.widget(new TankWidget(fluidTankList.getTankAt(index),
                        gridLeft + col * 18, 18 + row * 18, 18, 18)
                        .setBackgroundTexture(GuiTextures.FLUID_SLOT)
                        .setContainerClicking(true, true)
                        .setAlwaysShowFull(true));
            }
        }

        builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 7, 18 + 18 * GRID_ROWS + 12);
        return builder.build(getHolder(), entityPlayer);
    }
}
