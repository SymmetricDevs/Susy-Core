package supersymmetry.common.metatileentities.multi.rocket;

import static supercritical.api.pattern.SCPredicates.FLUID_BLOCKS_KEY;
import static supercritical.api.pattern.SCPredicates.fluid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import org.jetbrains.annotations.NotNull;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.Widget.ClickData;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.IndicatorImageWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.api.util.TextComponentUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockGlassCasing;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.MetaBlocks;
import supersymmetry.api.SusyLog;
import supersymmetry.api.rocketry.components.AbstractComponent;
import supersymmetry.api.rocketry.rockets.AbstractRocketBlueprint;
import supersymmetry.api.rocketry.rockets.RocketStage;
import supersymmetry.api.util.DataStorageLoader;
import supersymmetry.common.blocks.BlockSerpentine;
import supersymmetry.common.blocks.BlockRocketMultiblockCasing;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.blocks.rocketry.BlockProcessorCluster;
import supersymmetry.common.item.SuSyMetaItems;
import supersymmetry.common.materials.SusyMaterials;
import supersymmetry.common.mui.widget.RocketStageDisplayWidget;
import supersymmetry.common.mui.widget.SlotWidgetMentallyStable;
import supersymmetry.common.rocketry.SusyRocketComponents;

public class MetaTileEntityBlueprintAssembler extends MultiblockWithDisplayBase {

    private boolean coolantFilled;
    private List<BlockPos> coolantPositions;

    public IMultipleTankHandler inputCoolant;

    public static Map<String, Map<String, List<DataStorageLoader>>> generateSlotsFromBlueprint(
                                                                                               AbstractRocketBlueprint bp,
                                                                                               MetaTileEntity mte) {
        Map<String, Map<String, List<DataStorageLoader>>> map = new HashMap<>();
        // copy the array because it explodes if you dont
        for (RocketStage stage : new ArrayList<>(bp.stages)) {

            Map<String, List<DataStorageLoader>> stageComponents = new HashMap<>();
            for (String componentname : new HashSet<>(stage.componentLimits.keySet())) {
                List<DataStorageLoader> slots = new ArrayList<>();
                for (int i = 0; i < stage.maxComponentsOf(componentname); i++) {
                    slots.add(
                            new DataStorageLoader(
                                    mte,
                                    x -> {
                                        if (SuSyMetaItems.isMetaItem(x) == SuSyMetaItems.DATA_CARD_ACTIVE.metaValue) {
                                            if (x.hasTagCompound()) {
                                                AbstractComponent<?> c = AbstractComponent.getComponentFromName(
                                                        x.getTagCompound().getString("name"));
                                                if (c.getComponentSlotValidator().test(componentname)) {
                                                    return true;
                                                }
                                            }
                                        }
                                        return false;
                                    }));
                }
                stageComponents.put(componentname, slots);
            }
            map.put(stage.getName(), stageComponents);
        }
        return map;
    }

    // live widemann reaction
    // https://discord.com/channels/881234100504109166/881234101103890454/1402784628603097270
    public Map<String, Map<String, List<DataStorageLoader>>> slots = new HashMap<>();

    // TODO: make this only accept incomplete blueprints, it somehow breaks when they are already
    // built :c
    public DataStorageLoader rocketBlueprintSlot = new DataStorageLoader(
            this,
            item -> SuSyMetaItems.isMetaItem(item) == SuSyMetaItems.DATA_CARD_MASTER_BLUEPRINT.metaValue
    // &&
    // item.getTagCompound() != null
    // && !item.getTagCompound().getBoolean("buildstat")
    );

    public void fillCoolant(List<BlockPos> toFill, Fluid fluid, IMultipleTankHandler fluidInputs) {
        if (fluidInputs != null) {
            FluidStack toDrain = new FluidStack(fluid, 1000);
            FluidStack drained = fluidInputs.drain(toDrain, false);
            if (drained != null && drained.amount != 0) {
                if (drained.amount == 1000) {
                    World world = this.getWorld();
                    BlockPos pos = (BlockPos) toFill.get(0);
                    if (world.isBlockLoaded(pos) &&
                            (world.isAirBlock(pos) || world.getBlockState(pos).getBlock() == fluid.getBlock())) {
                        world.setBlockState(pos, fluid.getBlock().getDefaultState(), 2);
                        fluidInputs.drain(drained, true);
                        toFill.remove(0);
                    }
                }

            }
        }
    }

