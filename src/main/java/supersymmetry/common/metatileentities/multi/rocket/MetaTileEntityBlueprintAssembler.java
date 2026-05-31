package supersymmetry.common.metatileentities.multi.rocket;

import static supercritical.api.pattern.SCPredicates.FLUID_BLOCKS_KEY;
import static supercritical.api.pattern.SCPredicates.fluid;

import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.impl.ModularUIContainer;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityUIFactory;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.util.GTUtility;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockGlassCasing;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.MetaBlocks;
import supersymmetry.api.SusyLog;
import supersymmetry.api.blocks.VariantHorizontalRotatableBlock;
import supersymmetry.api.capability.SuSyDataCodes;
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

public class MetaTileEntityBlueprintAssembler extends MultiblockWithDisplayBase {

    private boolean coolantFilled;

    private List<BlockPos> coolantPositions;

    public IMultipleTankHandler inputCoolant;
    public IMultipleTankHandler outputCoolant;
    private IEnergyContainer energyContainer;

    public Map<String, Map<String, BlueprintRowState>> stageRows = new TreeMap<>();

    private String lastErrorStage;
    private String lastErrorComponent;
    private RocketStage.ComponentValidationResult lastErrorResult;

    private int buildProgress = 0;
    private int buildDuration = 1200;
    private boolean buildInProgress = false;
    private boolean blueprintBuilt = false;
    private boolean hasNotEnoughEnergy = false;
    private boolean buildHasNotEnoughCoolant = false;

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

    // Set on the server when a blueprint is inserted into an empty assembler. Adding the component
    // slots to an already-open container goes through ModularUIContainer#notifyWidgetChange, which
    // assigns slot numbers in non-deterministic HashSet order, desyncing client/server slot IDs.
    // We instead re-open the UI on the next tick so registration takes the deterministic constructor
    // path. See ModularUIContainer#notifyWidgetChange for the underlying warning.
    private boolean needsUIReopen = false;

    public MetaTileEntityBlueprintAssembler(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);

