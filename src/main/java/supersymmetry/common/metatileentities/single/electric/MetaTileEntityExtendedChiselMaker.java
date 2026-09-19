package supersymmetry.common.metatileentities.single.electric;

import static gregtech.api.capability.GregtechDataCodes.assignId;

import java.util.List;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IActiveOutputSide;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ImageCycleButtonWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.RenderUtil;
import supersymmetry.api.chisel.ExtendedChiselItem;
import supersymmetry.api.chisel.ExtendedChiselRegistry;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.mui.widget.SearchTextField;

/**
 * The Extended Chisel Maker: a single-block GregTech machine that converts <em>any</em> block of a
 * chosen {@link supersymmetry.api.chisel.ExtendedChiselGroup} into any other block of the very same
 * group (1 input block → 1 output block).
 *
 * <p>
 * There is no base template and no recipe map registration: the group itself is the recipe. A block is
 * accepted as input only if it belongs to at least one extended group, and an output is produced only
 * if the currently selected item shares a common group with the input. Items that appear in several
 * groups therefore allow cross-conversion between those groups' members.
 * </p>
 *
 * <p>
 * The selected output is picked through the GUI's folder browser (items are organized by their
 * Extended Location Identifiers, deduplicated) with an optional search bar at the top. Operation is
 * fully automatable: pipes/droppers may insert blocks from any side and extract the converted block
 * from any side, driven by the machine's import/export item handlers.
 * </p>
 */
