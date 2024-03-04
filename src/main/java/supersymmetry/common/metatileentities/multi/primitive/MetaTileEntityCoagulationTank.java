package supersymmetry.common.metatileentities.multi.primitive;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.IGhostSlotConfigurable;
import gregtech.api.capability.impl.GhostCircuitItemStackHandler;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapPrimitiveMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.api.util.GTTransferUtils;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import supersymmetry.api.gui.SusyGuiTextures;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.BlockCoagulationTankWall;
import supersymmetry.common.blocks.SuSyBlocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

public class MetaTileEntityCoagulationTank extends RecipeMapPrimitiveMultiblockController implements IGhostSlotConfigurable {

    @Nullable
    protected GhostCircuitItemStackHandler circuitInventory;
    private IItemHandlerModifiable actualImportItems;

    public MetaTileEntityCoagulationTank(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.COAGULATION_RECIPES);
        circuitInventory = new GhostCircuitItemStackHandler(this);
        circuitInventory.addNotifiableMetaTileEntity(this);
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityCoagulationTank(this.metaTileEntityId);
    }

    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle(new String[]{"XXX", "XXX", "XXX"})
                .aisle(new String[]{"XXX", "X#X", "X#X"}).setRepeatable(1, 4)
                .aisle(new String[]{"XXX", "XSX", "XXX"})
                .where('X',
                        states(new IBlockState[]{SuSyBlocks.COAGULATION_TANK_WALL
                                .getState(BlockCoagulationTankWall.CoagulationTankWallType.WOODEN_COAGULATION_TANK_WALL)})
                        .or(abilities(MultiblockAbility.EXPORT_ITEMS)
                                .setMaxGlobalLimited(1))
                        .or(abilities(MultiblockAbility.IMPORT_FLUIDS)
                                    .setMaxGlobalLimited(1)))
                .where('#', air())
                .where('S', this.selfPredicate()).build();
    }

    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return SusyTextures.WOODEN_COAGULATION_TANK_WALL;
    }

    protected ModularUI.Builder createUITemplate(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.PRIMITIVE_BACKGROUND, 176,166);
        builder.label(6, 6, this.getMetaFullName());
        builder.widget(new RecipeProgressWidget(this.recipeMapWorkable::getProgressPercent, 76, 41, 20, 15,
                GuiTextures.PRIMITIVE_BLAST_FURNACE_PROGRESS_BAR, ProgressWidget.MoveType.HORIZONTAL, SuSyRecipeMaps.COAGULATION_RECIPES));

        builder.widget((new SlotWidget(this.importItems, 0, 30, 30, true, true)
                .setBackgroundTexture(GuiTextures.PRIMITIVE_SLOT)));
        builder.widget((new SlotWidget(this.importItems, 1, 48, 30, true, true)
                .setBackgroundTexture(GuiTextures.PRIMITIVE_SLOT)));
        builder.widget((new TankWidget(this.importFluids.getTankAt(1), 30, 48, 18, 18))
                .setAlwaysShowFull(true)
                .setBackgroundTexture(GuiTextures.FLUID_SLOT)
                .setContainerClicking(true, true));
        builder.widget((new TankWidget(this.importFluids.getTankAt(0), 48, 48, 18, 18))
                .setAlwaysShowFull(true).setBackgroundTexture(GuiTextures.FLUID_SLOT)
                .setContainerClicking(true, true));
        builder.widget((new SlotWidget(this.exportItems, 0, 106, 39, true, false)
                .setBackgroundTexture(GuiTextures.PRIMITIVE_SLOT)));

        SlotWidget circuitSlot = new GhostCircuitSlotWidget(circuitInventory, 0, 124, 62)
                .setBackgroundTexture(GuiTextures.PRIMITIVE_SLOT, SusyGuiTextures.INT_CIRCUIT_OVERLAY_STEAM.get(true));
        builder.widget(getCircuitSlotTooltip(circuitSlot))
                .widget(new ClickButtonWidget(115, 62, 9, 9, "", click -> circuitInventory.addCircuitValue(click.isShiftClick ? 5 : 1)).setShouldClientCallback(true).setButtonTexture(GuiTextures.BUTTON_INT_CIRCUIT_PLUS).setDisplayFunction(() -> circuitInventory.hasCircuitValue() && circuitInventory.getCircuitValue() < IntCircuitIngredient.CIRCUIT_MAX))
                .widget(new ClickButtonWidget(115, 71, 9, 9, "", click -> circuitInventory.addCircuitValue(click.isShiftClick ? -5 : -1)).setShouldClientCallback(true).setButtonTexture(GuiTextures.BUTTON_INT_CIRCUIT_MINUS).setDisplayFunction(() -> circuitInventory.hasCircuitValue() && circuitInventory.getCircuitValue() > IntCircuitIngredient.CIRCUIT_MIN));

        return builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.PRIMITIVE_SLOT, 0);
    }

    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, this.getFrontFacing(),
                this.recipeMapWorkable.isActive(), this.recipeMapWorkable.isWorkingEnabled());
    }

    public void update() {
        super.update();
        if (this.getOffsetTimer() % 5 == 0 && this.isStructureFormed()){
            for (IFluidTank tank : getAbilities(MultiblockAbility.IMPORT_FLUIDS)) {
                if(tank.getFluid() != null){
                    NonNullList<FluidStack> fluidStacks = NonNullList.create();
                    int toFill = (this.importFluids.getTankAt(0).getCapacity() - this.importFluids.getTankAt(0).getFluidAmount());
                    int amount = Math.min(tank.getFluidAmount(), toFill);
                    fluidStacks.add(new FluidStack(tank.getFluid().getFluid(),amount));
                    if(GTTransferUtils.addFluidsToFluidHandler(this.importFluids,true, fluidStacks)) {
                        GTTransferUtils.addFluidsToFluidHandler(this.importFluids,false, fluidStacks);
                        tank.drain(amount, true);
                    }
                }
            }
            for (int i = 0; i < this.exportItems.getSlots(); i++) {
                ItemStack stack = this.exportItems.getStackInSlot(i);
                this.exportItems.setStackInSlot(i,GTTransferUtils.insertItem(new ItemHandlerList(this.getAbilities(MultiblockAbility.EXPORT_ITEMS)), stack,false));
            }
            this.fillInternalTankFromFluidContainer();
        }
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
    }

    @Override
    protected void initializeAbilities() {
        super.initializeAbilities();
    }

    @Nonnull
    protected ICubeRenderer getFrontOverlay() {
        return Textures.PRIMITIVE_PUMP_OVERLAY;
    }

    @Override

    public boolean hasMaintenanceMechanics() {
        return false;
    }

    @Override
    public boolean hasGhostCircuitInventory() {
        return true;
    }

    @Override
    public void setGhostCircuitConfig(int config) {
        if (this.circuitInventory == null || this.circuitInventory.getCircuitValue() == config) {
            return;
        }
        this.circuitInventory.setCircuitValue(config);
        if (!getWorld().isRemote) {
            markDirty();
        }
    }

    protected SlotWidget getCircuitSlotTooltip(SlotWidget widget) {
        String configString;

        if (circuitInventory == null || circuitInventory.getCircuitValue() == GhostCircuitItemStackHandler.NO_CONFIG) {
            configString = new TextComponentTranslation("gregtech.gui.configurator_slot.no_value").getFormattedText();
        } else {
            configString = String.valueOf(circuitInventory.getCircuitValue());
        }

        return widget.setTooltipText("gregtech.gui.configurator_slot.tooltip", configString);
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        actualImportItems = null;
    }

    @Override
    public IItemHandlerModifiable getImportItems() {
        if (actualImportItems == null) actualImportItems = circuitInventory == null ? super.getImportItems() : new ItemHandlerList(Arrays.asList(super.getImportItems(), circuitInventory));
        return actualImportItems;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        if (circuitInventory != null) circuitInventory.write(data);
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
                    stack = GTTransferUtils.insertItem(importItems, stack, false);
                    circuitInventory.setCircuitValueFromStack(stack);
                }
            } else {
                circuitInventory.read(data);
            }
        }
    }
}