        this.inputCoolant = new FluidTankList(true);
        this.energyContainer = new EnergyContainerList(new ArrayList<>());
    }

    public void fillCoolant(List<BlockPos> toFill, Fluid fluid, IMultipleTankHandler fluidInputs) {
        if (fluidInputs != null) {
            FluidStack toDrain = new FluidStack(fluid, 1000);
            FluidStack drained = fluidInputs.drain(toDrain, false);
            if (drained != null && drained.amount != 0) {
                if (drained.amount == 1000) {
                    World world = this.getWorld();
                    BlockPos pos = toFill.get(0);
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
            this.rocketBlueprintSlot.setLocked(!this.slotsEmpty() && !blueprintBuilt);
            if (needsUIReopen) {
                needsUIReopen = false;
                reopenUIForViewers();
            }
        }
    }

    /**
     * Re-opens this machine's UI for every player currently viewing it. Used after a blueprint is
     * inserted so that the dynamically-created component slots are registered through the
     * deterministic {@link ModularUIContainer} constructor path instead of the non-deterministic
     * {@code notifyWidgetChange} path, keeping client/server slot IDs in sync.
     */
    private void reopenUIForViewers() {
        if (getWorld() == null || getWorld().isRemote) {
            return;
        }
        List<EntityPlayerMP> viewers = new ArrayList<>();
        for (EntityPlayer player : getWorld().playerEntities) {
            if (player instanceof EntityPlayerMP &&
                    player.openContainer instanceof ModularUIContainer &&
                    ((ModularUIContainer) player.openContainer).getModularUI().holder == getHolder()) {
                viewers.add((EntityPlayerMP) player);
            }
        }
        for (EntityPlayerMP player : viewers) {
            // openUI calls closeContainer(), which drops whatever is on the cursor. Preserve it so
            // re-opening never eats a held item (e.g. when the blueprint was just placed from a stack).
            ItemStack cursor = player.inventory.getItemStack();
            player.inventory.setItemStack(ItemStack.EMPTY);
            MetaTileEntityUIFactory.INSTANCE.openUI(getHolder(), player);
            if (!cursor.isEmpty()) {
                player.inventory.setItemStack(cursor);
                player.updateHeldItem();
            }
        }
    }

    public AbstractRocketBlueprint getCurrentBlueprint() {
        if (rocketBlueprintSlot.isEmpty() || !rocketBlueprintSlot.getStackInSlot(0).hasTagCompound()) {
            return null;
        }
        NBTTagCompound tag = rocketBlueprintSlot.getStackInSlot(0).getTagCompound();
        AbstractRocketBlueprint bp = AbstractRocketBlueprint.getCopyOf(tag.getString("name"));
        if (bp != null && bp.readFromNBT(tag)) {
            bp.setStages(bp.getStages().stream().map(s -> (RocketStage) s.clone()).collect(Collectors.toList()));
            return bp;
        }
        return null;
    }

    public BlueprintRowState getRowState(RocketStage stage, String componentType) {
        Map<String, BlueprintRowState> stageSlots = stageRows.get(stage.getName());
        if (stageSlots == null) return null;
        return stageSlots.get(componentType);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        NBTTagCompound tag = super.writeToNBT(data);

        if (!rocketBlueprintSlot.isEmpty()) {
            tag.setTag(
                    "blueprint_slot", rocketBlueprintSlot.getStackInSlot(0).writeToNBT(new NBTTagCompound()));
        }

        tag.setTag("row_states", writeRowStatesToNBT());
        tag.setBoolean("buildInProgress", buildInProgress);
        tag.setInteger("buildProgress", buildProgress);
        tag.setInteger("buildDuration", buildDuration);
        tag.setBoolean("blueprintBuilt", blueprintBuilt);

        return tag;
    }

    private NBTTagCompound writeRowStatesToNBT() {
        NBTTagCompound root = new NBTTagCompound();
        for (Map.Entry<String, Map<String, BlueprintRowState>> stageEntry : stageRows.entrySet()) {
            NBTTagCompound stageTag = new NBTTagCompound();
            for (Map.Entry<String, BlueprintRowState> rowEntry : stageEntry.getValue().entrySet()) {
                stageTag.setTag(rowEntry.getKey(), rowEntry.getValue().writeStateToNBT());
            }
            root.setTag(stageEntry.getKey(), stageTag);
        }
        return root;
    }

    private void applyRowStatesFromNBT(NBTTagCompound root) {
        if (root == null) return;
        for (String stageName : root.getKeySet()) {
            Map<String, BlueprintRowState> stageMap = stageRows.get(stageName);
            if (stageMap == null) continue;
            NBTTagCompound stageTag = root.getCompoundTag(stageName);
            for (String componentType : stageTag.getKeySet()) {
                BlueprintRowState row = stageMap.get(componentType);
                if (row == null) continue;
                row.readStateFromNBT(stageTag.getCompoundTag(componentType));
            }
        }
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
                    this.stageRows = generateRowsFromBlueprint(bp, this);
                    applyRowStatesFromNBT(data.getCompoundTag("row_states"));
                }
            }

            buildInProgress = data.getBoolean("buildInProgress");
            buildProgress = data.getInteger("buildProgress");
            buildDuration = data.hasKey("buildDuration") ? data.getInteger("buildDuration") : 1200;
            blueprintBuilt = data.getBoolean("blueprintBuilt");
        } catch (Exception e) {
            SusyLog.logger.error(e);
            this.stageRows.clear();
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);

        if (hasBlueprint()) {
            buf.writeBoolean(true);
            buf.writeItemStack(rocketBlueprintSlot.getStackInSlot(0));
            AbstractRocketBlueprint bp = getCurrentBlueprint();
            buf.writeBoolean(bp != null);
            if (bp != null) {
                buf.writeCompoundTag(bp.writeToNBT());
                try {
                    buf.writeCompoundTag(writeRowStatesToNBT());
                } catch (Exception e) {
                    buf.writeCompoundTag(new NBTTagCompound());
                }
            }
        } else {
            buf.writeBoolean(false);
        }
        buf.writeBoolean(buildInProgress);
        buf.writeInt(buildProgress);
        buf.writeInt(buildDuration);
        buf.writeBoolean(blueprintBuilt);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);

        try {
            boolean hasBp = buf.readBoolean();
            if (hasBp) {
                ItemStack blueprintStack = buf.readItemStack();
                this.rocketBlueprintSlot.setStackInSlot(0, blueprintStack);
                boolean hasBpData = buf.readBoolean();
                if (hasBpData) {
                    NBTTagCompound bpTag = buf.readCompoundTag();
                    AbstractRocketBlueprint bp = AbstractRocketBlueprint.getCopyOf(bpTag.getString("name"));
                    if (bp != null && bp.readFromNBT(bpTag)) {
                        this.stageRows = generateRowsFromBlueprint(bp, this);
                    }
                    NBTTagCompound rowStatesTag = buf.readCompoundTag();
                    applyRowStatesFromNBT(rowStatesTag);
                }
            } else {
                this.stageRows.clear();
            }
            buildInProgress = buf.readBoolean();
            buildProgress = buf.readInt();
            buildDuration = buf.readInt();
            blueprintBuilt = buf.readBoolean();
        } catch (Exception e) {
            SusyLog.logger.error(e);
            this.stageRows.clear();
        }
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        if (dataId == GregtechDataCodes.LOCK_OBJECT_HOLDER) {
            rocketBlueprintSlot.setLocked(buf.readBoolean());
        } else if (dataId == SuSyDataCodes.BLUEPRINT_BUILD_RESULT) {
            String resultName = buf.readString(Short.MAX_VALUE);
            lastErrorStage = buf.readString(Short.MAX_VALUE);
            lastErrorComponent = buf.readString(Short.MAX_VALUE);
            lastErrorResult = resultName.isEmpty() ? null : RocketStage.ComponentValidationResult.valueOf(resultName);
        } else if (dataId == SuSyDataCodes.BLUEPRINT_BUILD_STATE) {
            buildInProgress = buf.readBoolean();
            buildProgress = buf.readInt();
            buildDuration = buf.readInt();
            blueprintBuilt = buf.readBoolean();
            hasNotEnoughEnergy = buf.readBoolean();
            buildHasNotEnoughCoolant = buf.readBoolean();
        }
        super.receiveCustomData(dataId, buf);
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.inputCoolant = new FluidTankList(true);
        this.energyContainer = new EnergyContainerList(new ArrayList<>());

        this.coolantPositions = null;
        this.coolantFilled = false;
        this.buildInProgress = false;
        this.buildProgress = 0;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityBlueprintAssembler(metaTileEntityId);
    }

    @Override
    public boolean isStructureObstructed() {
        return super.isStructureObstructed() || !coolantFilled;
    }

    public boolean buildBlueprint(AbstractRocketBlueprint bp) {
        try {
            lastErrorStage = null;
            lastErrorComponent = null;
            lastErrorResult = null;
            for (var stageEntry : stageRows.entrySet()) {
                String stageName = stageEntry.getKey();
                lastErrorStage = stageName;
                Optional<RocketStage> st = bp.getStages().stream().filter(x -> x.getName().equals(stageName))
                        .findFirst();
                if (!st.isPresent()) {
                    lastErrorResult = RocketStage.ComponentValidationResult.UNKNOWN;
                    lastErrorComponent = "";
                    return false;
                }
                RocketStage stage = st.get();
                for (var rowEntry : stageEntry.getValue().entrySet()) {
                    String componentType = rowEntry.getKey();
                    BlueprintRowState rowState = rowEntry.getValue();
                    lastErrorComponent = componentType;
                    SusyLog.logger.info("stage: {}, row type:{} [shortView:{},multiplier:{}]", stageName,
                            componentType, rowState.shortView, rowState.getMultiplier());
                    List<AbstractComponent<?>> rowCandidate = rowState.materializeComponents();
                    if (rowCandidate == null) {
                        lastErrorResult = RocketStage.ComponentValidationResult.INVALID_CARD;
                        return false;
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
        } catch (Exception e) {
            SusyLog.logger.error("Error in buildBlueprint", e);
            return false;
        }
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
        if (buildInProgress && hasNotEnoughEnergy) {
            ITextComponent msg = new TextComponentString(
                    I18n.format(this.getMetaName() + ".no_energy_build"));
            msg.getStyle().setColor(TextFormatting.YELLOW);
            textList.add(msg);
        }
        if (buildInProgress && buildHasNotEnoughCoolant) {
            ITextComponent msg = new TextComponentString(
                    I18n.format(this.getMetaName() + ".no_coolant_build"));
            msg.getStyle().setColor(TextFormatting.YELLOW);
            textList.add(msg);
        }
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.inputCoolant = new FluidTankList(true, getAbilities(MultiblockAbility.IMPORT_FLUIDS));
        this.outputCoolant = new FluidTankList(true, getAbilities(MultiblockAbility.EXPORT_FLUIDS));

        this.energyContainer = new EnergyContainerList(getAbilities(MultiblockAbility.INPUT_ENERGY));

        this.coolantPositions = context.getOrDefault(FLUID_BLOCKS_KEY, new ArrayList<>());
        this.coolantFilled = coolantPositions.isEmpty();
    }

    @Override
    protected void updateFormedValid() {
        if (!coolantFilled && getOffsetTimer() % 5 == 0) {
            fillCoolant(this.coolantPositions, SusyMaterials.FC75.getFluid(), inputCoolant);
            if (this.coolantPositions.isEmpty()) {
                this.coolantFilled = true;
            }
        }

        if (!buildInProgress) return;

        long inputVoltage = energyContainer.getInputVoltage();
        if (inputVoltage < GTValues.V[GTValues.MV]) {
            hasNotEnoughEnergy = true;
            if (getOffsetTimer() % 20 == 0) syncBuildState();
            return;
        }

        int tier = Math.max(GTValues.MV, GTUtility.getTierByVoltage(inputVoltage));
        buildDuration = Math.max(1, 1200 >> (tier - GTValues.MV));
        long energyPerTick = GTValues.V[tier];

        if (hasNotEnoughEnergy && energyContainer.getInputPerSec() > 19L * energyPerTick) {
            hasNotEnoughEnergy = false;
        }
        if (energyContainer.getEnergyStored() < energyPerTick || hasNotEnoughEnergy) {
            hasNotEnoughEnergy = true;
            if (getOffsetTimer() % 20 == 0) syncBuildState();
            return;
        }

        long consumed = energyContainer.removeEnergy(energyPerTick);
        if (consumed != -energyPerTick) {
            hasNotEnoughEnergy = true;
            if (getOffsetTimer() % 20 == 0) syncBuildState();
            return;
        }
        hasNotEnoughEnergy = false;

        // Coolant scales directly with power: 1 mB/t at MV, 4 at HV, 16 at EV, etc.
        int coolantPerTick = (int) (GTValues.V[tier] / GTValues.V[GTValues.MV]);

        FluidStack coolantCheck = inputCoolant.drain(
                new FluidStack(SusyMaterials.FC75.getFluid(), coolantPerTick), false);
        boolean enoughSpaceForCoolant = outputCoolant
                .fill(new FluidStack(SusyMaterials.WarmFC75.getFluid(), coolantPerTick), false) ==
                coolantPerTick;
        if (coolantCheck == null || coolantCheck.amount < coolantPerTick || !enoughSpaceForCoolant) {
            buildHasNotEnoughCoolant = true;
            if (getOffsetTimer() % 20 == 0) syncBuildState();
            return;
        }
        buildHasNotEnoughCoolant = false;
        inputCoolant.drain(new FluidStack(SusyMaterials.FC75.getFluid(), coolantPerTick), true);
        outputCoolant.fill(new FluidStack(SusyMaterials.WarmFC75.getFluid(), coolantPerTick), true);

        buildProgress++;
        if (buildProgress >= buildDuration) {
            completeBuild();
            return;
        }
        if (getOffsetTimer() % 20 == 0) syncBuildState();
    }

    private void completeBuild() {
        buildInProgress = false;
        AbstractRocketBlueprint bp = getCurrentBlueprint();
        if (bp != null) {
            boolean success = buildBlueprint(bp);
            if (success && !rocketBlueprintSlot.isEmpty()) {
                rocketBlueprintSlot.setNBT(nbt -> bp.writeToNBT());
                blueprintBuilt = true;
            }
        }
        buildProgress = 0;
        markDirty();
        writeCustomData(SuSyDataCodes.BLUEPRINT_BUILD_RESULT, buf -> {
            buf.writeString(lastErrorResult != null ? lastErrorResult.name() : "");
            buf.writeString(lastErrorStage != null ? lastErrorStage : "");
            buf.writeString(lastErrorComponent != null ? lastErrorComponent : "");
        });
        syncBuildState();
    }

    private void syncBuildState() {
        if (getWorld() != null && !getWorld().isRemote) {
            writeCustomData(SuSyDataCodes.BLUEPRINT_BUILD_STATE, buf -> {
                buf.writeBoolean(buildInProgress);
                buf.writeInt(buildProgress);
                buf.writeInt(buildDuration);
                buf.writeBoolean(blueprintBuilt);
                buf.writeBoolean(hasNotEnoughEnergy);
                buf.writeBoolean(buildHasNotEnoughCoolant);
            });
        }
    }

    private void autofillFromBlueprint(AbstractRocketBlueprint bp) {
        if (bp == null) return;
        for (RocketStage stage : bp.getStages()) {
            Map<String, BlueprintRowState> stageMap = stageRows.get(stage.getName());
            if (stageMap == null) continue;
            for (Map.Entry<String, List<AbstractComponent<?>>> entry : stage.getComponents().entrySet()) {
                String compType = entry.getKey();
                BlueprintRowState rowState = stageMap.get(compType);
                if (rowState == null) continue;
                List<AbstractComponent<?>> comps = entry.getValue();
                if (comps.isEmpty()) continue;
                rowState.shortView = false;
                rowState.multiplierIndex = 0;
                for (int i = 0; i < Math.min(comps.size(), rowState.slots.size()); i++) {
                    ItemStack card = SuSyMetaItems.DATA_CARD_ACTIVE.getStackForm();
                    NBTTagCompound tag = new NBTTagCompound();
                    comps.get(i).writeToNBT(tag);
                    card.setTagCompound(tag);
                    rowState.slots.get(i).setStackInSlot(0, card);
                }
            }
        }
        if (!this.getWorld().isRemote) {
            markDirty();
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
                .where('F', fluid(SusyMaterials.FC75.getFluid()))
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

    private Map<String, Map<String, BlueprintRowState>> generateRowsFromBlueprint(
                                                                                  AbstractRocketBlueprint bp,
                                                                                  MetaTileEntity mte) {
        Map<String, Map<String, BlueprintRowState>> map = new TreeMap<>();
        for (RocketStage stage : bp.getStages()) {
            Map<String, BlueprintRowState> stageComponents = new TreeMap<>();
            for (Map.Entry<String, int[]> entry : stage.getComponentLimits().entrySet()) {
                String componentType = entry.getKey();
                int[] validMultiplierValues = entry.getValue();
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
                stageComponents.put(componentType, new BlueprintRowState(slots, validMultiplierValues));
            }
            map.put(stage.getName(), stageComponents);
        }
        return map;
    }

    private boolean slotsEmpty() {
        return stageRows.values().stream()
                .flatMap(m -> m.values().stream())
                .flatMap(r -> r.slots.stream())
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
        if (rocketStageWidget == null) return;

        boolean hasbp = hasBlueprint();

        if (hasbp != hadBlueprint) {
            hadBlueprint = hasbp;

            if (hasbp) {
                initializeBlueprintSlots();
                autofillFromBlueprint(getCurrentBlueprint());
                rocketStageWidget.rebuildContainers();
                rocketStageWidget.setVisible(true);
                rocketStageWidget.setActive(true);
                if (getWorld() != null && !getWorld().isRemote) {
                    // Re-open next tick so the just-created component slots register deterministically.
                    needsUIReopen = true;
                }
            } else {
                stageRows.clear();
                if (!getWorld().isRemote) {
                    blueprintBuilt = false;
                    buildInProgress = false;
                    buildProgress = 0;
                    syncBuildState();
                }
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
        if (hasBlueprint() && stageRows.isEmpty()) {
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
                this::getRowState,
                this::markDirty);

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
                            if (!buildInProgress && hasBlueprint()) {
                                AbstractRocketBlueprint bp = getCurrentBlueprint();
                                if (bp != null) {
                                    boolean valid = buildBlueprint(bp);
                                    writeCustomData(SuSyDataCodes.BLUEPRINT_BUILD_RESULT, buf -> {
                                        buf.writeString(lastErrorResult != null ? lastErrorResult.name() : "");
                                        buf.writeString(lastErrorStage != null ? lastErrorStage : "");
                                        buf.writeString(lastErrorComponent != null ? lastErrorComponent : "");
                                    });
                                    if (valid) {
                                        buildInProgress = true;
                                        buildProgress = 0;
                                        blueprintBuilt = false;
                                        lastErrorResult = null;
                                        syncBuildState();
                                    }
                                }
                            }
                        }),
                () -> hasBlueprint() && !buildInProgress);

        conditional.addWidgetWithTest(
                new DynamicLabelWidget(
                        7, height - 115,
                        () -> I18n.format(
                                this.getMetaName() + ".compiling",
                                buildDuration > 0 ? (buildProgress * 100 / buildDuration) : 0),
                        0xFFAA00),
                () -> buildInProgress && hasBlueprint());

        conditional.addWidgetWithTest(
                new DynamicLabelWidget(50, height - 130, this::getLastErrorMessage, 0xFF5555),
                () -> this.lastErrorResult != null &&
                        this.lastErrorResult != RocketStage.ComponentValidationResult.SUCCESS &&
                        hasBlueprint() && !buildInProgress);
        conditional.addWidgetWithTest(
                new LabelWidget(50, height - 130, this.getMetaName() + ".build_error.success", 0x55FF55),
                () -> blueprintBuilt && this.lastErrorResult == RocketStage.ComponentValidationResult.SUCCESS &&
                        hasBlueprint());
        conditional.addWidgetWithTest(
                new LabelWidget(50, height - 118, this.getMetaName() + ".build_error.success.extract", 0x55FF55),
                () -> blueprintBuilt && this.lastErrorResult == RocketStage.ComponentValidationResult.SUCCESS &&
                        hasBlueprint());

        return builder;
    }

    private void initializeBlueprintSlots() {
        AbstractRocketBlueprint bp = getCurrentBlueprint();
        if (bp != null) {
            stageRows = generateRowsFromBlueprint(bp, this);
        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(),
                this.isStructureFormed(), true);
    }
}
