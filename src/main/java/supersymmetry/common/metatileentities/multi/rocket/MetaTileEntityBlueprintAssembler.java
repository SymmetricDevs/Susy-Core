package supersymmetry.common.metatileentities.multi.rocket;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
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
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockGlassCasing;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.SusyLog;
import supersymmetry.api.blocks.VariantHorizontalRotatableBlock;
import supersymmetry.api.rocketry.components.AbstractComponent;
import supersymmetry.api.rocketry.rockets.AbstractRocketBlueprint;
import supersymmetry.api.rocketry.rockets.RocketStage;
import supersymmetry.api.rocketry.rockets.RocketStage.ComponentValidationResult;
import supersymmetry.api.util.DataStorageLoader;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.blocks.rocketry.BlockProcessorCluster;
import supersymmetry.common.item.SuSyMetaItems;
import supersymmetry.common.materials.SusyMaterials;
import supersymmetry.common.mui.widget.ConditionalWidget;
import supersymmetry.common.mui.widget.RocketStageDisplayWidget;
import supersymmetry.common.mui.widget.SlotWidgetMentallyStable;
import supersymmetry.common.rocketry.SusyRocketComponents;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static supercritical.api.pattern.SCPredicates.FLUID_BLOCKS_KEY;
import static supercritical.api.pattern.SCPredicates.fluid;

public class MetaTileEntityBlueprintAssembler extends MultiblockWithDisplayBase {

    private boolean coolantFilled;

    private List<BlockPos> coolantPositions;

    public IMultipleTankHandler inputCoolant;

    public Map<String, Map<String, List<DataStorageLoader>>> slots = new HashMap<>();

    private String lastErrorStage;
    private String lastErrorComponent;
    private RocketStage.ComponentValidationResult lastErrorResult;

    public DataStorageLoader rocketBlueprintSlot = new DataStorageLoader(
            this,
            (item) -> {
                if (SuSyMetaItems.isMetaItem(item) == SuSyMetaItems.DATA_CARD_MASTER_BLUEPRINT.metaValue) {
                    if (item.hasTagCompound()) {
                        return true;
                    } else {
                        NBTTagCompound tag = SusyRocketComponents.ROCKET_SOYUZ_BLUEPRINT_DEFAULT.writeToNBT();
                        item.setTagCompound(tag);
                        return true;
                    }
                }
                return false;
            });

    private boolean hadBlueprint = false;

    public MetaTileEntityBlueprintAssembler(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);

