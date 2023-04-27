package supersymmetry.common.metatileentities.single.steam;

import gregtech.api.GTValues;
import gregtech.api.capability.impl.*;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SteamMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.api.util.GTTransferUtils;
import gregtech.client.renderer.ICubeRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import supersymmetry.api.SusyLog;
import supersymmetry.api.gui.SusyGuiTextures;
import supersymmetry.api.metatileentity.steam.SuSySteamProgressIndicator;
import supersymmetry.client.renderer.textures.SusyTextures;

import javax.annotation.Nullable;
import java.util.Arrays;

public class SuSySimpleSteamMetaTileEntity extends SteamMetaTileEntity {

    protected SuSySteamProgressIndicator progressIndicator;
    protected boolean isBrickedCasing;
    @Nullable
    protected GhostCircuitItemStackHandler circuitInventory;
    protected IItemHandler outputItemInventory;
    protected IFluidHandler outputFluidInventory;
    private IItemHandlerModifiable actualImportItems;

    public SuSySimpleSteamMetaTileEntity(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, SuSySteamProgressIndicator progressIndicator, ICubeRenderer renderer, boolean isBrickedCasing, boolean isHighPressure) {
        super(metaTileEntityId, recipeMap, renderer, isHighPressure);
        this.progressIndicator = progressIndicator;
        this.isBrickedCasing = isBrickedCasing;
        if (hasGhostCircuitInventory()) {
            this.circuitInventory = new GhostCircuitItemStackHandler();
            this.circuitInventory.addNotifiableMetaTileEntity(this);
        }
        initializeInventory();
        this.workableHandler = new RecipeLogicSteam(this,
                recipeMap, isHighPressure, steamFluidTank, 1.0);

    }

    protected boolean hasGhostCircuitInventory() {
        return true;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new SuSySimpleSteamMetaTileEntity(metaTileEntityId, workableHandler.getRecipeMap(), progressIndicator, renderer, isBrickedCasing, isHighPressure);
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        this.outputItemInventory = new ItemHandlerProxy(new ItemStackHandler(0), exportItems);
        this.outputFluidInventory = new FluidHandlerProxy(new FluidTankList(false), exportFluids);
        this.actualImportItems = null;
    }

    @Override
    public IItemHandlerModifiable getImportItems() {
        if (actualImportItems == null) this.actualImportItems = circuitInventory == null ? super.getImportItems() : new ItemHandlerList(Arrays.asList(super.getImportItems(), this.circuitInventory));
        return this.actualImportItems;
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        if (workableHandler == null) return new ItemStackHandler(0);
        return new NotifiableItemStackHandler(workableHandler.getRecipeMap().getMaxInputs(), this, false);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        if (workableHandler == null) return new ItemStackHandler(0);
        return new NotifiableItemStackHandler(workableHandler.getRecipeMap().getMaxOutputs(), this, true);
    }

    @Override
    public FluidTankList createImportFluidHandler() {
        super.createImportFluidHandler();
        if (workableHandler == null) return new FluidTankList(false, new IFluidTank[]{this.steamFluidTank});
        IFluidTank[] fluidImports = new IFluidTank[workableHandler.getRecipeMap().getMaxFluidInputs() + 1];
        fluidImports[0] = this.steamFluidTank;
        for (int i = 1; i < fluidImports.length; i++) fluidImports[i] = new NotifiableFluidTank(16000, this, false);
        return new FluidTankList(false, fluidImports);
    }

    @Override
    protected FluidTankList createExportFluidHandler() {
        if (workableHandler == null) return new FluidTankList(false);
        FluidTank[] fluidExports = new FluidTank[workableHandler.getRecipeMap().getMaxFluidOutputs()];
        for (int i = 0; i < fluidExports.length; i++) fluidExports[i] = new NotifiableFluidTank(16000, this, true);
        return new FluidTankList(false, fluidExports);
    }

