package supersymmetry.common.metatileentities.multi.rocket;

import static supercritical.api.pattern.SCPredicates.FLUID_BLOCKS_KEY;
import static supercritical.api.pattern.SCPredicates.fluid;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.capability.*;
import gregtech.api.capability.impl.EnergyContainerList;
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
import gregtech.api.util.TextComponentUtil;
import gregtech.api.util.world.DummyWorld;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockGlassCasing;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.MetaBlocks;
import supersymmetry.api.SusyLog;
import supersymmetry.api.blocks.VariantHorizontalRotatableBlock;
import supersymmetry.api.gui.SusyGuiTextures;
import supersymmetry.api.rocketry.fuels.RocketFuelEntry;
import supersymmetry.api.rocketry.rockets.AbstractRocketBlueprint;
import supersymmetry.api.rocketry.rockets.IAFSimprovable;
import supersymmetry.api.rocketry.rockets.RocketStage;
import supersymmetry.api.util.DataStorageLoader;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.blocks.rocketry.BlockProcessorCluster;
import supersymmetry.common.item.SuSyMetaItems;
import supersymmetry.common.materials.SusyMaterials;
import supersymmetry.common.mui.widget.ConditionalWidget;
import supersymmetry.common.mui.widget.RocketRenderWidget;
import supersymmetry.common.mui.widget.SlotWidgetMentallyStable;