        this.inputCoolant = new FluidTankList(true);
    }

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

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    public IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(
                MetalCasingType.STEEL_SOLID); // replace with real values later pls
    }

    public IBlockState getComputerState() {
        return SuSyBlocks.PROCESSOR_CLUSTER.getState(BlockProcessorCluster.TierType.TIER_1);
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

    public AbstractRocketBlueprint getCurrentBlueprint() {
        if (rocketBlueprintSlot.isEmpty() || !rocketBlueprintSlot.getStackInSlot(0).hasTagCompound()) {
            return null;
        }
        NBTTagCompound tag = rocketBlueprintSlot.getStackInSlot(0).getTagCompound();
        AbstractRocketBlueprint bp = AbstractRocketBlueprint.getCopyOf(tag.getString("name"));
        if (bp != null && bp.readFromNBT(tag)) {
            return bp;
        }
        return null;
    }

    public List<DataStorageLoader> getSlotsFor(RocketStage stage, String componentType) {
        Map<String, List<DataStorageLoader>> stageSlots = slots.get(stage.getName());
        if (stageSlots == null) {
            return new ArrayList<>();
        }
        return stageSlots.getOrDefault(componentType, new ArrayList<>());
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        NBTTagCompound tag = super.writeToNBT(data);

        NBTTagCompound guiSlots = new NBTTagCompound();
        for (Entry<String, Map<String, List<DataStorageLoader>>> stageEntry : slots.entrySet()) {
            NBTTagCompound stage = new NBTTagCompound();
            for (Entry<String, List<DataStorageLoader>> componentEntry : stageEntry.getValue().entrySet()) {
                NBTTagList cards = new NBTTagList();
                for (DataStorageLoader componentCard : componentEntry.getValue()) {
                    ItemStack stack = componentCard.getStackInSlot(0);
                    if (!stack.isEmpty()) {
                        cards.appendTag(stack.writeToNBT(new NBTTagCompound()));
                    }
                }
                stage.setTag(componentEntry.getKey(), cards);
            }
            guiSlots.setTag(stageEntry.getKey(), stage);
        }
        tag.setTag("gui_slots", guiSlots);

        if (!rocketBlueprintSlot.isEmpty()) {
            tag.setTag(
                    "blueprint_slot", rocketBlueprintSlot.getStackInSlot(0).writeToNBT(new NBTTagCompound()));
        }

        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);

        try {
            NBTTagCompound blueprintTag = data.getCompoundTag("blueprint_slot");
            if (blueprintTag != null && blueprintTag.getSize() > 0) {
                ItemStack blueprintStack = new ItemStack(blueprintTag);
                this.rocketBlueprintSlot.setStackInSlot(0, blueprintStack);
            }

            if (hasBlueprint()) {
                AbstractRocketBlueprint bp = getCurrentBlueprint();
                if (bp != null) {
                    this.slots = generateSlotsFromBlueprint(bp, this);

                    NBTTagCompound guiSlotsTag = data.getCompoundTag("gui_slots");
                    if (guiSlotsTag != null && guiSlotsTag.getSize() > 0) {
                        for (String stageKey : guiSlotsTag.getKeySet()) {
                            if (!slots.containsKey(stageKey)) continue;
                            NBTTagCompound stageTag = guiSlotsTag.getCompoundTag(stageKey);
                            if (stageTag == null || stageTag.getSize() == 0) continue;

                            for (String componentKey : stageTag.getKeySet()) {
                                if (!slots.get(stageKey).containsKey(componentKey)) continue;
                                NBTTagList componentCards = stageTag.getTagList(componentKey, NBT.TAG_COMPOUND);
                                if (componentCards == null) continue;

                                List<DataStorageLoader> slotList = slots.get(stageKey).get(componentKey);
                                for (int i = 0; i < componentCards.tagCount() && i < slotList.size(); i++) {
                                    NBTTagCompound stackTag = componentCards.getCompoundTagAt(i);
                                    if (stackTag != null && stackTag.getSize() > 0) {
                                        ItemStack stack = new ItemStack(stackTag);
                                        slotList.get(i).setStackInSlot(0, stack);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            SusyLog.logger.error(e);
            this.slots.clear();
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);

        if (hasBlueprint()) {
            buf.writeBoolean(true);
            buf.writeItemStack(rocketBlueprintSlot.getStackInSlot(0));

            AbstractRocketBlueprint bp = getCurrentBlueprint();
            if (bp != null) {
                NBTTagCompound bpTag = bp.writeToNBT();
                buf.writeCompoundTag(bpTag);
            }

            buf.writeVarInt(slots.size());
            for (Entry<String, Map<String, List<DataStorageLoader>>> stageEntry : slots.entrySet()) {
                buf.writeString(stageEntry.getKey());
                buf.writeVarInt(stageEntry.getValue().size());
                for (Entry<String, List<DataStorageLoader>> componentEntry : stageEntry.getValue().entrySet()) {
                    buf.writeString(componentEntry.getKey());
                    buf.writeVarInt(componentEntry.getValue().size());
                    for (DataStorageLoader slot : componentEntry.getValue()) {
                        buf.writeItemStack(slot.getStackInSlot(0));
                    }
                }
            }
        } else {
            buf.writeBoolean(false);
        }
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);

        try {
            boolean hasBp = buf.readBoolean();
            if (hasBp) {
                ItemStack blueprintStack = buf.readItemStack();
                this.rocketBlueprintSlot.setStackInSlot(0, blueprintStack);

                NBTTagCompound bpTag = buf.readCompoundTag();
                AbstractRocketBlueprint bp = AbstractRocketBlueprint.getCopyOf(bpTag.getString("name"));
                if (bp != null && bp.readFromNBT(bpTag)) {
                    this.slots = generateSlotsFromBlueprint(bp, this);

                    int numStages = buf.readVarInt();
                    for (int i = 0; i < numStages; i++) {
                        String stageKey = buf.readString(Short.MAX_VALUE);
                        if (!slots.containsKey(stageKey)) continue;
                        int numComponents = buf.readVarInt();
                        for (int j = 0; j < numComponents; j++) {
                            String componentKey = buf.readString(Short.MAX_VALUE);
                            if (!slots.get(stageKey).containsKey(componentKey)) continue;
                            int numSlots = buf.readVarInt();
                            List<DataStorageLoader> slotList = slots.get(stageKey).get(componentKey);
                            for (int k = 0; k < numSlots && k < slotList.size(); k++) {
                                ItemStack stack = buf.readItemStack();
                                if (!stack.isEmpty()) {
                                    slotList.get(k).setStackInSlot(0, stack);
                                }
                            }
                        }
                    }
                }
            } else {
                this.slots.clear();
            }
        } catch (Exception e) {
            SusyLog.logger.error(e);
            this.slots.clear();
        }
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        if (dataId == GregtechDataCodes.LOCK_OBJECT_HOLDER) {
            rocketBlueprintSlot.setLocked(buf.readBoolean());
        }
        super.receiveCustomData(dataId, buf);
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.inputCoolant = new FluidTankList(true);

        this.coolantPositions = null; // Clear water fill data when the structure is invalidated
        this.coolantFilled = false;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityBlueprintAssembler(metaTileEntityId);
    }

    @Override
    public boolean isStructureObstructed() {
        return super.isStructureObstructed() || !coolantFilled;
    }

    public boolean buildBlueprint(AbstractRocketBlueprint bp, RocketStageDisplayWidget dw) {
        lastErrorStage = null;
        lastErrorComponent = null;
        lastErrorResult = null;
        for (var aisle : dw.stageContainers.entrySet()) {
            String stageName = aisle.getKey();
            lastErrorStage = stageName;
            Optional<RocketStage> st = bp.getStages().stream().filter(x -> x.getName().equals(stageName)).findFirst();
            if (!st.isPresent()) {
                lastErrorResult = RocketStage.ComponentValidationResult.UNKNOWN;
                lastErrorComponent = "";
                return false;
            }
            RocketStage stage = st.get();
            var rows = aisle.getValue().components;
            for (var row : rows.entrySet()) {
                var componentType = row.getKey();
                var entry = row.getValue();
                lastErrorComponent = componentType;
                List<AbstractComponent<?>> rowCandidate;
                if (entry.shortView) {
                    var c = entry.selector.getSelectedValue();

                    var firstnbt = ((SlotWidget) row.getValue().itemList.widgets.get(0)).getHandle().getStack()
                            .getTagCompound();
                    if (firstnbt == null) {
                        lastErrorResult = RocketStage.ComponentValidationResult.INVALID_CARD;
                        return false;
                    }
                    var template = AbstractComponent.getComponentFromName(firstnbt.getString("name"))
                            .readFromNBT(firstnbt);
                    if (!template.isPresent()) {
                        lastErrorResult = RocketStage.ComponentValidationResult.INVALID_CARD;
                        return false;
                    }
                    var t = template.get();
                    rowCandidate = Stream.generate(() -> t).limit(c).collect(Collectors.toList());

                } else {
                    List<AbstractComponent<?>> list = row.getValue().itemList.widgets.stream()
                            .map((x) -> ((SlotWidget) x).getHandle().getStack()).filter(x -> x.hasTagCompound())
                            .map(x -> x.getTagCompound()).filter(x -> x.hasKey("name"))
                            .map(x -> AbstractComponent.getComponentFromName(x.getString("name")).readFromNBT(x))
                            .filter(x -> x.isPresent()).map(x -> x.get()).collect(Collectors.toList());
                    rowCandidate = list;

                }
                ComponentValidationResult res = stage.setComponentListEntry(componentType, rowCandidate);
                if (res != RocketStage.ComponentValidationResult.SUCCESS) {
                    lastErrorResult = res;
                    return false;
                }
            }
        }
        lastErrorResult = RocketStage.ComponentValidationResult.SUCCESS;
        return true;
    }

    public String getLastErrorMessage() {
        if (lastErrorResult == null) {
            return "";
        }
        String message = I18n.format(
                this.getMetaName() + ".build_error." + lastErrorResult.getName(),
                lastErrorStage == null ? "" : I18n.format("susy.rocketry.stages." + lastErrorStage + ".name"),
                lastErrorComponent == null ? "" :
                        I18n.format("susy.rocketry.components." + lastErrorComponent + ".name"));
        if (message.length() <= 30) {
            return message;
        }
        StringBuilder wrapped = new StringBuilder();
        String[] words = message.split(" ");
        int lineLength = 0;
        for (String word : words) {
            if (lineLength + word.length() + 1 > 30 && lineLength > 0) {
                wrapped.append("\n");
                lineLength = 0;
            }
            if (lineLength > 0) {
                wrapped.append(" ");
                lineLength++;
            }
            wrapped.append(word);
            lineLength += word.length();
        }
        return wrapped.toString();
    }

    @Override
    protected void addErrorText(List<ITextComponent> textList) {
        super.addErrorText(textList);
        if (isStructureFormed() && !coolantFilled) {
            ITextComponent msg = new TextComponentString("Coolant Not Filled");
            msg.getStyle().setColor(TextFormatting.RED);
            textList.add(msg);
        }
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.inputCoolant = new FluidTankList(true, getAbilities(MultiblockAbility.IMPORT_FLUIDS));

        this.coolantPositions = context.getOrDefault(FLUID_BLOCKS_KEY, new ArrayList<>());
        this.coolantFilled = coolantPositions.isEmpty();
    }

    @Override
    protected void updateFormedValid() {
        if (!coolantFilled && getOffsetTimer() % 5 == 0) {
            fillCoolant(
                    this.coolantPositions, SusyMaterials.Perfluoro2Methyl3Pentanone.getFluid(), inputCoolant);
            if (this.coolantPositions.isEmpty()) {
                this.coolantFilled = true;
            }
        }
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("IIIIIII", "IIIIIII", "IIIIIII", "IIIIIII", "IIIIIII")
                .aisle("IIIIIII", "IPFPFPI", "IPFPFPI", "IFFFFFI", "ITTTTTI")
                .aisle("IIIIIII", "IPFPFPI", "IPFPFPI", "IFFFFFI", "ITTTTTI")
                .aisle("IIIIIII", "IPFPFPI", "IPFPFPI", "IFFFFFI", "ITTTTTI")
                .aisle("IIISIII", "ITCTCTI", "ITCTCTI", "ITCTCTI", "IIIIIII")
                .where('S', selfPredicate())
                .where(' ', air())
                .where('C', states(getCasingState()))
                .where(
                        'P',
                        states(
                                getComputerState()
                                        .withProperty(VariantHorizontalRotatableBlock.FACING, EnumFacing.SOUTH)))
                .where(
                        'T',
                        states(
                                MetaBlocks.TRANSPARENT_CASING.getState(BlockGlassCasing.CasingType.TEMPERED_GLASS)))
                .where('F', fluid(SusyMaterials.Perfluoro2Methyl3Pentanone.getFluid()))
                .where(
                        'I',
                        abilities(MultiblockAbility.IMPORT_FLUIDS)
                                .setMaxGlobalLimited(1)
                                .setMinGlobalLimited(1, 1)
                                .or(
                                        abilities(MultiblockAbility.EXPORT_FLUIDS)
                                                .setMaxGlobalLimited(1)
                                                .setMaxGlobalLimited(1, 1))
                                .or(
                                        abilities(MultiblockAbility.INPUT_ENERGY)
                                                .setMaxGlobalLimited(2)
                                                .setMinGlobalLimited(1, 1)
                                                .or(states(getCasingState()))
                                                .or(
                                                        maintenancePredicate()
                                                                .setMaxGlobalLimited(1)
                                                                .setMinGlobalLimited(1, 1)))
                                .or(states(getCasingState())))
                .build();
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return SusyTextures.BLUEPRINT_ASSEMBLER_OVERLAY;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return createGUITemplate(entityPlayer).build(this.getHolder(), entityPlayer);
    }

    @Override
    protected boolean shouldSerializeInventories() {
        return false; // this block needs its own implementation
    }

    private Map<String, Map<String, List<DataStorageLoader>>> generateSlotsFromBlueprint(
                                                                                         AbstractRocketBlueprint bp,
                                                                                         MetaTileEntity mte) {
        Map<String, Map<String, List<DataStorageLoader>>> map = new HashMap<>();
        for (RocketStage stage : bp.getStages()) {
            Map<String, List<DataStorageLoader>> stageComponents = new HashMap<>();
            for (String componentType : stage.getComponentLimits().keySet()) {
                List<DataStorageLoader> slots = new ArrayList<>();
                int maxSlots = stage.maxComponentsOf(componentType);
                for (int i = 0; i < maxSlots; i++) {
                    slots.add(
                            new DataStorageLoader(
                                    mte,
                                    item -> {
                                        if (SuSyMetaItems.isMetaItem(item) ==
                                                SuSyMetaItems.DATA_CARD_ACTIVE.metaValue) {
                                            if (item.hasTagCompound()) {
                                                NBTTagCompound tag = item.getTagCompound();
                                                if (tag.hasKey("name")) {
                                                    var c = AbstractComponent
                                                            .getComponentFromName(tag.getString("name"));
                                                    if (c == null) return false;
                                                    if (c.getComponentSlotValidator().test(componentType)) {
                                                        return true;
                                                    }
                                                }
                                            }
                                        }
                                        return false;
                                    }));
                }
                stageComponents.put(componentType, slots);
            }
            map.put(stage.getName(), stageComponents);
        }
        return map;
    }

    private boolean slotsEmpty() {
        return slots.values().stream()
                .flatMap(m -> m.values().stream())
                .flatMap(List::stream)
                .allMatch(DataStorageLoader::isEmpty);
    }

    private boolean hasBlueprint() {
        if (rocketBlueprintSlot.isEmpty()) {
            return false;
        }
        ItemStack stack = rocketBlueprintSlot.getStackInSlot(0);
        return stack != null && (stack.getMetadata() == SuSyMetaItems.DATA_CARD_MASTER_BLUEPRINT.metaValue) &&
                !stack.isEmpty() && stack.getItem() != null && stack.hasTagCompound();
    }

    private void onBlueprintSlotChanged(RocketStageDisplayWidget rocketStageWidget) {
        if (rocketStageWidget == null) {
            return;
        }

        boolean hasbp = hasBlueprint();

        if (hasbp != hadBlueprint) {
            hadBlueprint = hasbp;

            if (hasbp) {

                initializeBlueprintSlots();
                rocketStageWidget.rebuildContainers();
                rocketStageWidget.setVisible(true);
                rocketStageWidget.setActive(true);
            } else {
                slots.clear();
                rocketStageWidget.rebuildContainers();
                rocketStageWidget.setVisible(false);
                rocketStageWidget.setActive(false);
            }
        }
    }

    private ModularUI.Builder createGUITemplate(EntityPlayer entityPlayer) {
        int width = 300;
        int height = 280;

        hadBlueprint = hasBlueprint();
        if (hasBlueprint() && slots.isEmpty()) {
            initializeBlueprintSlots();
        }

        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, width, height);
        builder.image(4, 4, width - 8, height - 8, GuiTextures.DISPLAY);

        ConditionalWidget conditional = new ConditionalWidget(0, 0, width, height, () -> !hasBlueprint());
        conditional.addWidget(
                new LabelWidget(
                        width / 2 - 40,
                        height / 2 - 29,
                        I18n.format(this.getMetaName() + ".blueprint_request"),
                        0x505050));
        builder.widget(conditional);

        builder.widget(
                new IndicatorImageWidget(width - 23, height - 23, 17, 17, GuiTextures.GREGTECH_LOGO_DARK)
                        .setWarningStatus(GuiTextures.GREGTECH_LOGO_BLINKING_YELLOW, this::addWarningText)
                        .setErrorStatus(GuiTextures.GREGTECH_LOGO_BLINKING_RED, this::addErrorText));
        RocketStageDisplayWidget mainw = new RocketStageDisplayWidget(
                new Position(9, 20),
                new Size(width - 20, 28 * 4),
                this::getCurrentBlueprint,
                this::getSlotsFor);

        if (hasBlueprint()) {
            mainw.buildContainers();
            mainw.setVisible(true);
            mainw.setActive(true);
        } else {
            mainw.setVisible(false);
            mainw.setActive(false);
        }

        builder.label(9, 9, getMetaFullName(), 0xFFFFFF);

        builder.bindPlayerInventory(entityPlayer.inventory, height - 80);

        SlotWidgetMentallyStable blueprintSlot = new SlotWidgetMentallyStable(
                rocketBlueprintSlot, 0, width / 2 - 9, height / 2, true, true)
                        .setBackgroundTexture(GuiTextures.SLOT_DARK);
        rocketBlueprintSlot.setLocked(!slotsEmpty());

        blueprintSlot.setChangeListener(
                () -> {
                    this.onBlueprintSlotChanged(mainw);
                    if (hasBlueprint()) {
                        blueprintSlot.setSelfPosition(new Position(width - 48, height - 28));
                    } else {
                        blueprintSlot.setSelfPosition(new Position(width / 2 - 9, height / 2 - 18));
                    }
                });

        builder.widget(mainw);
        builder.widget(blueprintSlot);

        conditional.addWidgetWithTest(
                new ClickButtonWidget(
                        7,
                        height - 115,
                        35,
                        25,
                        I18n.format(this.getMetaName() + ".build_button"),
                        click -> {
                            AbstractRocketBlueprint bp = getCurrentBlueprint();
                            if (bp != null) {
                                boolean success = buildBlueprint(bp, mainw);
                                if (success) {
                                    if (!rocketBlueprintSlot.isEmpty()) {
                                        NBTTagCompound tag = bp.writeToNBT();
                                        rocketBlueprintSlot.setNBT(nbt -> tag);
                                    }
                                }
                            }
                        })
                                .setShouldClientCallback(true),
                () -> this.hasBlueprint());

        conditional.addWidgetWithTest(new DynamicLabelWidget(50, height - 130, this::getLastErrorMessage, 0xFF5555),
                () -> this.lastErrorResult != RocketStage.ComponentValidationResult.SUCCESS && hasBlueprint());
        conditional.addWidgetWithTest(
                new LabelWidget(50, height - 130, this.getMetaName() + ".build_error.success", 0x55FF55),
                () -> this.lastErrorResult == RocketStage.ComponentValidationResult.SUCCESS && hasBlueprint());
        conditional.addWidgetWithTest(
                new LabelWidget(50, height - 118, this.getMetaName() + ".build_error.success.extract", 0x55FF55),
                () -> this.lastErrorResult == RocketStage.ComponentValidationResult.SUCCESS && hasBlueprint());

        return builder;
    }

    private void initializeBlueprintSlots() {
        AbstractRocketBlueprint bp = getCurrentBlueprint();
        if (bp != null) {
            slots = generateSlotsFromBlueprint(bp, this);
        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(),
                this.isStructureFormed(), true);
    }

}