    @Override
    protected boolean isBrickedCasing() {
        return isBrickedCasing;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return createGuiTemplate(entityPlayer).build(getHolder(), entityPlayer);
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected void randomDisplayTick(float x, float y, float z, EnumParticleTypes flame, EnumParticleTypes smoke) {
        super.randomDisplayTick(x, y, z, flame, smoke);
        if (GTValues.RNG.nextBoolean()) this.getWorld().spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x, y + 0.5F, z, 0.0, 0.0, 0.0, new int[0]);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        if (circuitInventory != null) this.circuitInventory.write(data);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (circuitInventory != null) {
            if (data.hasKey("CircuitInventory", Constants.NBT.TAG_COMPOUND)) {
                ItemStackHandler legacyCircuitInventory = new ItemStackHandler();
                for (int i =0; i < legacyCircuitInventory.getSlots(); i++) {
                    ItemStack stack = legacyCircuitInventory.getStackInSlot(i);
                    if (stack.isEmpty()) continue;
                    stack = GTTransferUtils.insertItem(this.importItems, stack, false);
                    this.circuitInventory.setCircuitValueFromStack(stack);
                }
            } else {
                this.circuitInventory.read(data);
            }
        }
    }

    public void setGhostCircuitConfig(int config) {
        if (this.circuitInventory == null || this.circuitInventory.getCircuitValue() == config) return;
        this.circuitInventory.setCircuitValue(config);
        if (!getWorld().isRemote) markDirty();
    }

    protected ModularUI.Builder createGuiTemplate(EntityPlayer player) {
        RecipeMap<?> recipeMap = workableHandler.getRecipeMap();
        int yOffset = 0;

        ModularUI.Builder builder = super.createUITemplate(player);
        addRecipeProgressBar(builder, recipeMap, yOffset);
        addInventorySlotGroup(builder, importItems, importFluids, false, yOffset);
        addInventorySlotGroup(builder, exportItems, exportFluids, true, yOffset);

        if (exportItems.getSlots() + exportFluids.getTanks() <= 9) {
            if (this.circuitInventory != null) {
                SlotWidget circuitSlot = new GhostCircuitSlotWidget(circuitInventory, 0, 124, 62 + yOffset)
                        .setBackgroundTexture(GuiTextures.SLOT_STEAM.get(isHighPressure), getCircuitSlotOverlay());
                builder.widget(getCircuitSlotToolTip(circuitSlot))
                        .widget(new ClickButtonWidget(115, 62 + yOffset, 9, 9, "", click -> circuitInventory.addCircuitValue(click.isShiftClick ? 5 : 1)).setShouldClientCallback(true).setButtonTexture(GuiTextures.BUTTON_INT_CIRCUIT_PLUS).setDisplayFunction(() -> circuitInventory.hasCircuitValue() && circuitInventory.getCircuitValue() < IntCircuitIngredient.CIRCUIT_MAX))
                        .widget(new ClickButtonWidget(115, 71 + yOffset, 9, 9, "", click -> circuitInventory.addCircuitValue(click.isShiftClick ? -5 : -1)).setShouldClientCallback(true).setButtonTexture(GuiTextures.BUTTON_INT_CIRCUIT_MINUS).setDisplayFunction(() -> circuitInventory.hasCircuitValue() && circuitInventory.getCircuitValue() > IntCircuitIngredient.CIRCUIT_MIN));
            }
        }

        return builder;
    }

    protected TextureArea getCircuitSlotOverlay() {
        return SusyTextures.INT_CIRCUIT_OVERLAY.get(isHighPressure);
    }

    protected SlotWidget getCircuitSlotToolTip(SlotWidget widget) {
        return widget.setTooltipText("gregtech.gui.configurator_slot.tooltip");
    }

    protected void addRecipeProgressBar(ModularUI.Builder builder, RecipeMap<?> map, int yOffset) {
        int x = 89 - progressIndicator.width / 2;
        int y = yOffset + 42 - progressIndicator.height/ 2;
        builder.widget(new RecipeProgressWidget(workableHandler::getProgressPercent, x, y, progressIndicator.width, progressIndicator.height, progressIndicator.progressBarTexture.get(isHighPressure), progressIndicator.progressMoveType, map));
    }