public class MetaTileEntityExtendedChiselMaker extends MetaTileEntity
                                               implements IControllable, IActiveOutputSide {

    public static final int INPUT_SLOT = 0;
    public static final int RESULT_SLOT = 0;

    public static final int UPDATE_SELECTION = assignId();
    public static final int UPDATE_ENABLED = assignId();
    public static final int UPDATE_OUTPUT_FACING = assignId();
    public static final int UPDATE_AUTO_OUTPUT = assignId();

    private static final String NBT_KEY = "SelectedChiselItemId";
    private static final String NBT_ENABLED = "CraftingEnabled";
    private static final String NBT_OUTPUT_FACING = "OutputFacing";
    private static final String NBT_AUTO_OUTPUT = "AutoOutput";

    @Nullable private String selectedItemId;
    private boolean craftingEnabled = true;
    private boolean autoOutputItems = false;
    @Nullable private EnumFacing outputFacingItems;

    public MetaTileEntityExtendedChiselMaker(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity holder) {
        return new MetaTileEntityExtendedChiselMaker(metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        IVertexOperation[] colouredPipeline = ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering())));
        Textures.VOLTAGE_CASINGS[GTValues.LV].render(renderState, translation, colouredPipeline);
        SusyTextures.EXTENDED_CHISEL_OVERLAY.renderOrientedState(renderState, translation, pipeline, getFrontFacing(),
                false, isCraftingEnabled());
        EnumFacing output = getOutputFacingItems();
        if (output != null && getExportItems().getSlots() > 0) {
            Textures.PIPE_OUT_OVERLAY.renderSided(output, renderState,
                    RenderUtil.adjustTrans(translation, output, 2), pipeline);
        }
        if (isAutoOutputItems() && output != null) {
            Textures.ITEM_OUTPUT_OVERLAY.renderSided(output, renderState,
                    RenderUtil.adjustTrans(translation, output, 2), pipeline);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(Textures.VOLTAGE_CASINGS[GTValues.LV].getParticleSprite(), getPaintingColorForRendering());
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            processCrafting();
            if (isAutoOutputItems() && getOffsetTimer() % 5 == 0) {
                pushItemsIntoNearbyHandlers(getOutputFacingItems());
            }
        }
    }

    @Override
    public void setFrontFacing(EnumFacing frontFacing) {
        super.setFrontFacing(frontFacing);
        if (this.outputFacingItems == null) {
            setOutputFacingItems(frontFacing.getOpposite());
        }
    }

    @Override
    public boolean isValidFrontFacing(EnumFacing facing) {
        return super.isValidFrontFacing(facing) && facing != getOutputFacingItems();
    }

    @Override
    public boolean onWrenchClick(EntityPlayer playerIn, EnumHand hand, EnumFacing wrenchSide,
                                 CuboidRayTraceResult hitResult) {
        if (!playerIn.isSneaking()) {
            if (getOutputFacingItems() == wrenchSide) {
                return false;
            }
            if (hasFrontFacing() && wrenchSide == getFrontFacing()) {
                return false;
            }
            if (wrenchSide != null && !getWorld().isRemote) {
                setOutputFacingItems(wrenchSide);
            }
            return true;
        }
        return super.onWrenchClick(playerIn, hand, wrenchSide, hitResult);
    }

    private void processCrafting() {
        if (!craftingEnabled || selectedItemId == null) {
            return;
        }
        ItemStack input = this.importItems.getStackInSlot(INPUT_SLOT);
        if (input.isEmpty() || !isExtendedItem(input)) {
            return;
        }
        ExtendedChiselItem selected = ExtendedChiselRegistry.resolve(selectedItemId);
        if (selected == null) {
            return;
        }
        // The only recipe rule: input and selected output must share a common extended group.
        ItemStack result = selected.getStack();
        if (result.isEmpty() || !ExtendedChiselRegistry.areCompatible(input, result)) {
            return;
        }
        ItemStack inserted = this.exportItems.insertItem(RESULT_SLOT, result, false);
        if (!inserted.isEmpty()) {
            // Could not store the whole result; do not consume the input.
            return;
        }
        this.importItems.extractItem(INPUT_SLOT, 1, false);
        this.markDirty();
    }

    /** Returns true if the given stack is a registered extended chisel item (part of any group). */
    private static boolean isExtendedItem(ItemStack stack) {
        return !ExtendedChiselRegistry.getGroupsFor(stack).isEmpty();
    }

    @Nullable public String getSelectedItemId() {
        return selectedItemId;
    }

    /** Server side setter used by the GUI widget when an item is picked. */
    public void setSelectedItemId(@Nullable String itemId) {
        if (itemId != null && itemId.isEmpty()) {
            itemId = null;
        }
        if (java.util.Objects.equals(this.selectedItemId, itemId)) {
            return;
        }
        final String stored = itemId;
        this.selectedItemId = itemId;
        this.markDirty();
        if (!getWorld().isRemote) {
            this.writeCustomData(UPDATE_SELECTION, buf -> buf.writeString(stored == null ? "" : stored));
        }
    }

    public void onSelectionSync(PacketBuffer buf) {
        this.selectedItemId = buf.readString(Short.MAX_VALUE);
    }

    public boolean isCraftingEnabled() {
        return craftingEnabled;
    }

    /** Server side setter used by the work-enable toggle button in the GUI. */
    public void setCraftingEnabled(boolean enabled) {
        if (this.craftingEnabled == enabled) {
            return;
        }
        this.craftingEnabled = enabled;
        this.markDirty();
        if (!getWorld().isRemote) {
            this.writeCustomData(UPDATE_ENABLED, buf -> buf.writeBoolean(enabled));
        }
    }

    public void onEnabledSync(PacketBuffer buf) {
        this.craftingEnabled = buf.readBoolean();
    }

    @Override
    public boolean isWorkingEnabled() {
        return isCraftingEnabled();
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        setCraftingEnabled(isWorkingAllowed);
    }

    @Override
    public boolean isAutoOutputFluids() {
        return false;
    }

    @Override
    public boolean isAllowInputFromOutputSideItems() {
        return false;
    }

    @Override
    public boolean isAllowInputFromOutputSideFluids() {
        return false;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        if (capability == GregtechTileCapabilities.CAPABILITY_ACTIVE_OUTPUT_SIDE &&
                side == getOutputFacingItems()) {
            return GregtechTileCapabilities.CAPABILITY_ACTIVE_OUTPUT_SIDE.cast(this);
        }
        return super.getCapability(capability, side);
    }

    public boolean isAutoOutputItems() {
        return autoOutputItems;
    }

    public void setAutoOutputItems(boolean autoOutputItems) {
        if (this.autoOutputItems == autoOutputItems) {
            return;
        }
        this.autoOutputItems = autoOutputItems;
        this.markDirty();
        if (!getWorld().isRemote) {
            this.writeCustomData(UPDATE_AUTO_OUTPUT, buf -> buf.writeBoolean(autoOutputItems));
        }
    }

    public EnumFacing getOutputFacingItems() {
        return outputFacingItems == null ? getFrontFacing().getOpposite() : outputFacingItems;
    }

    public void setOutputFacingItems(EnumFacing outputFacing) {
        this.outputFacingItems = outputFacing;
        if (!getWorld().isRemote) {
            notifyBlockUpdate();
            this.writeCustomData(UPDATE_OUTPUT_FACING, buf -> buf.writeByte(outputFacing.getIndex()));
            markDirty();
        }
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new NotifiableItemStackHandler(this, 1, this, false);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new NotifiableItemStackHandler(this, 1, this, true);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 352, 304)
                .label(7, 5, getMetaFullName());

        // Top-left sidebar: input slot, result slot, and a power toggle for enabling/disabling
        // automatic conversion.
        builder.widget(new SlotWidget(this.importItems, INPUT_SLOT, 12, 18, true, true)
                .setBackgroundTexture(GuiTextures.SLOT));
        builder.widget(new SlotWidget(this.exportItems, RESULT_SLOT, 12, 54, true, false)
                .setBackgroundTexture(GuiTextures.SLOT));
        builder.widget(new ImageCycleButtonWidget(11, 84, 18, 18, GuiTextures.BUTTON_POWER,
                this::isCraftingEnabled, this::setCraftingEnabled)
                .setTooltipHoverString(v -> v <= 0 ? "susy.machine.extended_chisel_maker.crafting.disabled" :
                        "susy.machine.extended_chisel_maker.crafting.enabled"));
        builder.widget(new ToggleButtonWidget(11, 104, 18, 18, GuiTextures.BUTTON_ITEM_OUTPUT,
                this::isAutoOutputItems, this::setAutoOutputItems)
                .setTooltipText("gregtech.gui.item_auto_output.tooltip")
                .shouldUseBaseBackground());

        // Search bar above the folder browser + grid, then the browser itself filling the full width
        // above the player inventory.
        ExtendedChiselTableWidget chiselWidget = new ExtendedChiselTableWidget(50, 38, 296, 178);
        SearchTextField searchBar = new SearchTextField(50, 16, 296, 18,
                chiselWidget::getSearchQuery, chiselWidget::setSearchQuery);
        chiselWidget.setClearSearchCallback(() -> {
            searchBar.setText("");
            chiselWidget.setSearchQuery("");
        });
        builder.widget(searchBar);
        builder.widget(chiselWidget);

        // Player inventory stacked at the bottom, clear of the middle grid and folder tree.
        return builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 7, 222)
                .build(getHolder(), entityPlayer);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setString(NBT_KEY, selectedItemId == null ? "" : selectedItemId);
        data.setBoolean(NBT_ENABLED, craftingEnabled);
        data.setBoolean(NBT_AUTO_OUTPUT, autoOutputItems);
        data.setInteger(NBT_OUTPUT_FACING, getOutputFacingItems().getIndex());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        String s = data.getString(NBT_KEY);
        this.selectedItemId = s.isEmpty() ? null : s;
        this.craftingEnabled = data.getBoolean(NBT_ENABLED);
        this.autoOutputItems = data.getBoolean(NBT_AUTO_OUTPUT);
        if (data.hasKey(NBT_OUTPUT_FACING)) {
            this.outputFacingItems = EnumFacing.VALUES[data.getInteger(NBT_OUTPUT_FACING)];
        }
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeString(selectedItemId == null ? "" : selectedItemId);
        buf.writeBoolean(craftingEnabled);
        buf.writeBoolean(autoOutputItems);
        buf.writeByte(getOutputFacingItems().getIndex());
    }

    @Override
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        String s = buf.readString(Short.MAX_VALUE);
        this.selectedItemId = s.isEmpty() ? null : s;
        this.craftingEnabled = buf.readBoolean();
        this.autoOutputItems = buf.readBoolean();
        this.outputFacingItems = EnumFacing.VALUES[buf.readByte()];
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == UPDATE_SELECTION) {
            onSelectionSync(buf);
        } else if (dataId == UPDATE_ENABLED) {
            onEnabledSync(buf);
        } else if (dataId == UPDATE_AUTO_OUTPUT) {
            this.autoOutputItems = buf.readBoolean();
            scheduleRenderUpdate();
        } else if (dataId == UPDATE_OUTPUT_FACING) {
            this.outputFacingItems = EnumFacing.VALUES[buf.readByte()];
            scheduleRenderUpdate();
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        tooltip.add(I18n.format("susy.machine.extended_chisel_maker.tooltip.1"));
        tooltip.add(I18n.format("susy.machine.extended_chisel_maker.tooltip.2"));
    }
}
