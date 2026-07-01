package supersymmetry.common.metatileentities.multi.rocket;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import org.jetbrains.annotations.NotNull;

import cam72cam.mod.entity.ModdedEntity;
import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.TextureArea;
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
import gregtech.api.util.RelativeDirection;
import gregtech.api.util.Size;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.*;
import supersymmetry.api.SusyLog;
import supersymmetry.api.gui.SusyGuiTextures;
import supersymmetry.api.metatileentity.multiblock.IRedstoneControllable;
import supersymmetry.api.metatileentity.multiblock.SuSyPredicates;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.api.recipes.logic.RocketAssemblerLogic;
import supersymmetry.api.rocketry.components.AbstractComponent;
import supersymmetry.api.rocketry.rockets.AbstractRocketBlueprint;
import supersymmetry.api.util.DataStorageLoader;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.BlockRocketAssemblerCasing;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.entities.EntityTransporterErector;
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
                    if (bp != null && bp.readFromNBT(tag) && bp.isFullBlueprint()) {
                        return true;
                    }
                }
                return false;
            });

    // list of every component that has to be constructed.
    public List<AbstractComponent<?>> componentList = new ArrayList<>();
    public int componentIndex = 0;
    public boolean isAssemblyWorking = false;
    private List<Runnable> signalActions = new ArrayList<>();

    public MetaTileEntityRocketAssembler(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.ROCKET_ASSEMBLER);
        signalActions.add(
                () -> {
                    if (!this.blueprintSlot.isEmpty() && this.componentList.isEmpty()) {
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
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data = super.writeToNBT(data);
        data.setTag("blueprint", this.blueprintSlot.getStackInSlot(0).writeToNBT(new NBTTagCompound()));

        data.setBoolean("isWorking", isAssemblyWorking);
        data.setInteger("componentIndex", componentIndex);

        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        NBTTagCompound stackdata = (NBTTagCompound) data.getTag("blueprint");
        this.isAssemblyWorking = data.getBoolean("isWorking");
        if (stackdata != null && stackdata != new NBTTagCompound()) {
            ItemStack stack = new ItemStack(stackdata);
            this.blueprintSlot.setStackInSlot(0, stack);
            if (!this.blueprintSlot.isEmpty() && isAssemblyWorking) {
                this.startAssembly(this.getCurrentBlueprint());
            }
        }
        this.componentIndex = data.getInteger("componentIndex");
        this.blueprintSlot.setLocked(this.isAssemblyWorking);
    }

    public List<Runnable> getSignalActions() {
        return signalActions;
    }

    @Override
    public int getSignalCeiling() {
        return signalActions.size() - 1;
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
        return isAssemblyWorking ? ((RocketAssemblerLogic) this.recipeMapWorkable).getRecipe(100000) : null;
    }

    public void abortAssembly() {
        this.blueprintSlot.setLocked(false);
        this.isAssemblyWorking = false;
        this.componentIndex = 0;
        this.componentList.clear();
        // Clear any partially-built rocket left on the erector.
        EntityTransporterErector erector = findTransporterErector();
        if (erector != null) {
            erector.setRocketLoaded(false);
        }
        this.recipeMapWorkable.invalidate(); // this can break some things
    }

    public void finishAssembly() {
        this.blueprintSlot.setLocked(false);
        this.isAssemblyWorking = false;
        this.componentIndex = 0;
        this.componentList.clear();
        EntityTransporterErector erector = findTransporterErector();

        if (erector != null) {
            erector.setRocketLoaded(true);
            NBTTagCompound rocketNBT = erector.getRocketNBT();
            rocketNBT.setLong("assemblerPosition", this.getPos().toLong());
            rocketNBT.setTag("rocket", this.getCurrentBlueprint().writeToNBT());
        }
    }

    public EntityTransporterErector findTransporterErector() {
        AxisAlignedBB internalBB = this.getInternalBB();
        if (this.getWorld() == null) {
            return null;
        }
        List<ModdedEntity> trains = getWorld().getEntitiesWithinAABB(ModdedEntity.class, internalBB);

        if (!trains.isEmpty()) {
            for (ModdedEntity forgeTrainEntity : trains) {
                if (forgeTrainEntity.getSelf() instanceof EntityTransporterErector rollingStock &&
                        !rollingStock.isRocketLoaded()) {
                    return rollingStock;
                }
            }
        }
        return null;
    }

    public void startAssembly(AbstractRocketBlueprint bp) {
        ((RocketAssemblerLogic) this.recipeMapWorkable).setInputsValid();
        this.componentIndex = 0;

        this.isAssemblyWorking = true;
        this.componentList = bp.getStages().stream()
                .flatMap(x -> x.getComponents().values().stream())
                .flatMap(List::stream)
                .collect(Collectors.toList());
        this.blueprintSlot.setLocked(true);
    }

    public AbstractComponent<?> getCurrentCraftTarget() {
        if (isAssemblyWorking && componentList.size() >= componentIndex + 1) {
            return this.componentList.get(this.componentIndex);
        } else {
            abortAssembly();
        }

        return null;
    }

    // meant to be called after a recipe is done
    public void nextComponent() {
        if (!isAssemblyWorking) return;
        this.componentIndex++;
    }

    public boolean hasSuitableErector() {
        EntityTransporterErector erector = findTransporterErector();
        if (erector == null) {
            return false;
        }
        if (this.componentList.isEmpty()) {
            return erector.getNextAssemblyProgress() == 0;
        }
        return erector.getNextAssemblyProgress() == (float) this.componentIndex / this.componentList.size();
    }

    public void displayAssemblerProgress() {
        // Reveal the rocket up to the components built so far on the erector. This is synced to the client
        // by the T/E itself.
        EntityTransporterErector erector = findTransporterErector();
        if (erector != null && !this.componentList.isEmpty()) {
            erector.setAssemblyProgress((float) this.componentIndex / this.componentList.size(),
                    (float) (this.componentIndex + 1) / this.componentList.size(),
                    this.getWorld().getTotalWorldTime(),
                    this.getWorld().getTotalWorldTime() + this.getRecipeLogic().getMaxProgress());
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isAssemblyWorking);
        buf.writeInt(componentIndex);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.isAssemblyWorking = buf.readBoolean();
        this.componentIndex = buf.readInt();
        this.blueprintSlot.setLocked(this.isAssemblyWorking);
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
        if (index == 1 && isAssemblyWorking && this.componentList.size() != 0) {
            return (float) (this.componentIndex + 1) / (float) this.componentList.size();
        }
        if (index == 0 && isAssemblyWorking && this.recipeMapWorkable.isWorking()) {
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

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return SusyTextures.AFS_OVERLAY;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(),
                this.isStructureFormed(), true);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return createUITemplate(entityPlayer).build(getHolder(), entityPlayer);
    }

    protected ModularUI.Builder createUITemplate(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 198, 216);
        builder.image(4, 4, 190, 117, GuiTextures.DISPLAY)
                .widget(new ProgressWidget(
                        () -> this.getFillPercentage(0),
                        4, 123, 94, 7,
                        this.getProgressBarTexture(0),
                        ProgressWidget.MoveType.HORIZONTAL))
                .widget(new ProgressWidget(
                        () -> this.getFillPercentage(1),
                        100, 123, 94, 7,
                        this.getProgressBarTexture(1),
                        ProgressWidget.MoveType.HORIZONTAL)
                                .setHoverTextConsumer(this::addBarHoverText));
        builder.widget(
                new IndicatorImageWidget(174, 101, 17, 17, getLogo())
                        .setWarningStatus(getWarningLogo(), this::addWarningText)
                        .setErrorStatus(getErrorLogo(), this::addErrorText));

        builder.label(9, 9, getMetaFullName(), 0xFFFFFF);
        // TODO make this take less space so that the ItemCostWidget has more space
        builder.widget(
                new AdvancedTextWidget(9, 20, this::addDisplayText, 0xFFFFFF)
                        .setMaxWidthLimit(181)
                        .setClickHandler(this::handleDisplayClick));

        // Power Button
        IControllable controllable = getCapability(GregtechTileCapabilities.CAPABILITY_CONTROLLABLE, null);
        if (controllable != null) {
            builder.widget(
                    new ImageCycleButtonWidget(
                            173,
                            191,
                            18,
                            18,
                            GuiTextures.BUTTON_POWER,
                            controllable::isWorkingEnabled,
                            controllable::setWorkingEnabled));
            builder.widget(new ImageWidget(173, 209, 18, 6, GuiTextures.BUTTON_POWER_DETAIL));
        }

        // start button
        builder.widget(
                new ClickButtonWidget(
                        173,
                        151,
                        18,
                        18,
                        "",
                        (clickData -> {
                            if (!this.blueprintSlot.isEmpty() && this.componentList.isEmpty()) {
                                this.startAssembly(this.getCurrentBlueprint());
                            }
                        }))
                                .setTooltipText("susy.machine.rocket_assembler.gui.start")
                                .setButtonTexture(SusyGuiTextures.ROCKET_ASSEMBLER_BUTTON_START));
        // stop button
        builder.widget(
                new ClickButtonWidget(
                        173,
                        133,
                        18,
                        18,
                        "",
                        (clickData1 -> {
                            this.abortAssembly();
                        }))
                                .setButtonTexture(SusyGuiTextures.ROCKET_ASSEMBLER_BUTTON_STOP)
                                .setTooltipText("susy.machine.rocket_assembler.gui.stop"));
        builder.dynamicLabel(
                40,
                79,
                () -> {
                    return !blueprintSlot.isEmpty() ? "" : I18n.format(this.getMetaName() + ".blueprint_slot.name");
                },
                0x404040);
        SlotWidgetMentallyStable blueprintslot = new SlotWidgetMentallyStable(this.blueprintSlot, 0, 173, 79);
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
                        new Size(158, 50),
                        new Position(9, 70),
                        this::getCurrentRecipe,
                        // TODO less stupid predicate thats synced to the client
                        // because isAssemblyWorking is not
                        () -> this.blueprintSlot.isLocked()));

        builder.bindPlayerInventory(entityPlayer.inventory, 133);
        return builder;
    }

    private void addBarHoverText(List<ITextComponent> iTextComponents) {
        if (componentList.isEmpty())
            return;
        iTextComponents.add(new TextComponentTranslation("susy.machine.rocket_assembler.gui.overall_progress",
                this.componentIndex, this.componentList.size(),
                String.format("%.1f", (double) 100 * this.componentIndex / this.componentList.size())));
    }

    @Override
    protected void addWarningText(List<ITextComponent> textList) {
        super.addWarningText(textList);
        if (isAssemblyWorking && !((RocketAssemblerLogic) recipeMapWorkable).hasEnoughElectrodes) {
            textList.add(new TextComponentTranslation("susy.machine.rocket_assembler.warning.no_electrodes"));
        }
    }

    @Override
    protected void addErrorText(List<ITextComponent> textList) {
        super.addErrorText(textList);
        if (isAssemblyWorking && findTransporterErector() == null) {
            textList.add(new TextComponentTranslation("susy.machine.rocket_assembler.error.no_erector"));
        }
    }

    @Override
    protected @NotNull Widget getFlexButton(int x, int y, int width, int height) {
        return new ClickButtonWidget(
                x,
                y,
                width,
                height,
                "",
                (clickData -> {
                    this.abortAssembly();
                }))
                        .setButtonTexture(SusyGuiTextures.ROCKET_ASSEMBLER_BUTTON_STOP)
                        .setTooltipText("susy.machine.rocket_assembler.gui.stop");
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
                        "             VV VV           VV VV           VV VV             ",
                        "              VGV             VGV             VGV              ",
                        "BBBBBBBBBBBBBBBGBBBBBBBBBBBBBBBGBBBBBBBBBBBBBBBGBBBBBBBBBBBBBBB",
                        " P           PHHHP           PHHHP           PHHHP           P ")
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
                        "             VV VV           VV VV           VV VV             ",
                        "              VGV             VGV             VGV              ",
                        "BBBBBBBBBBBBBBBGBBBBBBBBBBBBBBBGBBBBBBBBBBBBBBBGBBBBBBBBBBBBBBB",
                        " P           PHHHP           PHHHP           PHHHP           P ")
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
                        "                                                               ",
                        "                                                               ")
                .where(' ', any())
                .where('M', maintenancePredicate())
                .where('S', selfPredicate())
                .where(
                        'F',
                        states(
                                MetaBlocks.STONE_BLOCKS.get(StoneVariantBlock.StoneVariant.SMOOTH)
                                        .getState(StoneVariantBlock.StoneType.CONCRETE_LIGHT)))
                .where(
                        'C',
                        states(MetaBlocks.STONE_BLOCKS.get(StoneVariantBlock.StoneVariant.SMOOTH)
                                .getState(StoneVariantBlock.StoneType.CONCRETE_LIGHT))
                                        .or(MetaTileEntityComponentRedstoneController.controllerPredicate()
                                                .setMaxGlobalLimited(2))
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
                .where('G',
                        states(MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_GEARBOX)))
                .where('H', states(MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.STEEL_PIPE)))
                .where('V', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID)))
                .build();
    }

    protected @NotNull Widget getStopButton(int x, int y, int width, int height) {
        return new ClickButtonWidget(
                x,
                y,
                width,
                height,
                "",
                (clickData -> {
                    this.abortAssembly();
                }))
                        .setButtonTexture(SusyGuiTextures.ROCKET_ASSEMBLER_BUTTON_STOP)
                        .setTooltipText("susy.machine.rocket_assembler.gui.stop");
    }

    private AxisAlignedBB getInternalBB() {
        EnumFacing front = getFrontFacing();
        // The left side of the controller, not from the player's perspective
        EnumFacing left = RelativeDirection.LEFT.getRelativeFacing(front, getUpwardsFacing(), isFlipped());
        EnumFacing up = RelativeDirection.UP.getRelativeFacing(front, getUpwardsFacing(), isFlipped());

        BlockPos pos = getPos();
        var v1 = pos.offset(left.getOpposite(), 33).offset(up.getOpposite(), 4);
        var v2 = pos.offset(left, 33).offset(up, 10).offset(front.getOpposite(), 17);
        return new AxisAlignedBB(v1, v2);
    }

    @Override
    public TextureArea getProgressBarTexture(int index) {
        return index == 0 ? SusyGuiTextures.PROGRESS_BAR_ROCKET_ASSEMBLER_COMPONENT :
                SusyGuiTextures.PROGRESS_BAR_ROCKET_ASSEMBLER_OVERALL;
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                      CuboidRayTraceResult hitResult) {
        if (playerIn.isCreative() && this.getCurrentBlueprint() != null) {
            finishAssembly();
            return true;
        }
        return false;
    }
}