    public MetaTileEntityBlueprintAssembler(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);

        this.inputCoolant = new FluidTankList(true);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    public IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(
                MetalCasingType.STEEL_SOLID); // replace with real values later pls
    }

    public IBlockState getComputerState() {
        return SuSyBlocks.PROCESSOR_CLUSTER
                .getState(BlockProcessorCluster.TierType.TIER_1);
    }

    @Override
    public ModularUI getModularUI(EntityPlayer entityPlayer) {
        return null; // createGUITemplate(entityPlayer).build(this.getHolder(), entityPlayer);
    }

    @Override
    public void update() {
        super.update();
        if (!this.getWorld().isRemote) {
            this.rocketBlueprintSlot.setLocked(!this.slotsEmpty());
        }
    }

    public void onBlueprintRemoved(RocketStageDisplayWidget mainwindow) {
        // if (this.slots.isEmpty() )
        this.slots.clear();

        mainwindow.selectedStageIndex = 0;
        mainwindow.error = RocketStage.ComponentValidationResult.UNKNOWN;
        mainwindow.setVisible(false);
        mainwindow.setActive(false);
    }

    // this should only be ran when a new blueprint item is inserted, as doing this will void items
    public void onBlueprintInserted(RocketStageDisplayWidget mainwindow) {
        mainwindow.setActive(true);
        mainwindow.setVisible(true);
        if (!rocketBlueprintSlot.isEmpty() && rocketBlueprintSlot.getStackInSlot(0).hasTagCompound()) {
            NBTTagCompound tag = rocketBlueprintSlot.getStackInSlot(0).getTagCompound();
            AbstractRocketBlueprint bp = AbstractRocketBlueprint.getCopyOf(tag.getString("name"));
            if (bp.readFromNBT(tag)) {
                this.slots = generateSlotsFromBlueprint(bp, this);
                mainwindow.generateFromBlueprint(bp);
            } else {
                SusyLog.logger.error("invalid blueprint");
            }
        }
    }

    public void buildBlueprint(ClickData clickData, RocketStageDisplayWidget mainWindow) {
        if (!rocketBlueprintSlot.isEmpty() && rocketBlueprintSlot.getStackInSlot(0).hasTagCompound()) {
            NBTTagCompound tag = rocketBlueprintSlot.getStackInSlot(0).getTagCompound();
            AbstractRocketBlueprint bp = AbstractRocketBlueprint.getCopyOf(tag.getString("name"));
            if (mainWindow.blueprintBuildAttempt(bp)) {
                // SusyLog.logger.info("build success, blueprint writeout: {}", bp.writeToNBT());
                this.rocketBlueprintSlot.setNBT(_ -> bp.writeToNBT());
            }
            // else {
            // SusyLog.logger.info(
            // "status: {}\nerror stage: {}\nerror component: {}",
            // mainWindow.error.getTranslationKey(),
            // mainWindow.errorStage,
            // mainWindow.errorComponentType);
            // }
        }
    }

    // TODO: remove ts
    public void setDefaultBlueprint(Widget.ClickData data) {
        rocketBlueprintSlot.clearNBT();
        NBTTagCompound tag = SusyRocketComponents.ROCKET_SOYUZ_BLUEPRINT_DEFAULT.writeToNBT();
        rocketBlueprintSlot.setNBT(_ -> tag);
        SusyLog.logger.info("set the nbt to {}", tag);
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.LOCK_OBJECT_HOLDER) {
            rocketBlueprintSlot.setLocked(buf.readBoolean());
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        NBTTagCompound tag = super.writeToNBT(data);
        NBTTagCompound guiSlots = new NBTTagCompound();
        for (Entry<String, Map<String, List<DataStorageLoader>>> stageEntry : this.slots.entrySet()) {
            NBTTagCompound stage = new NBTTagCompound();
            for (Entry<String, List<DataStorageLoader>> componentEntry : stageEntry.getValue().entrySet()) {
                NBTTagList cards = new NBTTagList();
                for (DataStorageLoader componentCard : componentEntry.getValue()) {
                    NBTTagCompound item = componentCard.getStackInSlot(0).writeToNBT(new NBTTagCompound());
                    cards.appendTag(item);
                }
                stage.setTag(componentEntry.getKey(), cards);
            }
            guiSlots.setTag(stageEntry.getKey(), stage);
        }
        tag.setTag(
                "blueprint_slot",
                this.rocketBlueprintSlot.getStackInSlot(0).writeToNBT(new NBTTagCompound()));
        tag.setTag("gui_slots", guiSlots);

        return tag;
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        NBTTagCompound guiSlots = new NBTTagCompound();
        for (Entry<String, Map<String, List<DataStorageLoader>>> stageEntry : this.slots.entrySet()) {
            NBTTagCompound stage = new NBTTagCompound();
            for (Entry<String, List<DataStorageLoader>> componentEntry : stageEntry.getValue().entrySet()) {
                NBTTagList cards = new NBTTagList();
                for (DataStorageLoader componentCard : componentEntry.getValue()) {
                    NBTTagCompound item = componentCard.getStackInSlot(0).writeToNBT(new NBTTagCompound());
                    cards.appendTag(item);
                }
                stage.setTag(componentEntry.getKey(), cards);
            }
            guiSlots.setTag(stageEntry.getKey(), stage);
        }
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag(
                "blueprint_slot",
                this.rocketBlueprintSlot.getStackInSlot(0).writeToNBT(new NBTTagCompound()));
        tag.setTag("guiSlots", guiSlots);
        buf.writeCompoundTag(tag);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        try {
            var data = buf.readCompoundTag();
            NBTTagCompound emptyTag = new NBTTagCompound();
            NBTTagList emptyTagList = new NBTTagList();
            NBTTagCompound blueprintTag = data.getCompoundTag("blueprint_slot");
            if (blueprintTag != null) {
                ItemStack blueprintStack = new ItemStack(blueprintTag);
                this.rocketBlueprintSlot.setStackInSlot(0, blueprintStack);

                if (SuSyMetaItems.isMetaItem(blueprintStack) == SuSyMetaItems.DATA_CARD_MASTER_BLUEPRINT.metaValue) {
                    if (blueprintStack.hasTagCompound()) {
                        AbstractRocketBlueprint bp = AbstractRocketBlueprint.getCopyOf(
                                blueprintStack.getTagCompound().getString("name"));
                        if (bp.readFromNBT(blueprintStack.getTagCompound())) {
                            this.slots = generateSlotsFromBlueprint(bp, this);
                            NBTTagCompound guiSlotsTag = data.getCompoundTag("gui_slots");
                            if (guiSlotsTag != emptyTag) {
                                for (String stageKey : guiSlotsTag.getKeySet()) {
                                    NBTTagCompound stageTag = guiSlotsTag.getCompoundTag(stageKey);
                                    if (stageTag == emptyTag) {
                                        continue;
                                    }
                                    for (String componentKey : stageTag.getKeySet()) {
                                        NBTTagList component_cards = stageTag.getTagList(componentKey,
                                                NBT.TAG_COMPOUND);
                                        if (component_cards == emptyTagList) {
                                            continue;
                                        }
                                        for (int i = 0; i < component_cards.tagCount(); i++) {
                                            NBTTagCompound stackTag = component_cards.getCompoundTagAt(i);
                                            if (stackTag == emptyTag) {
                                                continue;
                                            }
                                            ItemStack stack = new ItemStack(stackTag);
                                            this.slots.get(stageKey).get(componentKey).get(i).setStackInSlot(0, stack);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                this.rocketBlueprintSlot.setStackInSlot(0, ItemStack.EMPTY);
            }

        } catch (Exception e) {}
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        NBTTagCompound empty_tag = new NBTTagCompound();
        // assuming that new NBTTagCompound() == new NBTTagCompound() and
        // there are no funny java things going on :pray:
        NBTTagList empty_tag_list = new NBTTagList();
        NBTTagCompound blueprintTag = data.getCompoundTag("blueprint_slot");
        if (blueprintTag != null) {
            ItemStack blueprintStack = new ItemStack(blueprintTag);
            this.rocketBlueprintSlot.setStackInSlot(0, blueprintStack);

            if (SuSyMetaItems.isMetaItem(blueprintStack) == SuSyMetaItems.DATA_CARD_MASTER_BLUEPRINT.metaValue) {
                if (blueprintStack.hasTagCompound()) {
                    AbstractRocketBlueprint bp = AbstractRocketBlueprint
                            .getCopyOf(blueprintStack.getTagCompound().getString("name"));
                    if (bp.readFromNBT(blueprintStack.getTagCompound())) {
                        this.slots = generateSlotsFromBlueprint(bp, this);

                        NBTTagCompound guiSlotsTag = data.getCompoundTag("gui_slots");
                        if (guiSlotsTag != empty_tag) {
                            for (String stageKey : guiSlotsTag.getKeySet()) {
                                NBTTagCompound stageTag = guiSlotsTag.getCompoundTag(stageKey);
                                if (stageTag == empty_tag) {
                                    continue;
                                }
                                // Map<String, List<DataStorageLoader>> componentMap = new HashMap<>();
                                for (String componentKey : stageTag.getKeySet()) {
                                    NBTTagList component_cards = stageTag.getTagList(componentKey, NBT.TAG_COMPOUND);
                                    if (component_cards == empty_tag_list) {
                                        continue;
                                    }
                                    // List<DataStorageLoader> loaderList = new ArrayList<>(stickList.tagCount());
                                    for (int i = 0; i < component_cards.tagCount(); i++) {
                                        NBTTagCompound stackTag = component_cards.getCompoundTagAt(i);
                                        if (stackTag == empty_tag) {
                                            continue;
                                        }
                                        ItemStack stack = new ItemStack(stackTag);
                                        // sorry if this explodes
                                        this.slots.get(stageKey).get(componentKey).get(i).setStackInSlot(0, stack);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            this.rocketBlueprintSlot.setStackInSlot(0, ItemStack.EMPTY);
        }
    }

    @Override
    protected void addErrorText(List<ITextComponent> textList) {
        super.addErrorText(textList);
        if (isStructureFormed() && !coolantFilled) {
            textList.add(TextComponentUtil.translationWithColor(TextFormatting.RED,
                    "susy.multiblock.aerospace_flight_simulator.obstructed"));
            textList.add(TextComponentUtil.translationWithColor(TextFormatting.GRAY,
                    "susy.multiblock.aerospace_flight_simulator.obstructed.desc"));
        }
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.inputCoolant = new FluidTankList(true);

        this.coolantPositions = null; // Clear water fill data when the structure is invalidated
        this.coolantFilled = false;
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.inputCoolant = new FluidTankList(true, getAbilities(MultiblockAbility.IMPORT_FLUIDS));

        this.coolantPositions = context.getOrDefault(FLUID_BLOCKS_KEY, new ArrayList<>());
        this.coolantFilled = coolantPositions.isEmpty();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityBlueprintAssembler(metaTileEntityId);
    }

    @Override
    protected void updateFormedValid() {
        if (!coolantFilled && getOffsetTimer() % 5 == 0) {
            fillCoolant(this.coolantPositions, SusyMaterials.Perfluoro2Methyl3Pentanone.getFluid(), inputCoolant);
            if (this.coolantPositions.isEmpty()) {
                this.coolantFilled = true;
            }
        }
    }

    @Override
    public boolean isStructureObstructed() {
        return super.isStructureObstructed() || !coolantFilled;
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("IIIIIII", "EEEEEEE", "EEEEEEE", "EEEEEEE", "EEEEEEE")
                .aisle("IIIIIII", "EPFPFPE", "EPFPFPE", "EFFFFFE", "ETTTTTE")
                .aisle("IIIIIII", "EPFPFPE", "EPFPFPE", "EFFFFFE", "ETTTTTE")
                .aisle("IIIIIII", "EPFPFPE", "EPFPFPE", "EFFFFFE", "ETTTTTE")
                .aisle("IIISIII", "ETCTCTE", "ETCTCTE", "ETCTCTE", "EEEEEEE")
                .where('S', selfPredicate())
                .where(' ', air())
                .where('C', states(getCasingState()))
                .where('P', states(getComputerState()))
                .where('T', states(MetaBlocks.TRANSPARENT_CASING.getState(BlockGlassCasing.CasingType.TEMPERED_GLASS)))
                .where('B', states(SuSyBlocks.SERPENTINE.getState(BlockSerpentine.SerpentineType.BASIC)))
                .where('F', fluid(SusyMaterials.Perfluoro2Methyl3Pentanone.getFluid()))
                .where(
                        'I',
                        abilities(MultiblockAbility.IMPORT_FLUIDS)
                                .setMaxGlobalLimited(1)
                                .setMinGlobalLimited(1, 1)
                                .or(abilities(MultiblockAbility.EXPORT_FLUIDS)
                                        .setMaxGlobalLimited(1)
                                        .setMaxGlobalLimited(1, 1))
                                .or(abilities(MultiblockAbility.INPUT_ENERGY)
                                        .setMaxGlobalLimited(2)
                                        .setMinGlobalLimited(1, 1)
                                        .or(states(getCasingState()))
                                        .or(maintenancePredicate().setMaxGlobalLimited(1).setMinGlobalLimited(1, 1)))
                                .or(states(getCasingState())))
                .where(
                        'E',
                        abilities(MultiblockAbility.INPUT_ENERGY)
                                .setMaxGlobalLimited(2)
                                .setMinGlobalLimited(1, 1)
                                .or(states(getCasingState()))
                                .or(maintenancePredicate().setMaxGlobalLimited(1).setMinGlobalLimited(1, 1)))
                .build();
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.ASSEMBLER_OVERLAY;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return createGUITemplate(entityPlayer).build(this.getHolder(), entityPlayer);
    }

    @Override
    protected boolean shouldSerializeInventories() {
        return false; // this block needs its own implementation
    }

    private boolean slotsEmpty() {
        return slots.values().stream()
                .flatMap(m -> m.values().stream())
                .flatMap(List::stream)
                .allMatch(DataStorageLoader::isEmpty);
    }

    private ModularUI.Builder createGUITemplate(EntityPlayer entityPlayer) {
        int width = 300;
        int height = 280;

        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, width, height);
        // black display screen in the background
        builder.image(4, 4, width - 8, height - 8, GuiTextures.DISPLAY);

        builder.dynamicLabel(
                width / 2 - 20,
                height / 2 - 16,
                () -> rocketBlueprintSlot.isEmpty() ? I18n.format(this.getMetaName() + ".blueprint_request") : "",
                0x404040);
        builder.widget(
                new IndicatorImageWidget(width - 23, height - 23, 17, 17, GuiTextures.GREGTECH_LOGO_DARK)
                        .setWarningStatus(GuiTextures.GREGTECH_LOGO_BLINKING_YELLOW, this::addWarningText)
                        .setErrorStatus(GuiTextures.GREGTECH_LOGO_BLINKING_RED, this::addErrorText));

        builder.widget(
                new ClickButtonWidget(
                        width - 40,
                        height - 130,
                        40,
                        30,
                        new TextComponentTranslation("debug").getUnformattedComponentText(),
                        click -> {
                            this.setDefaultBlueprint(click);
                        }));

        builder.label(9, 9, getMetaFullName(), 0xFFFFFF);
        builder.bindPlayerInventory(entityPlayer.inventory, height - 80);
        // this is the thing that displays slots for components
        RocketStageDisplayWidget mainWindow = new RocketStageDisplayWidget(
                new Position(9, 20),
                new Size(width - 20, 28 * 4),
                (stage, name) -> {
                    if (!slots.containsKey(stage.getName())) {
                        throw new IllegalStateException("missing the key value for a stage");
                    }
                    if (!slots.get(stage.getName()).containsKey(name)) {
                        throw new IllegalStateException("missing the key value for a component");
                    }
                    return this.slots.get(stage.getName()).get(name);
                });
        mainWindow.insertionAction = this::onBlueprintInserted;
        mainWindow.removalAction = this::onBlueprintRemoved;
        mainWindow.setVisible(false);
        mainWindow.setActive(false);
        SlotWidgetMentallyStable blueprintContainer = new SlotWidgetMentallyStable(rocketBlueprintSlot, 0, width / 2,
                height / 2);

        ClickButtonWidget buildButton = new ClickButtonWidget(
                7,
                height - 140,
                35,
                25,
                new TextComponentTranslation(this.getMetaName() + ".build_button_label")
                        .getFormattedText(),
                (c) -> {
                    this.buildBlueprint(c, mainWindow);
                })
                        .setShouldClientCallback(true);
        builder.dynamicLabel(
                50,
                150,
                () -> {
                    if (!this.rocketBlueprintSlot.isEmpty()) {
                        return mainWindow.getStatusText();
                    } else {
                        return "";
                    }
                },
                0x808080);
        // doesnt get ran sometimes so you have to check in the main too
        rocketBlueprintSlot.setLocked(!slotsEmpty());

        // this ChangeListener thing is a mess, it gets called mostly correctly server side (although it
        // still gets called when you just click on the slot with nothing happenning), but on the client
        // it gets called on initialization, and every single time the server syncs anything in the ui
        // to the client
        // for that reason i've decided to stop trying to make this random number generator work
        // correctly on both sides and just used updateInfo :goog:?
        blueprintContainer.setChangeListener(
                () -> {
                    blueprintContainer.setSelfPosition(
                            rocketBlueprintSlot.isEmpty() ? new Position(width / 2, height / 2) :
                                    new Position(width - 40, height - 40));

                    // the part that actually matters should be server side
                    if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
                        if (this.rocketBlueprintSlot.isEmpty() ||
                                !this.rocketBlueprintSlot.getStackInSlot(0).hasTagCompound()) {
                            mainWindow.onBlueprintRemoved();

                        } else {
                            if (this.rocketBlueprintSlot.getStackInSlot(0).getMetadata() ==
                                    SuSyMetaItems.DATA_CARD_MASTER_BLUEPRINT.metaValue &&
                                    this.rocketBlueprintSlot.getStackInSlot(0).hasTagCompound()) {
                                mainWindow.onBlueprintInserted();
                            }
                        }
                    }
                });
        // initialize the thing in case the blueprint was already in the machine
        if (!this.rocketBlueprintSlot.isEmpty() && rocketBlueprintSlot.getStackInSlot(0).hasTagCompound()) {

            NBTTagCompound tag = rocketBlueprintSlot.getStackInSlot(0).getTagCompound();

            AbstractRocketBlueprint bp = AbstractRocketBlueprint.getCopyOf(tag.getString("name"));
            if (bp == null) {
                SusyLog.logger.error("invalid blueprint");
                return builder;
            }
            if (this.slots.isEmpty()) {
                mainWindow.onBlueprintInserted();
            }

            if (bp.readFromNBT(tag)) {
                mainWindow.generateFromBlueprint(bp);
                mainWindow.setActive(true);
                mainWindow.setVisible(true);
            } else {
                SusyLog.logger.error("invalid blueprint");
            }
        }

        builder.widget(mainWindow);
        builder.widget(blueprintContainer.setBackgroundTexture(GuiTextures.SLOT_DARK));
        builder.widget(buildButton);

        return builder;
    }
}
