package supersymmetry.common.metatileentities.multi.rocket;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import gregtech.api.capability.*;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.IProgressBarMultiblock;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.recipes.Recipe;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import supersymmetry.api.SusyLog;
import supersymmetry.api.metatileentity.multiblock.IRedstoneControllable;
import supersymmetry.api.metatileentity.multiblock.SuSyPredicates;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.api.recipes.logic.RocketAssemblerLogic;
import supersymmetry.api.rocketry.components.AbstractComponent;
import supersymmetry.api.rocketry.rockets.AbstractRocketBlueprint;
import supersymmetry.api.util.DataStorageLoader;
import supersymmetry.common.blocks.BlockRocketAssemblerCasing;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.metatileentities.multiblockpart.MetaTileEntityComponentRedstoneController;
import supersymmetry.common.mui.widget.ItemCostWidget;
import supersymmetry.common.mui.widget.SlotWidgetMentallyStable;

public class MetaTileEntityRocketAssembler extends RecipeMapMultiblockController
                                           implements IProgressBarMultiblock, IRedstoneControllable {

    public DataStorageLoader blueprintSlot = new DataStorageLoader(
            this,
            x -> {
                if (x.hasTagCompound()) {
                    NBTTagCompound tag = x.getTagCompound();
                    AbstractRocketBlueprint bp = AbstractRocketBlueprint.getCopyOf(tag.getString("name"));
                    if (bp.readFromNBT(tag) && bp.isFullBlueprint()) {
                        // this.startAssembly(bp); <- this will nullref if you call it before its
                        // actually inserted, which
                        // happens after this function returns :C
                        return true;
                    }
                }
                return false;
            });

    // list of every component that has to be constructed.
    public List<AbstractComponent<?>> componentList = new ArrayList<>();

    public int componentIndex = 0;

    public boolean isWorking = false;
    private List<String> signalNames = new ArrayList<>();
    private List<Runnable> signalActions = new ArrayList<>();

    public MetaTileEntityRocketAssembler(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.ROCKET_ASSEMBLER);
        signalNames.add("start_assembly");
        signalNames.add("stop_assembly");
        signalActions.add(
                () -> {
                    if (!this.blueprintSlot.isEmpty()) {
                        this.startAssembly(this.getCurrentBlueprint());
                    }
                });
        signalActions.add(
                () -> {
                    this.abortAssembly();
                });
        this.recipeMapWorkable = new RocketAssemblerLogic(this); // <-- recipes are generated here
    }

    @Override
    public List<String> getSignals() {
        return signalNames;
    }

    @Override
    public String getSignalName(int sig) {
        return this.signalNames.get(sig);
    }

    @Override
    public void pulse(int sig) {
        this.signalActions.get(sig).run();
    }

    public AbstractRocketBlueprint getCurrentBlueprint() {
        if (blueprintSlot.isEmpty()) return null;
        NBTTagCompound tag = blueprintSlot.getStackInSlot(0).getTagCompound();
        AbstractRocketBlueprint bp = AbstractRocketBlueprint.getCopyOf(tag.getString("name"));
        if (bp.readFromNBT(tag)) {
            return bp;
        } else {
            SusyLog.logger.error("failed to read a blueprint {}", tag);
            return null;
            // hopefully never happens since its checked when the item is inserted
        }
    }

    public Recipe getCurrentRecipe() {
        return isWorking ? ((RocketAssemblerLogic) this.recipeMapWorkable).getComponentRecipe() : null;
    }

    public void abortAssembly() {
        this.blueprintSlot.setLocked(false);
        SusyLog.logger.info("assembly force stopped");
        this.isWorking = false;
        this.componentIndex = 0;
        this.componentList.clear();
        this.recipeMapWorkable.invalidate(); // this can break some things
    }

    public void finishAssembly() {
        SusyLog.logger.info("assembly finished");
        this.blueprintSlot.setLocked(false);
        this.isWorking = false;
        this.componentIndex = 0;
        this.componentList.clear();
        // TODO: actually spawn the rocket entity?
        // abortAssembly();
    }

    public void startAssembly(AbstractRocketBlueprint bp) {
        ((RocketAssemblerLogic) this.recipeMapWorkable).setInputsValid();
        this.componentIndex = 0;

        this.isWorking = true;
        this.componentList = bp.getStages().stream()
                .flatMap(x -> x.getComponents().values().stream())
                .flatMap(List::stream)
                .collect(Collectors.toList());
        // for (var input :
        // ((RocketAssemblerLogic) this.recipeMapWorkable).getComponentRecipe().getInputs()) {
        // SusyLog.logger.info(
        // "amount {} item {}",
        // input.getAmount(),
        // Stream.of(input.getInputStacks())
        // .map(x -> x.getDisplayName())
        // .collect(Collectors.toList()));
        // }
        this.blueprintSlot.setLocked(true);
    }

    public AbstractComponent<?> getCurrentCraftTarget() {
        if (isWorking && componentList.size() >= componentIndex + 1) {
            return this.componentList.get(this.componentIndex);
        } else {
            abortAssembly();
        }

        return null;
    }

    // meant to be called after a recipe is done
    public void nextComponent() {
        if (!isWorking) return;
        if (this.componentList.size() - 1 > this.componentIndex) {
            this.componentIndex++;
            SusyLog.logger.info(
                    "processing component {}/{}, isWorking:{},component:{}",
                    this.componentIndex,
                    this.componentList.size(),
                    this.isWorking,
                    getCurrentCraftTarget() == null ? null : getCurrentCraftTarget().getName());

        } else {
            finishAssembly();
        }
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.LOCK_OBJECT_HOLDER) {
            this.blueprintSlot.setLocked(buf.readBoolean());
        }
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Override
    public int getNumProgressBars() {
        return 2;
    }

    @Override
    public double getFillPercentage(int index) {
        if (!isStructureFormed()) return 0;
        if (index == 1 && isWorking && this.componentList.size() != 0) {
            return (float) (this.componentIndex + 1) / (float) this.componentList.size();
        }
        if (index == 0 && isWorking && this.recipeMapWorkable.isWorking()) {
            return (float) this.recipeMapWorkable.getProgress() / (float) this.recipeMapWorkable.getMaxProgress();
        }
        return 0;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityRocketAssembler(metaTileEntityId);
    }

    @Override
    protected boolean shouldShowVoidingModeButton() {
        return false;
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle(
                        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
                        "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
                        "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ")
                .aisle(
                        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
                        "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
                        "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
                        " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
                        " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
                        " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
                        " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
                        " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
                        " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
                        " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
                        " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
                        " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
                        " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
                        " PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP ")
                .aisle(
                        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        " P           P   P           P   P           P   P           P ")
                .aisle(
                        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        " P           P   P           P   P           P   P           P ")
                .aisle(
                        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        " P           P   P           P   P           P   P           P ")
                .aisle(
                        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB",
                        " P           P   P           P   P           P   P           P ")
                .aisle(
                        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
                        "RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        " P           P   P           P   P           P   P           P ")
                .aisle(
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        " P           P   P           P   P           P   P           P ")
                .aisle(
                        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
                        "RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        " P           P   P           P   P           P   P           P ")
                .aisle(
                        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB",
                        " P           P   P           P   P           P   P           P ")
                .aisle(
                        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        " P           P   P           P   P           P   P           P ")
                .aisle(
                        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        " P           P   P           P   P           P   P           P ")
                .aisle(
                        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        " P           P   P           P   P           P   P           P ")
                .aisle(
                        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
                        "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
                        "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
                        " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
                        " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
                        " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
                        " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
                        " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
                        " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
                        " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
                        " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
                        " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
                        " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
                        " PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP ")
                .aisle(
                        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
                        "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
                        "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCSMCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ")
                .where(' ', any())
                .where('M', maintenancePredicate())
                .where('S', selfPredicate())
                .where(
                        'F',
                        states(
                                SuSyBlocks.ROCKET_ASSEMBLER_CASING.getState(
                                        BlockRocketAssemblerCasing.RocketAssemblerCasingType.REINFORCED_FOUNDATION)))
                .where(
                        'C',
                        states(
                                SuSyBlocks.ROCKET_ASSEMBLER_CASING.getState(
                                        BlockRocketAssemblerCasing.RocketAssemblerCasingType.FOUNDATION))
                                                .or(MetaTileEntityComponentRedstoneController.controllerPredicate())
                                                .or(
                                                        abilities(MultiblockAbility.IMPORT_ITEMS)
                                                                .setPreviewCount(1)
                                                                .setMinGlobalLimited(1)
                                                                .setMaxGlobalLimited(2))
                                                .or(
                                                        abilities(MultiblockAbility.INPUT_ENERGY)
                                                                .setMinGlobalLimited(8)
                                                                .setMaxGlobalLimited(8)
                                                                .setPreviewCount(8)))
                .where('R', SuSyPredicates.rails())
                .where(
                        'P',
                        states(
                                SuSyBlocks.ROCKET_ASSEMBLER_CASING.getState(
                                        BlockRocketAssemblerCasing.RocketAssemblerCasingType.STRUCTURAL_FRAME)))
                .where(
                        'B',
                        states(
                                SuSyBlocks.ROCKET_ASSEMBLER_CASING.getState(
                                        BlockRocketAssemblerCasing.RocketAssemblerCasingType.RAILS)))
                .build();
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.ASSEMBLER_OVERLAY;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return createUITemplate(entityPlayer).build(getHolder(), entityPlayer);
    }

    // @Override
    // public void checkStructurePattern() {
    // SusyLog.logger.info("checkStructurePattern");
    // PatternMatchContext context =
    // structurePattern.checkPatternFastAt(
    // getWorld(), getPos(), getFrontFacing().getOpposite(), getUpwardsFacing(),
    // allowsFlip());
    // if (context != null && !this.isStructureFormed()) {
    // Set<IMultiblockPart> rawPartsSet = context.getOrCreate("MultiblockParts", HashSet::new);
    // ArrayList<IMultiblockPart> parts = new ArrayList<>(rawPartsSet);
    // for (var part : parts) {
    // SusyLog.logger.info("part: {}\n{}", part.getClass().toString(), part);
    // }
    // }
    //
    // super.checkStructurePattern();
    // }

    protected ModularUI.Builder createUITemplate(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 198, 208);
        // Display
        if (this.showProgressBar()) {
            builder.image(4, 4, 190, 109, GuiTextures.DISPLAY);

            if (this.getNumProgressBars() == 2) {
                // double bar
                ProgressWidget progressBar = new ProgressWidget(
                        () -> this.getFillPercentage(0),
                        4,
                        115,
                        94,
                        7,
                        this.getProgressBarTexture(0),
                        ProgressWidget.MoveType.HORIZONTAL)
                                .setHoverTextConsumer(list -> this.addBarHoverText(list, 0));
                builder.widget(progressBar);

                progressBar = new ProgressWidget(
                        () -> this.getFillPercentage(1),
                        100,
                        115,
                        94,
                        7,
                        this.getProgressBarTexture(1),
                        ProgressWidget.MoveType.HORIZONTAL)
                                .setHoverTextConsumer(list -> this.addBarHoverText(list, 1));
                builder.widget(progressBar);
            }

            builder.widget(
                    new IndicatorImageWidget(174, 93, 17, 17, getLogo())
                            .setWarningStatus(getWarningLogo(), this::addWarningText)
                            .setErrorStatus(getErrorLogo(), this::addErrorText));
        } else {
            builder.image(4, 4, 190, 117, GuiTextures.DISPLAY);
            builder.widget(
                    new IndicatorImageWidget(174, 101, 17, 17, getLogo())
                            .setWarningStatus(getWarningLogo(), this::addWarningText)
                            .setErrorStatus(getErrorLogo(), this::addErrorText));
        }

        builder.label(9, 9, getMetaFullName(), 0xFFFFFF);
        builder.widget(
                new AdvancedTextWidget(9, 20, this::addDisplayText, 0xFFFFFF)
                        .setMaxWidthLimit(181)
                        .setClickHandler(this::handleDisplayClick));

        // Power Butto
        IControllable controllable = getCapability(GregtechTileCapabilities.CAPABILITY_CONTROLLABLE, null);
        if (controllable != null) {
            builder.widget(
                    new ImageCycleButtonWidget(
                            173,
                            183,
                            18,
                            18,
                            GuiTextures.BUTTON_POWER,
                            controllable::isWorkingEnabled,
                            controllable::setWorkingEnabled));
            builder.widget(new ImageWidget(173, 201, 18, 6, GuiTextures.BUTTON_POWER_DETAIL));
        }

        // Flex Button
        builder.widget(
                new ClickButtonWidget(
                        173,
                        143,
                        18,
                        18,
                        "susy.machine.rocket_assembler.gui.start",
                        (clickData -> {
                            if (!this.blueprintSlot.isEmpty()) {
                                this.startAssembly(this.getCurrentBlueprint());
                            }
                        })));

        builder.widget(getFlexButton(173, 125, 18, 18));
        builder.dynamicLabel(
                110,
                52,
                () -> {
                    return !blueprintSlot.isEmpty() ? "" : I18n.format(this.getMetaName() + ".blueprint_slot.name");
                },
                0x404040);
        SlotWidgetMentallyStable blueprintslot = new SlotWidgetMentallyStable(this.blueprintSlot, 0, 170, 72);
        blueprintslot.setBackgroundTexture(GuiTextures.SLOT_DARK);
        blueprintslot.setChangeListener(
                () -> {
                    if (blueprintSlot.isEmpty()) {
                        this.abortAssembly();
                    }
                });
        builder.widget(blueprintslot);
        builder.widget(
                new ItemCostWidget(
                        new Size(158, 70),
                        new Position(9, 60),
                        this::getCurrentRecipe,
                        this.recipeMapWorkable::isWorking));
        builder.bindPlayerInventory(entityPlayer.inventory, 125);
        return builder;
    }

    @Override
    protected @NotNull Widget getFlexButton(int x, int y, int width, int height) {
        return getStopButton(x, y, width, height);
    }

    protected @NotNull Widget getStopButton(int x, int y, int width, int height) {
        return new ClickButtonWidget(
                x,
                y,
                width,
                height,
                "susy.machine.rocket_assembler.gui.stop",
                (clickData -> {
                    this.abortAssembly();
                }));
    }
}