    protected void addInventorySlotGroup(ModularUI.Builder builder, IItemHandlerModifiable itemHandler, FluidTankList fluidHandler, boolean isOutputs, int yOffset) {
        int itemsSlotsCount = itemHandler.getSlots();
        int fluidSlotsCount = fluidHandler.getTanks() - ((isOutputs) ? 0 : 1) ; // Remove input steam tank

        boolean invertFluids = false;
        if (itemsSlotsCount == 0) {
            int tmp = itemsSlotsCount;
            itemsSlotsCount = fluidSlotsCount;
            fluidSlotsCount = tmp;
            invertFluids = true;
        }

        int[] inputSlotGrid = determineSlotsGrid(itemsSlotsCount);
        int itemsSlotsLeft = inputSlotGrid[0];
        int itemsSlotsDown = inputSlotGrid[1];

        boolean isVerticalFluid = itemsSlotsDown >= fluidSlotsCount && itemsSlotsLeft < 3;
        int fluidGridHeight = ((fluidSlotsCount / 3 == 0) ? 1 : fluidSlotsCount / 3);

        int fullGridHeight = itemsSlotsDown + (isVerticalFluid ? 0 : fluidGridHeight);
        if (fullGridHeight >= 3) yOffset += 4;

        int startInputsX = isOutputs ? 89 + progressIndicator.width / 2 + 9 : 89 - (progressIndicator.width / 2 + 9 + itemsSlotsLeft * 18);
        int startInputsY = yOffset + (isVerticalFluid ? 42 - ((itemsSlotsDown * 18) / 2) : 42 - (((fluidSlotsCount - 1) / 3 + 1) * 18));

        boolean wasGroup = itemHandler.getSlots() + fluidHandler.getTanks() == 12;
        if (wasGroup) startInputsY -= 9;
        else if (itemHandler.getSlots() >= 6 && fluidHandler.getTanks() >= 2 && !isOutputs) startInputsY -= 9;

        for (int i = 0; i < itemsSlotsDown; i++) {
            for (int j = 0; j < itemsSlotsLeft; j++) {
                int slotIndex = i * itemsSlotsLeft + j;
                if (slotIndex >= itemsSlotsCount) break;
                int x = startInputsX + 18 * j;
                int y = startInputsY + 18 * i;
                addSlot(builder, x, y, slotIndex, itemHandler, fluidHandler, invertFluids, isOutputs);
            }
        }

        if (wasGroup) startInputsY += 2;
        if (fluidSlotsCount > 0 || invertFluids) {
            if (isVerticalFluid) {
                int startSpecX = isOutputs ? startInputsX + itemsSlotsLeft * 18 : startInputsX - 18;
                for (int i = 0; i < fluidSlotsCount; i++) addSlot(builder, startSpecX, startInputsY + 18 * i, i, itemHandler, fluidHandler, !invertFluids, isOutputs);
            } else {
                int startSpecY = startInputsY + itemsSlotsDown * 18;
                for (int i = 0; i < fluidSlotsCount; i++) {
                    int x = isOutputs ? startInputsX + 18 * (i % 3) : startInputsX + itemsSlotsLeft * 18 - 18 - 18 * (i % 3);
                    int y = startSpecY + (i / 3) * 18;
                    addSlot(builder, x, y, i, itemHandler, fluidHandler, !invertFluids, isOutputs);
                }
            }
        }
    }

    protected void addSlot(ModularUI.Builder builder, int x, int y, int slotIndex, IItemHandlerModifiable itemHandler, FluidTankList fluidHandler, boolean isFluid, boolean isOutputs) {
        if (!isOutputs && isFluid) slotIndex++; //Skip steam slot
        if (!isFluid) builder.widget(new SlotWidget(itemHandler, slotIndex, x, y, true, !isOutputs)
                .setBackgroundTexture(getOverlaysForSlot(isOutputs, false)));
        else builder.widget(new TankWidget(fluidHandler.getTankAt(slotIndex), x, y, 18, 18).setAlwaysShowFull(true)
                    .setBackgroundTexture(getOverlaysForSlot(isOutputs, true))
                    .setContainerClicking(true, !isOutputs));
    }

    protected TextureArea[] getOverlaysForSlot(boolean isOutputs, boolean isFluid) {
        return new TextureArea[] {isFluid ? SusyGuiTextures.FLUID_SLOT_STEAM.get(isHighPressure) : GuiTextures.SLOT_STEAM.get(isHighPressure)};
    }

    protected static int[] determineSlotsGrid(int itemInputsCounts) {
        if (itemInputsCounts == 3) return new int[] {3, 1};
        int slotsLeft = (int) Math.ceil(Math.sqrt(itemInputsCounts));
        int slotsDown = (int) Math.ceil(itemInputsCounts / (double) slotsLeft);
        return new int[] {slotsLeft, slotsDown};
    }
}