// TODO add a tooltip to the controller item that mentions losing progress if power/coolant is cut
public class MetaTileEntityAerospaceFlightSimulator extends MultiblockWithDisplayBase
                                                    implements IWorkable {

    private static Fluid COOLANT_IN;

    private static Fluid COOLANT_OUT;

    private static Entity createEntityByResource(ResourceLocation rl, World world) {
        EntityEntry entry = ForgeRegistries.ENTITIES.getValue(rl);
        if (entry == null) {
            throw new IllegalArgumentException("No entity registered under " + rl);
        }
        try {
            return entry.newInstance(world);
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate entity with constructor (World)", e);
        }
    }

    private IEnergyContainer energyContainer;
    public IMultipleTankHandler inputCoolant;

    public IMultipleTankHandler outputCoolant;

    private boolean isActive = false;

    private boolean isWorkingEnabled = true;

    protected boolean hasNotEnoughEnergy;

    private long progress = 0;

    private boolean coolantFilled;
    private List<BlockPos> coolantPositions;

    public DataStorageLoader rocketBlueprintSlot = new DataStorageLoader(
            this,
            item -> SuSyMetaItems.isMetaItem(item) == SuSyMetaItems.DATA_CARD_MASTER_BLUEPRINT.metaValue &&
                    item.getTagCompound() != null && item.getTagCompound().getBoolean("buildstat"));

    private boolean hasNotEnoughCoolant = false;

    public RocketFuelEntry fuel;

    private double gravity = 9.81;

    public MetaTileEntityAerospaceFlightSimulator(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);

        COOLANT_IN = SusyMaterials.Perfluoro2Methyl3Pentanone.getFluid();

        COOLANT_OUT = SusyMaterials.WarmPerfluoro2Methyl3Pentanone.getFluid();
        if (COOLANT_OUT == null) {
            COOLANT_OUT = FluidRegistry.getFluid("lava");
        }

        this.energyContainer = new EnergyContainerList(new ArrayList<>());
        this.inputCoolant = new FluidTankList(true);
        this.outputCoolant = new FluidTankList(true);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.isActive = data.getBoolean("isActive");
        this.isWorkingEnabled = data.getBoolean("isWorkingEnabled");
        if (data.hasKey("progress")) {
            this.progress = data.getLong("progress");
        }
        this.gravity = data.getDouble("gravity");

        if (data.hasKey("fuelRegistryName")) {
            this.fuel = RocketFuelEntry.getCopyOf(data.getString("fuelRegistryName"));
        }

        NBTTagCompound blueprintTag = data.getCompoundTag("blueprint_slot");
        if (blueprintTag != null) {
            ItemStack blueprintStack = new ItemStack(blueprintTag);
            this.rocketBlueprintSlot.setStackInSlot(0, blueprintStack);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        NBTTagCompound tag = super.writeToNBT(data);
        tag.setBoolean("isActive", this.isActive);
        tag.setBoolean("isWorkingEnabled", this.isWorkingEnabled);
        if (progress != 0)
            tag.setLong("progress", this.progress);
        tag.setDouble("gravity", this.gravity);

        if (this.fuel != null) {
            tag.setString("fuelRegistryName", this.fuel.getRegistryName());
        }

        if (!rocketBlueprintSlot.isEmpty()) {
            var bp = rocketBlueprintSlot.getStackInSlot(0).writeToNBT(new NBTTagCompound());
            tag.setTag("blueprint_slot", bp);
        }

        return tag;
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(this.isActive);
        buf.writeBoolean(this.isWorkingEnabled);
        buf.writeLong(this.progress);
        buf.writeDouble(this.gravity);
        if (this.fuel != null) {
            buf.writeBoolean(true);
            buf.writeString(this.fuel.getRegistryName());
        } else {
            buf.writeBoolean(false);
        }

        if (hasBlueprint()) {
            buf.writeBoolean(true);
            buf.writeItemStack(rocketBlueprintSlot.getStackInSlot(0));
        } else {
            buf.writeBoolean(false);
        }
        if (this.isActive)
            this.rocketBlueprintSlot.setLocked(true);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.isActive = buf.readBoolean();
        this.isWorkingEnabled = buf.readBoolean();
        this.progress = buf.readLong();
        this.gravity = buf.readDouble();
        if (buf.readBoolean()) {
            this.fuel = RocketFuelEntry.getCopyOf(buf.readString(32767));
        }

        if (buf.readBoolean()) {
            try {
                ItemStack blueprintStack = buf.readItemStack();
                this.rocketBlueprintSlot.setStackInSlot(0, blueprintStack);
            } catch (Exception e) {
                // goognt
            }
        }
        if (this.isActive) this.rocketBlueprintSlot.setLocked(true);
    }

    @Override
    public int getProgress() {
        return (int) this.progress;
    }

    @Override
    public int getMaxProgress() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isActive() {
        return super.isActive() && this.isActive;
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.isWorkingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        this.isWorkingEnabled = isWorkingAllowed;
        markDirty();
        World world = getWorld();
        if (world != null && !world.isRemote) {
            writeCustomData(GregtechDataCodes.WORKING_ENABLED, buf -> buf.writeBoolean(isWorkingEnabled));
        }
    }

    public void setActive(boolean active) {
        if (this.isActive != active && this.isStructureFormed()) {
            this.isActive = active;
            markDirty();
            World world = getWorld();
            if (world != null && !world.isRemote) {
                writeCustomData(GregtechDataCodes.WORKABLE_ACTIVE, buf -> buf.writeBoolean(active));
            }
        }
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.WORKING_ENABLED) {
            this.setWorkingEnabled(buf.readBoolean());
        }
        if (dataId == GregtechDataCodes.WORKABLE_ACTIVE) {
            this.setActive(buf.readBoolean());
        }
        if (dataId == GregtechDataCodes.LOCK_OBJECT_HOLDER) {
            this.rocketBlueprintSlot.setLocked(buf.readBoolean());
        }
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.energyContainer = new EnergyContainerList(new ArrayList<>());
        this.inputCoolant = new FluidTankList(true);
        this.outputCoolant = new FluidTankList(true);
        this.progress = 0;

        this.coolantPositions = null; // Clear water fill data when the structure is invalidated
        this.coolantFilled = false;
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
        return MetaBlocks.METAL_CASING.getState(MetalCasingType.STEEL_SOLID);
    }

    public IBlockState getComputerState() {
        return SuSyBlocks.PROCESSOR_CLUSTER.getState(BlockProcessorCluster.TierType.TIER_1);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityAerospaceFlightSimulator(metaTileEntityId);
    }

    // doesnt check if the blueprint itself is complete but that should go onto the slot check
    public boolean hasBlueprint() {
        return !this.rocketBlueprintSlot.isEmpty() && this.rocketBlueprintSlot.getStackInSlot(0).hasTagCompound() &&
                this.rocketBlueprintSlot.getStackInSlot(0).getMetadata() ==
                        SuSyMetaItems.DATA_CARD_MASTER_BLUEPRINT.metaValue;
    }

    @Nullable
    public AbstractRocketBlueprint getBlueprint() {
        if (!this.rocketBlueprintSlot.isEmpty() && this.rocketBlueprintSlot.getStackInSlot(0).hasTagCompound()) {
            NBTTagCompound tag = this.rocketBlueprintSlot.getStackInSlot(0).getTagCompound();
            AbstractRocketBlueprint bp = AbstractRocketBlueprint.getCopyOf(tag.getString("name"));
            if (bp != null && bp.readFromNBT(tag) && bp.isFullBlueprint()) {
                return bp;
            }
        }
        return null;
    }

    public void stop() {
        setActive(false);
        setWorkingEnabled(false);
        this.rocketBlueprintSlot.setLocked(false);
        AbstractRocketBlueprint bp = getBlueprint();
        if (bp == null || !bp.isFullBlueprint()) {
            SusyLog.logger.info("bp == {}", bp);
            return;
        }
        if (bp instanceof IAFSimprovable bp2) {
            bp2.setAFSimprovement(this.progress);
        }

        this.rocketBlueprintSlot.setNBT(
                (ignored) -> {
                    NBTTagCompound n = bp.writeToNBT();
                    n.setBoolean("buildstat", true);
                    return n;
                });
        this.progress = 0;
    }

    // wipe the progress when there is not enough power/coolant to prevent the player from having too
    // much fun
    public void crash() {
        setActive(false);
        setWorkingEnabled(false);
        this.rocketBlueprintSlot.setLocked(false);
        this.progress = 0;
    }

    public void start() {
        var bp = this.getBlueprint();
        if (bp instanceof IAFSimprovable bp2) {
            this.progress = bp2.getAFSimprovement();
            setActive(true);
            setWorkingEnabled(true);
            this.rocketBlueprintSlot.setLocked(true);
        }
    }

    @Override
    public boolean isStructureObstructed() {
        return super.isStructureObstructed() || !coolantFilled;
    }

    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.ASSEMBLER_OVERLAY;
    }

    // fix getEnergyToConsume and getCompute when some generic computer blocks are added
    protected int getEnergyToConsume() {
        return 1000;
    }

    // per tick
    protected int getCompute() {
        return 1;
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.energyContainer = new EnergyContainerList(getAbilities(MultiblockAbility.INPUT_ENERGY));
        this.inputCoolant = new FluidTankList(true, getAbilities(MultiblockAbility.IMPORT_FLUIDS));
        this.outputCoolant = new FluidTankList(true, getAbilities(MultiblockAbility.EXPORT_FLUIDS));

        this.coolantPositions = context.getOrDefault(FLUID_BLOCKS_KEY, new ArrayList<>());
        this.coolantFilled = coolantPositions.isEmpty();
    }

    @Override
    protected void addErrorText(List<ITextComponent> textList) {
        super.addErrorText(textList);
        if (hasNotEnoughCoolant) {
            textList.add(new TextComponentTranslation(this.getMetaName() + ".gui.no_coolant_warning"));
        }
        if (isStructureFormed() && !coolantFilled) {
            textList.add(
                    TextComponentUtil.translationWithColor(
                            TextFormatting.RED, this.getMetaName() + ".obstructed"));
            textList.add(
                    TextComponentUtil.translationWithColor(
                            TextFormatting.GRAY, this.getMetaName() + ".obstructed.desc"));
        }
    }

    // mb/tick
    protected int getCoolantToConsume(int energyUsage) {
        return (int) Math.ceil(Math.sqrt((double) energyUsage / 20));
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

        if (!this.isActive() || !this.isWorkingEnabled() || this.isStructureObstructed()) {
            return;
        }
        int energyToConsume = getEnergyToConsume();
        boolean maintenance = ConfigHolder.machines.enableMaintenance && hasMaintenanceMechanics();
        if (maintenance) {
            energyToConsume += getNumMaintenanceProblems() * energyToConsume / 10;
        }
        int coolantToConsume = getCoolantToConsume(energyToConsume);
        FluidStack drainedFluid = inputCoolant.drain(new FluidStack(COOLANT_IN, coolantToConsume), false);
        boolean enoughCoolant = false;
        if (drainedFluid != null) {
            enoughCoolant = drainedFluid.amount == coolantToConsume;
        }
        boolean enoughSpaceForCoolant = outputCoolant.fill(new FluidStack(COOLANT_OUT, coolantToConsume), false) ==
                coolantToConsume;
        if (enoughCoolant && enoughSpaceForCoolant) {
            hasNotEnoughCoolant = false;
        } else {
            hasNotEnoughCoolant = true;
            crash();
        }
        if (hasNotEnoughEnergy && energyContainer.getInputPerSec() > 19L * energyToConsume) {
            hasNotEnoughEnergy = false;
        }
        boolean enoughEnergy = energyContainer.getEnergyStored() >= energyToConsume && !hasNotEnoughEnergy;
        if (enoughEnergy && !hasNotEnoughCoolant) {
            long consumed = energyContainer.removeEnergy(energyToConsume);
            if (consumed == -energyToConsume) {
                inputCoolant.drain(new FluidStack(COOLANT_IN, coolantToConsume), true);
                outputCoolant.fill(new FluidStack(COOLANT_OUT, coolantToConsume), true);
                setActive(true);
                this.progress += this.getCompute();
            } else {
                hasNotEnoughEnergy = true;
                crash();
            }
        } else {
            hasNotEnoughEnergy = true;
            crash();
        }
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("IIIIIIIII", "IIIIIIIII", "IIIIIIIII", "IIIIIIIII", "IIIIIIIII")
                .aisle("IIIIIIIII", "IPFPFPFPI", "IPFPFPFPI", "IFFFFFFFI", "ITTTTTTTI")
                .aisle("IIIIIIIII", "IPFPFPFPI", "IPFPFPFPI", "IFFFFFFFI", "ITTTTTTTI")
                .aisle("IIIIIIIII", "IPFPFPFPI", "IPFPFPFPI", "IFFFFFFFI", "ITTTTTTTI")
                .aisle("IIIIIIIII", "IPFPFPFPI", "IPFPFPFPI", "IFFFFFFFI", "ITTTTTTTI")
                .aisle("IIIISIIII", "ITCTCTCTI", "ITCTCTCTI", "ITCTCTCTI", "IIIIIIIII")
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

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return createGUITemplate(entityPlayer).build(this.getHolder(), entityPlayer);
    }

    private ModularUI.Builder createGUITemplate(EntityPlayer entityPlayer) {
        if (!this.isStructureFormed()) {
            ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 165, 35);
            builder.widget(new ImageWidget(4, 4, 157, 27, GuiTextures.DISPLAY));
            builder.label(9, 12, this.getMetaName() + ".gui.not_formed", 0xAE5421);
            return builder;
        }
        int width = 280;
        int height = 210;

        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, width, height);
        builder.image(4, 4, width - 8, height - 91, GuiTextures.DISPLAY);
        builder.widget(
                new IndicatorImageWidget(width - 24, height / 2 - 3, 17, 17, getLogo())
                        .setWarningStatus(getWarningLogo(), this::addWarningText)
                        .setErrorStatus(getErrorLogo(), this::addErrorText));
        if (this instanceof IControllable controllable) {
            builder.widget(
                    new ImageCycleButtonWidget(
                            width - 42,
                            height - 23,
                            18,
                            18,
                            GuiTextures.BUTTON_POWER,
                            controllable::isWorkingEnabled,
                            controllable::setWorkingEnabled));
            builder.widget(
                    new ImageWidget(width - 42, height - 5, 18, 6, GuiTextures.BUTTON_POWER_DETAIL));
        }
        builder.bindPlayerInventory(entityPlayer.inventory, 125);

        // blueprint slot
        SlotWidgetMentallyStable blueprintSlot = new SlotWidgetMentallyStable(this.rocketBlueprintSlot, 0, 0, 0);

        blueprintSlot.setSelfPosition(new Position(12, height / 5 - 27));
        blueprintSlot.setChangeListener(
                () -> {
                    if (this.rocketBlueprintSlot.isEmpty()) {
                        blueprintSlot.setSelfPosition(new Position(12, height / 2 - 27));
                    } else {
                        blueprintSlot.setSelfPosition(new Position(width - 23, height - 23));
                    }
                });
        blueprintSlot.setBackgroundTexture(GuiTextures.SLOT_DARK);
        builder.widget(blueprintSlot);

        ConditionalWidget mainGroup = new ConditionalWidget(0, 0, width, height, () -> true);
        ConditionalWidget fuelGroup = new ConditionalWidget(-120, 0, 120, 80, () -> true);
        fuelGroup.addWidget(new ImageWidget(0, 0, 120, 80, GuiTextures.DISPLAY));
        fuelGroup.addWidget(
                new LabelWidget(
                        5, 5, I18n.format(this.getMetaName() + ".gui.fuel_selector_label"), 0xffffff));
        fuelGroup.addWidget(
                new FuelRegistrySelectorWidget(
                        4,
                        14,
                        80,
                        60,
                        (fuel) -> {
                            this.fuel = fuel;
                        }));
        // Gravity selector
        fuelGroup.addWidget(
                new LabelWidget(
                        5, 60, I18n.format(this.getMetaName() + ".gui.gravity_selector_label"), 0xffffff));
        fuelGroup.addWidget(
                new TextFieldWidget2(
                        5,
                        68,
                        60,
                        12,
                        () -> Double.valueOf(gravity).toString(),
                        value -> {
                            if (!value.isEmpty()) {
                                try {
                                    gravity = Double.parseDouble(value);
                                    if (gravity <= 0) {
                                        gravity = 9.81;
                                    }
                                } catch (NumberFormatException ignored) {
                                    gravity = 9.81;
                                }
                            }
                        })
                                .setAllowedChars(TextFieldWidget2.DECIMALS)
                                .setMaxLength(6));

        mainGroup.addWidgetWithTest(
                fuelGroup, () -> this.hasBlueprint() && !this.isActive() && this.fuel == null);

        mainGroup.addWidgetWithTest(
                new AdvancedTextWidget(
                        9,
                        19,
                        (l) -> {
                            AbstractRocketBlueprint bp = this.getBlueprint();
                            if (this.hasBlueprint() && bp != null) {
                                l.add(
                                        new TextComponentTranslation(
                                                this.getMetaName() + ".gui.rocket_name",
                                                I18n.format("susy.rocketry." + bp.name + ".name")));

                                l.add(
                                        new TextComponentTranslation(
                                                this.getMetaName() + ".gui.stages", bp.getStages().size()));
                                for (RocketStage stage : bp.getStages()) {
                                    l.add(new TextComponentString("  - " + I18n.format(stage.getLocalizationKey())));
                                }
                            }
                        },
                        0xffffff),
                () -> this.hasBlueprint() && !isActive());

        // multi information
        // these should probably be visible at all times in some different corner
        mainGroup.addWidgetWithTest(
                new LabelWidget(
                        width - 130,
                        9,
                        I18n.format(getMetaName() + ".gui.computation_power", this.getCompute()),
                        0xffffff),
                () -> !this.isActive());
        mainGroup.addWidgetWithTest(
                new LabelWidget(
                        width - 130,
                        20,
                        I18n.format(
                                getMetaName() + ".gui.coolant_flow",
                                this.getCoolantToConsume(this.getEnergyToConsume()) * 20),
                        0xffffff),
                () -> !this.isActive());
        mainGroup.addWidgetWithTest(
                new LabelWidget(
                        width - 130,
                        31,
                        I18n.format(getMetaName() + ".gui.energy_consumption", this.getEnergyToConsume()),
                        0xffffff),
                () -> !this.isActive());

        mainGroup.addWidgetWithTest(
                new LabelWidget(9, 9, getMetaFullName(), 0xffffff), () -> !this.isActive());

        mainGroup.addWidgetWithTest(
                new LabelWidget(9, height - 80, I18n.format(this.getMetaName() + "gui.cant_improve_error"), 0xff0000),
                () -> this.hasBlueprint() && !(this.getBlueprint() instanceof IAFSimprovable));
        // start button
        mainGroup.addWidgetWithTest(
                new ClickButtonWidget(
                        width - 61,
                        height - 23,
                        18,
                        18,
                        "",
                        (ignored) -> {
                            if (this.hasBlueprint() && !this.isActive() && this.fuel != null) {
                                int energyToConsume = getEnergyToConsume();
                                boolean maintenance = ConfigHolder.machines.enableMaintenance &&
                                        hasMaintenanceMechanics();
                                if (maintenance) {
                                    energyToConsume += getNumMaintenanceProblems() * energyToConsume / 10;
                                }
                                int coolantToConsume = getCoolantToConsume(energyToConsume);
                                boolean enoughCoolant = inputCoolant.drain(new FluidStack(COOLANT_IN, coolantToConsume),
                                        false).amount == coolantToConsume;
                                boolean enoughSpaceForCoolant = outputCoolant
                                        .fill(new FluidStack(COOLANT_OUT, coolantToConsume), false) == coolantToConsume;
                                if (enoughCoolant && enoughSpaceForCoolant) {
                                    hasNotEnoughCoolant = false;
                                } else {
                                    hasNotEnoughCoolant = true;
                                    crash();
                                }
                                if (hasNotEnoughEnergy && energyContainer.getInputPerSec() > 19L * energyToConsume) {
                                    hasNotEnoughEnergy = false;
                                }
                                if (!hasNotEnoughEnergy && !hasNotEnoughCoolant) {
                                    this.start();
                                }
                            }
                        })
                                .setTooltipText(this.getMetaName() + ".gui.start_button")
                                .setButtonTexture(SusyGuiTextures.GREEN_CIRCLE),
                () -> !this.isActive() && this.hasBlueprint() && this.getBlueprint() instanceof IAFSimprovable);
        // stop button
        mainGroup.addWidgetWithTest(
                new ClickButtonWidget(
                        width - 61,
                        height - 23,
                        18,
                        18,
                        "",
                        (ignored) -> {
                            if (this.hasBlueprint() && this.isActive()) {

                                // && !hasNotEnoughCoolant
                                // && !hasNotEnoughEnergy
                                this.stop();
                            }
                        })
                                .setTooltipText(this.getMetaName() + ".gui.stop_button")
                                .setButtonTexture(SusyGuiTextures.RED_CIRCLE),
                () -> this.isActive() && this.hasBlueprint());

        // button to open the fuel selector ui again, would be nice to give it a different texture
        mainGroup.addWidgetWithTest(
                new ClickButtonWidget(
                        width - 79,
                        height - 23,
                        18,
                        18,
                        "",
                        (ignored) -> {
                            if (!this.isActive() && this.fuel != null) {
                                this.fuel = null;
                            }
                        })
                                .setTooltipText(this.getMetaName() + ".gui.reset_fuel")
                                .setButtonTexture(SusyGuiTextures.RED_X)
                                .setShouldClientCallback(true),
                () -> !this.isActive() && this.fuel != null);

        // rocket render

        mainGroup.addWidgetConditionalInit(
                () -> {
                    AbstractRocketBlueprint bp = this.getBlueprint();
                    if (this.hasBlueprint() && bp != null && bp.isFullBlueprint() && this.isActive()) {
                        return true;
                    }
                    return false;
                },
                () -> {
                    AbstractRocketBlueprint bp = this.getBlueprint();
                    if (bp != null && bp.isFullBlueprint()) {
                        ResourceLocation entity_res = bp.relatedEntity;
                        DummyWorld world = new DummyWorld();
                        Entity rocketentity = this.createEntityByResource(entity_res, world);
                        rocketentity.setPosition(0, 0, 0);
                        return new RocketRenderWidget(
                                new Size(width - 15, 100), new Position(7, 11), rocketentity);
                    }
                    SusyLog.logger.fatal("something very very bad happened in the ui, bp:{}",
                            bp == null ? "null" : bp.writeToNBT());
                    return null;
                });
        builder.widget(mainGroup);

        return builder;
    }
}
