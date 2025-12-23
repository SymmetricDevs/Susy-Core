package supersymmetry.common.metatileentities.multi.rocket;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import org.jetbrains.annotations.NotNull;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.IWorkable;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.ImageCycleButtonWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.IndicatorImageWidget;
import gregtech.api.gui.widgets.LabelWidget;
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
import gregtech.api.util.world.DummyWorld;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.MetaBlocks;
import supersymmetry.api.SusyLog;
import supersymmetry.api.rocketry.rockets.AbstractRocketBlueprint;
import supersymmetry.api.util.DataStorageLoader;
import supersymmetry.common.item.SuSyMetaItems;
import supersymmetry.common.mui.widget.ConditionalWidget;
import supersymmetry.common.mui.widget.RocketRenderWidget;
import supersymmetry.common.mui.widget.SlotWidgetMentallyStable;

// TODO add a tooltip to the controller item that mentions losing progress if power/coolant is cut
public class MetaTileEntityAerospaceFlightSimulator extends MultiblockWithDisplayBase
                                                    implements IWorkable {

    private static Fluid COOLANT_IN;

    private static Fluid COOLANT_OUT;

    private static final double f0 = 0.5;

    private static final double k = 1.0;
    private static final double xh = -1000.0 / k * Math.log(1.0 - f0);

    public static Entity createEntityByResource(ResourceLocation rl, World world) {
        var entry = ForgeRegistries.ENTITIES.getValue(rl);
        if (entry == null) {
            throw new IllegalArgumentException("No entity registered under " + rl);
        }
        try {
            return entry.newInstance(world);
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate entity with constructor (World)", e);
        }
    }

    public static double getSuccessProbability(double progress) {
        return 1.0 - Math.exp(-k * (progress + xh) / 1000.0);
    }

    private IEnergyContainer energyContainer;
    public IMultipleTankHandler inputCoolant;

    public IMultipleTankHandler outputCoolant;

    private boolean isActive = false;

    private boolean isWorkingEnabled = true;

    protected boolean hasNotEnoughEnergy;

    private int progress = 0;

    public DataStorageLoader rocketBlueprintSlot = new DataStorageLoader(
            this,
            item -> SuSyMetaItems.isMetaItem(item) == SuSyMetaItems.DATA_CARD_MASTER_BLUEPRINT.metaValue &&
                    item.getTagCompound() != null && item.getTagCompound().getBoolean("buildstat"));

    private boolean hasNotEnoughCoolant = false;

    public MetaTileEntityAerospaceFlightSimulator(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);

        COOLANT_IN = FluidRegistry.getFluid("water");

        COOLANT_OUT = FluidRegistry.getFluid("warm_water");
        if (COOLANT_OUT == null) {
            COOLANT_OUT = FluidRegistry.getFluid("lava");
        }

        this.energyContainer = new EnergyContainerList(new ArrayList<>());
        this.inputCoolant = new FluidTankList(true);
        this.outputCoolant = new FluidTankList(true);
    }

    @Override
    public int getProgress() {
        return this.progress;
    }

    @Override
    public int getMaxProgress() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isActive() {
        return super.isActive() && this.isActive;
    }

    public void setActive(boolean active) {
        if (this.isActive != active) {
            this.isActive = active;
            markDirty();
            World world = getWorld();
            if (world != null && !world.isRemote) {
                writeCustomData(GregtechDataCodes.WORKABLE_ACTIVE, buf -> buf.writeBoolean(active));
            }
        }
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
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    public IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(MetalCasingType.STEEL_SOLID);
    }

    public IBlockState getComputerState() {
        return MetaBlocks.METAL_CASING.getState(MetalCasingType.STEEL_SOLID);
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

    // can return null if something breaks
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
        // used as a measure of how complex the entire rocket is -> should take longer to simulate i
        // guess?
        double totalAssemblyTime = bp.getStages().stream()
                .flatMap(x -> x.getComponents().values().stream().flatMap(y -> y.stream()))
                .mapToDouble(x -> x.getAssemblyDuration())
                .sum();
        var successProb = getSuccessProbability((double) this.progress / totalAssemblyTime);
        bp.AFSSuccessChance = successProb;
        this.rocketBlueprintSlot.setNBT(
                (_) -> {
                    var n = bp.writeToNBT();
                    n.setBoolean("buildstat", true);
                    return n;
                });
        setActive(false);
        this.rocketBlueprintSlot.setLocked(false);
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
        this.progress = 0;
        setActive(true);
        setWorkingEnabled(true);
        this.rocketBlueprintSlot.setLocked(true);
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
        this.progress = 0;
    }

    @Override
    protected void addErrorText(List<ITextComponent> textList) {
        super.addErrorText(textList);
        if (hasNotEnoughCoolant) {
            textList.add(new TextComponentTranslation(this.getMetaName() + ".gui.no_coolant_warning"));
        }
    }

    // mb/tick
    protected int getCoolantToConsume(int energyUsage) {
        return (int) Math.ceil(Math.sqrt((double) energyUsage / 20));
    }

    @Override
    protected void updateFormedValid() {
        if (!this.isActive() || !this.isWorkingEnabled()) {
            return;
        }
        int energyToConsume = getEnergyToConsume();
        boolean maintenance = ConfigHolder.machines.enableMaintenance && hasMaintenanceMechanics();
        if (maintenance) {
            energyToConsume += getNumMaintenanceProblems() * energyToConsume / 10;
        }
        int coolantToConsume = getCoolantToConsume(energyToConsume);
        boolean enoughCoolant = inputCoolant.drain(new FluidStack(COOLANT_IN, coolantToConsume), false).amount ==
                coolantToConsume;
        var tmp_fill_result = outputCoolant.fill(new FluidStack(COOLANT_OUT, coolantToConsume), false);
        boolean enoughSpaceForCoolant = tmp_fill_result == coolantToConsume;
        if (enoughCoolant && enoughSpaceForCoolant) {
            hasNotEnoughCoolant = false;
        } else {
            hasNotEnoughCoolant = true;
            // SusyLog.logger.info(
            // "hasNotEnoughCoolant:{},"
            // + "
            // hasNotEnoughEnergy:{},enoughCoolant:{},enoughSpaceForCoolant:{},coolantToConsume:{},energyToConsume:{},tmp_fill_result:{},cc:{}",
            // hasNotEnoughCoolant,
            // hasNotEnoughEnergy,
            // enoughCoolant,
            // enoughSpaceForCoolant,
            // coolantToConsume,
            // energyToConsume,
            // tmp_fill_result,
            // outputCoolant.getFluidTanks());
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
                .aisle("EEEEE", "EEEEE", "     ", "     ", "     ", "CCCCC")
                .aisle("CCCCC", "CCCCC", "PPPPP", "IPPPO", "PPPPP", "CCCCC")
                .aisle("CCCCC", "CCCCC", "PPPPP", "IPPPO", "PPPPP", "CCCCC")
                .aisle("CCCCC", "CCCCC", "PPPPP", "IPPPO", "PPPPP", "CCCCC")
                .aisle("CCSMC", "CCCCC", "     ", "     ", "     ", "CCCCC")
                .where('M', maintenancePredicate())
                .where('S', selfPredicate())
                .where(' ', air())
                .where('C', states(getCasingState()))
                .where('P', states(getComputerState()))
                .where(
                        'I',
                        abilities(MultiblockAbility.IMPORT_FLUIDS)
                                .setMaxGlobalLimited(2)
                                .setMinGlobalLimited(1, 1)
                                .or(states(getComputerState())))
                .where(
                        'O',
                        abilities(MultiblockAbility.EXPORT_FLUIDS)
                                .setMaxGlobalLimited(2)
                                .setMinGlobalLimited(1, 1)
                                .or(states(getComputerState())))
                .where(
                        'E',
                        abilities(MultiblockAbility.INPUT_ENERGY)
                                .setMaxGlobalLimited(6)
                                .setMinGlobalLimited(1, 6)
                                .or(states(getCasingState())))
                .build();
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return createGUITemplate(entityPlayer).build(this.getHolder(), entityPlayer);
    }

    private ModularUI.Builder createGUITemplate(EntityPlayer entityPlayer) {
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
        var bpw = new SlotWidgetMentallyStable(this.rocketBlueprintSlot, 0, 0, 0);

        bpw.setSelfPosition(new Position(12, height / 2 - 27));

        var g = new ConditionalWidget(
                0,
                0,
                width,
                height,
                () -> {
                    return true;
                });
        g.addWidgetWithTest(
                new AdvancedTextWidget(
                        9,
                        19,
                        (l) -> {
                            var bp = this.getBlueprint();
                            if (this.hasBlueprint() && bp != null) {
                                l.add(
                                        new TextComponentTranslation(
                                                this.getMetaName() + ".gui.rocket_name",
                                                I18n.format("susy.rocketry." + bp.name + ".name")));

                                l.add(
                                        new TextComponentTranslation(
                                                this.getMetaName() + ".gui.stages", bp.getStages().size()));
                                for (var stage : bp.getStages()) {
                                    l.add(new TextComponentString("  - " + I18n.format(stage.getLocalizationKey())));
                                }
                            }
                        },
                        0xffffff),
                () -> {
                    return this.hasBlueprint() && !isActive();
                });
        bpw.setChangeListener(
                () -> {
                    if (this.rocketBlueprintSlot.isEmpty()) {
                        bpw.setSelfPosition(new Position(12, height / 2 - 27));
                    } else {
                        bpw.setSelfPosition(new Position(width - 23, height - 23));
                    }
                });
        bpw.setBackgroundTexture(GuiTextures.SLOT_DARK);
        builder.widget(bpw);
        // label gets in the way a little
        g.addWidgetWithTest(
                new LabelWidget(9, 9, getMetaFullName(), 0xffffff),
                () -> {
                    return (!this.isActive());
                });
        g.addWidgetWithTest(
                new ClickButtonWidget(
                        width - 40,
                        height - 40,
                        18,
                        18,
                        "",
                        (_) -> {
                            if (this.hasBlueprint() && !this.isActive()) {

                                // && !hasNotEnoughCoolant
                                // && !hasNotEnoughEnergy
                                this.start();
                            }
                        })
                                .setTooltipText(this.getMetaName() + ".gui.start_button"),
                () -> {
                    return (!this.isActive() && this.hasBlueprint());
                });
        g.addWidgetWithTest(
                new ClickButtonWidget(
                        width - 40,
                        height - 40,
                        18,
                        18,
                        "",
                        (_) -> {
                            if (this.hasBlueprint() && this.isActive()) {

                                // && !hasNotEnoughCoolant
                                // && !hasNotEnoughEnergy
                                this.stop();
                            }
                        })
                                .setTooltipText(this.getMetaName() + ".gui.stop_button"),
                () -> {
                    return (this.isActive() && this.hasBlueprint());
                });

        // rocket render

        g.addWidgetConditionalInit(
                () -> {
                    if (this.hasBlueprint() && this.getBlueprint() != null && isActive()) {
                        return true;
                    }
                    return false;
                },
                () -> {
                    var bp = this.getBlueprint();
                    if (bp != null && bp.isFullBlueprint()) {
                        ResourceLocation entity_res = bp.relatedEntity;
                        DummyWorld world = new DummyWorld();
                        Entity rocketentity = this.createEntityByResource(entity_res, world);
                        rocketentity.setPosition(0, 0, 0);
                        return new RocketRenderWidget(
                                new Size(width - 15, 100), new Position(7, 11), rocketentity);
                    }
                    return null;
                });
        builder.widget(
                new AdvancedTextWidget(
                        width - 100,
                        height - 82,
                        (l) -> {
                            var c = this.progress;

                            l.add(new TextComponentTranslation(Integer.toString(c)));
                        },
                        0xffffff));

        builder.widget(g);

        return builder;
    }
}
