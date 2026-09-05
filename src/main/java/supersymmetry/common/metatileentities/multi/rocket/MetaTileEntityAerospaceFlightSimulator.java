package supersymmetry.common.metatileentities.multi.rocket;

import static supercritical.api.pattern.SCPredicates.FLUID_BLOCKS_KEY;
import static supercritical.api.pattern.SCPredicates.fluid;
import static supersymmetry.api.capability.SuSyDataCodes.SYNC_AFS;
import static supersymmetry.api.space.CelestialObjects.EARTH;
import static supersymmetry.api.space.Planetoid.getPlanetFromItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.items.ItemStackHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
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
import supersymmetry.api.metatileentity.multiblock.SuSyPredicates;
import supersymmetry.api.rocketry.fuels.LiquidRocketFuelEntry;
import supersymmetry.api.rocketry.fuels.RocketFuelEntry;
import supersymmetry.api.rocketry.rockets.AbstractRocketBlueprint;
import supersymmetry.api.rocketry.rockets.IAFSImprovable;
import supersymmetry.api.space.Planetoid;
import supersymmetry.api.unification.material.properties.SolidRocketFuelProperty;
import supersymmetry.api.util.DataStorageLoader;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.item.SuSyMetaItems;
import supersymmetry.common.materials.SusyMaterials;
import supersymmetry.common.mui.widget.ConditionalWidget;
import supersymmetry.common.mui.widget.FuelRegistrySelectorWidget;
import supersymmetry.common.mui.widget.RocketRenderWidget;
import supersymmetry.common.mui.widget.SlotWidgetMentallyStable;
import supersymmetry.common.rocketry.SuccessCalculation.AFSStats;

// TODO add a tooltip to the controller item that mentions losing progress if power/coolant is cut
public class MetaTileEntityAerospaceFlightSimulator extends MultiblockWithDisplayBase implements IWorkable {

    private static Fluid COOLANT_IN;

    private static Fluid COOLANT_OUT;
    private int computationPerTick;
    private int coolantPerTick;
    private int energyPerTick;

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

    private boolean isWorkingEnabled = false;

    protected boolean hasNotEnoughEnergy;

    private long progress = 0;

    private boolean coolantFilled;
    private List<BlockPos> coolantPositions;

    public DataStorageLoader rocketBlueprintSlot = new DataStorageLoader(this,
            item -> SuSyMetaItems.isMetaItem(item) == SuSyMetaItems.DATA_CARD_MASTER_BLUEPRINT.metaValue &&
                    item.getTagCompound() != null && item.getTagCompound().getBoolean("buildstat"));

    private boolean hasNotEnoughCoolant = false;

    public RocketFuelEntry fuel;
    public Planetoid planet = EARTH;
    public final List<FluidStack> fuelList = new ArrayList<>();

    // phantom slot backing the solid fuel selector; only dusts of a material with a
    // SolidRocketFuelProperty get in
    public final ItemStackHandler solidFuelSlot = new ItemStackHandler(1) {

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return SolidRocketFuelProperty.search(stack) != null;
        }

        @Override
        public void setStackInSlot(int slot, @NotNull ItemStack stack) {
            // phantom slots write straight through setStackInSlot, which skips isItemValid
            if (!stack.isEmpty() && !isItemValid(slot, stack)) {
                return;
            }
            super.setStackInSlot(slot, stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            setFuelFromData();
            markDirty();
        }
    };

    public final ItemStackHandler planetSlot = new ItemStackHandler(1) {

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return getPlanetFromItem(stack) != null;
        }

        @Override
        public void setStackInSlot(int slot, @NotNull ItemStack stack) {
            // phantom slots write straight through setStackInSlot, which skips isItemValid
            if (!stack.isEmpty() && !isItemValid(slot, stack)) {
                return;
            }
            super.setStackInSlot(slot, stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            setFuelFromData();
            markDirty();
        }
    };

    private double turnAltitude = 50;
    private double cargoMass = 0;
    private AFSStats stats = AFSStats.none();

    public MetaTileEntityAerospaceFlightSimulator(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);

        COOLANT_IN = SusyMaterials.FC75.getFluid();

        COOLANT_OUT = SusyMaterials.WarmFC75.getFluid();
        if (COOLANT_OUT == null) {
            COOLANT_OUT = FluidRegistry.getFluid("lava");
        }

        this.energyContainer = new EnergyContainerList(new ArrayList<>());
        this.inputCoolant = new FluidTankList(true);
        this.outputCoolant = new FluidTankList(true);
        this.fuelList.add(null);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.isWorkingEnabled = data.getBoolean("isWorkingEnabled");
        if (data.hasKey("progress")) {
            this.progress = data.getLong("progress");
        }
        this.turnAltitude = data.getDouble("turnAltitude");
        this.cargoMass = data.getDouble("cargoMass");
        this.fuelList.clear();
        if (data.hasKey("fuelListSize")) {
            for (int i = 0; i < data.getInteger("fuelListSize"); i++) {
                NBTTagCompound tag = data.getCompoundTag("fuelList" + i);
                if (tag.isEmpty()) {
                    this.fuelList.add(null);
                } else {
                    this.fuelList.add(FluidStack.loadFluidStackFromNBT(data.getCompoundTag("fuelList" + i)));
                }
            }
        }
        this.solidFuelSlot.deserializeNBT(data.getCompoundTag("solidFuelSlot"));
        if (data.hasKey("AFSStats")) {
            this.stats = AFSStats.deserializeNBT(data.getCompoundTag("AFSStats"));
        }

        NBTTagCompound blueprintTag = data.getCompoundTag("blueprint_slot");
        if (blueprintTag != null) {
            ItemStack blueprintStack = new ItemStack(blueprintTag);
            this.rocketBlueprintSlot.setStackInSlot(0, blueprintStack);
        }
        // after the blueprint, since which of the two inputs is read depends on it
        setFuelFromData();
        this.computationPerTick = data.getInteger("computation");
        this.coolantPerTick = data.getInteger("coolant");
        this.energyPerTick = data.getInteger("energy");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        NBTTagCompound tag = super.writeToNBT(data);
        tag.setBoolean("isWorkingEnabled", this.isWorkingEnabled);
        if (progress != 0)
            tag.setLong("progress", this.progress);
        tag.setDouble("turnAltitude", this.turnAltitude);

        tag.setInteger("fuelListSize", this.fuelList.size());
        for (int i = 0; i < this.fuelList.size(); i++) {
            if (fuelList.get(i) == null) {
                tag.setTag("fuelList" + i, new NBTTagCompound());
            } else {
                tag.setTag("fuelList" + i, this.fuelList.get(i).writeToNBT(new NBTTagCompound()));
            }
        }
        tag.setTag("solidFuelSlot", this.solidFuelSlot.serializeNBT());

        if (!rocketBlueprintSlot.isEmpty()) {
            var bp = rocketBlueprintSlot.getStackInSlot(0).writeToNBT(new NBTTagCompound());
            tag.setTag("blueprint_slot", bp);
        }
        tag.setTag("AFSStats", this.stats.serializeNBT());
        return tag;
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(this.isWorkingEnabled);
        buf.writeLong(this.progress);
        buf.writeDouble(this.turnAltitude);
        buf.writeDouble(this.cargoMass);
        buf.writeInt(this.fuelList.size());
        for (int i = 0; i < this.fuelList.size(); i++) {
            if (this.fuelList.get(i) == null) {
                buf.writeBoolean(false);
            } else {
                buf.writeBoolean(true);
                buf.writeCompoundTag(this.fuelList.get(i).writeToNBT(new NBTTagCompound()));
            }
        }
        buf.writeItemStack(this.solidFuelSlot.getStackInSlot(0));

        if (hasBlueprint()) {
            buf.writeBoolean(true);
            buf.writeItemStack(rocketBlueprintSlot.getStackInSlot(0));
        } else {
            buf.writeBoolean(false);
        }
        this.stats.writeToBuffer(buf);
        if (this.isWorkingEnabled)
            this.rocketBlueprintSlot.setLocked(true);

        buf.writeInt(this.computationPerTick);
        buf.writeInt(this.coolantPerTick);
        buf.writeInt(this.energyPerTick);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.isWorkingEnabled = buf.readBoolean();
        this.progress = buf.readLong();
        this.turnAltitude = buf.readDouble();
        this.cargoMass = buf.readDouble();

        this.fuelList.clear();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            if (buf.readBoolean()) {
                try {
                    this.fuelList.add(FluidStack.loadFluidStackFromNBT(buf.readCompoundTag()));
                } catch (IOException e) {
                    // goognt
                }
            } else {
                this.fuelList.add(null);
            }
        }
        try {
            this.solidFuelSlot.setStackInSlot(0, buf.readItemStack());
        } catch (IOException e) {
            // goognt
        }
        if (buf.readBoolean()) {
            try {
                ItemStack blueprintStack = buf.readItemStack();
                this.rocketBlueprintSlot.setStackInSlot(0, blueprintStack);
            } catch (Exception e) {
                // goognt
            }
        }
        // after the blueprint, since which of the two inputs is read depends on it
        setFuelFromData();
        this.stats.readFromBuffer(buf);
        if (this.isWorkingEnabled)
            this.rocketBlueprintSlot.setLocked(true);

        this.computationPerTick = buf.readInt();
        this.coolantPerTick = buf.readInt();
        this.energyPerTick = buf.readInt();
    }

    // the fuel is derived state; fuelList and solidFuelSlot are what persists, and
    // the blueprint decides which of the two is read
    public void setFuelFromData() {
        if (!planetSlot.getStackInSlot(0).isEmpty()) {
            planet = getPlanetFromItem(planetSlot.getStackInSlot(0));
        }
        if (isSolidBlueprint()) {
            this.fuel = SolidRocketFuelProperty.search(this.solidFuelSlot.getStackInSlot(0));
            return;
        }

        List<Fluid> userFluids = this.fuelList.stream().filter(x -> x != null).map(FluidStack::getFluid)
                .collect(Collectors.toList());

        Optional<LiquidRocketFuelEntry> entry = LiquidRocketFuelEntry.search(userFluids);
        this.fuel = entry.orElse(null);
    }

    public boolean isSolidBlueprint() {
        AbstractRocketBlueprint bp = getBlueprint();
        return bp != null && bp.isSolidRocket();
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
        return this.isWorkingEnabled;
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.isWorkingEnabled;
    }

    protected void setWorkingEnabledInternal(boolean isWorkingAllowed) {
        this.isWorkingEnabled = isWorkingAllowed;
        markDirty();
        World world = getWorld();
        if (world != null && !world.isRemote) {
            writeCustomData(GregtechDataCodes.WORKING_ENABLED, buf -> buf.writeBoolean(isWorkingEnabled));
        }
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        // May or may not go
        if (isWorkingAllowed) {
            this.start();
        } else {
            this.stop();
        }
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.WORKING_ENABLED) {
            this.setWorkingEnabledInternal(buf.readBoolean());
        }
        if (dataId == GregtechDataCodes.LOCK_OBJECT_HOLDER) {
            this.rocketBlueprintSlot.setLocked(buf.readBoolean());
        }
        if (dataId == SYNC_AFS) {
            this.computationPerTick = buf.readInt();
            this.coolantPerTick = buf.readInt();
            this.energyPerTick = buf.readInt();
            this.progress = buf.readLong();
            this.stats = AFSStats.readFromBuffer(buf);
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

        crash();
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

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityAerospaceFlightSimulator(metaTileEntityId);
    }

    // doesnt check if the blueprint itself is complete but that should go onto the
    // slot check
    public boolean hasBlueprint() {
        return !this.rocketBlueprintSlot.isEmpty() && this.rocketBlueprintSlot.getStackInSlot(0).hasTagCompound() &&
                this.rocketBlueprintSlot.getStackInSlot(0)
                        .getMetadata() == SuSyMetaItems.DATA_CARD_MASTER_BLUEPRINT.metaValue;
    }

    @Nullable public AbstractRocketBlueprint getBlueprint() {
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
        setWorkingEnabledInternal(false);
        this.rocketBlueprintSlot.setLocked(false);
        AbstractRocketBlueprint bp = getBlueprint();
        if (bp == null || !bp.isFullBlueprint()) {
            SusyLog.logger.info("bp == {}", bp);
            return;
        }
        if (bp instanceof IAFSImprovable bp2) {
            bp2.setAFSImprovement(this.progress);
        }

        this.rocketBlueprintSlot.setNBT((ignored) -> {
            NBTTagCompound n = bp.writeToNBT();
            n.setBoolean("buildstat", true);
            return n;
        });
        this.progress = 0;
    }

    // wipe the progress when there is not enough power/coolant to prevent the
    // player from having too much fun
    public void crash() {
        setWorkingEnabledInternal(false);
        this.rocketBlueprintSlot.setLocked(false);
        this.progress = 0;
        this.hasNotEnoughEnergy = false;
        this.hasNotEnoughCoolant = false;
    }

    public void start() {
        if (this.hasBlueprint() && !this.isActive() && this.fuel != null) {
            int energyToConsume = getEnergyToConsume();
            boolean maintenance = ConfigHolder.machines.enableMaintenance && hasMaintenanceMechanics();
            if (maintenance) {
                energyToConsume += getNumMaintenanceProblems() * energyToConsume / 10;
            }
            int coolantToConsume = getCoolantToConsume();
            FluidStack drained = inputCoolant.drain(new FluidStack(COOLANT_IN, coolantToConsume), false);
            boolean enoughCoolant = drained != null && drained.amount == coolantToConsume;
            boolean enoughSpaceForCoolant = outputCoolant.fill(new FluidStack(COOLANT_OUT, coolantToConsume),
                    false) == coolantToConsume;
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
                var bp = this.getBlueprint();
                if (bp instanceof IAFSImprovable bp2) {
                    this.progress = bp2.getAFSImprovement();
                    setWorkingEnabledInternal(true);
                    this.rocketBlueprintSlot.setLocked(true);
                }
            }
        }
    }

    @Override
    public boolean isStructureObstructed() {
        return super.isStructureObstructed() || !coolantFilled;
    }

    @Override
    protected ICubeRenderer getFrontOverlay() {
        return SusyTextures.AFS_OVERLAY;
    }

    protected int getEnergyToConsume() {
        return energyPerTick;
    }

    // per tick
    protected int getCompute() {
        return computationPerTick;
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.energyContainer = new EnergyContainerList(getAbilities(MultiblockAbility.INPUT_ENERGY));
        this.inputCoolant = new FluidTankList(true, getAbilities(MultiblockAbility.IMPORT_FLUIDS));
        this.outputCoolant = new FluidTankList(true, getAbilities(MultiblockAbility.EXPORT_FLUIDS));

        this.coolantPositions = context.getOrDefault(FLUID_BLOCKS_KEY, new ArrayList<>());
        this.coolantFilled = coolantPositions.isEmpty();
        this.computationPerTick = context.getOrDefault("computation", 0);
        this.coolantPerTick = context.getOrDefault("coolant", 0);
        this.energyPerTick = context.getOrDefault("energy", 0);
        sendComputationInfoToClient();
    }

    private void sendComputationInfoToClient() {
        this.writeCustomData(SYNC_AFS, (buf) -> {
            buf.writeInt(this.computationPerTick);
            buf.writeInt(this.coolantPerTick);
            buf.writeInt(this.energyPerTick);
            buf.writeLong(this.progress);
            this.stats.writeToBuffer(buf);
        });
    }

    @Override
    protected void addErrorText(List<ITextComponent> textList) {
        super.addErrorText(textList);
        if (hasNotEnoughCoolant) {
            textList.add(new TextComponentTranslation(this.getMetaName() + ".gui.no_coolant_warning"));
        }
        if (isStructureFormed() && !coolantFilled) {
            textList.add(
                    TextComponentUtil.translationWithColor(TextFormatting.RED, this.getMetaName() + ".obstructed"));
            textList.add(TextComponentUtil.translationWithColor(TextFormatting.GRAY,
                    this.getMetaName() + ".obstructed.desc"));
        }
    }

    // mb/tick
    protected int getCoolantToConsume() {
        return coolantPerTick;
    }

    @Override
    protected void updateFormedValid() {
        if (!coolantFilled && getOffsetTimer() % 5 == 0) {
            fillCoolant(this.coolantPositions, SusyMaterials.FC75.getFluid(), inputCoolant);
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
        int coolantToConsume = getCoolantToConsume();
        FluidStack drainedFluid = inputCoolant.drain(new FluidStack(COOLANT_IN, coolantToConsume), false);
        boolean enoughCoolant = false;
        if (drainedFluid != null) {
            enoughCoolant = drainedFluid.amount == coolantToConsume;
        }
        boolean enoughSpaceForCoolant = outputCoolant.fill(new FluidStack(COOLANT_OUT, coolantToConsume),
                false) == coolantToConsume;
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
                this.progress += this.getCompute();
            } else {
                hasNotEnoughEnergy = true;
                crash();
            }
        } else {
            hasNotEnoughEnergy = true;
            crash();
        }
        if (getOffsetTimer() % 100 == 0) {
            this.stats = this.getBlueprint().calculateInitialSuccess(this.planet, this.fuel,
                    this.turnAltitude, this.cargoMass, this.progress);

            sendComputationInfoToClient();
        }
    }

    @Override
    public void update() {
        super.update();
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
                .where('P', SuSyPredicates.computation())
                .where('T', states(MetaBlocks.TRANSPARENT_CASING.getState(BlockGlassCasing.CasingType.TEMPERED_GLASS)))
                .where('F', fluid(SusyMaterials.FC75.getFluid()))
                .where('I', abilities(MultiblockAbility.IMPORT_FLUIDS).setMaxGlobalLimited(1).setMinGlobalLimited(1, 1)
                        .or(abilities(MultiblockAbility.EXPORT_FLUIDS).setMinGlobalLimited(1).setMaxGlobalLimited(1, 1))
                        .or(abilities(MultiblockAbility.INPUT_ENERGY).setMaxGlobalLimited(2).setMinGlobalLimited(1, 1)
                                .or(states(getCasingState()))
                                .or(maintenancePredicate().setMaxGlobalLimited(1).setMinGlobalLimited(1, 1)))
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
        int width = 320;
        int height = 210;

        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, width, height);
        builder.image(4, 4, width - 8, height - 91, GuiTextures.DISPLAY);
        builder.widget(new IndicatorImageWidget(width - 24, height / 2 - 3, 17, 17, getLogo())
                .setWarningStatus(getWarningLogo(), this::addWarningText)
                .setErrorStatus(getErrorLogo(), this::addErrorText));
        builder.widget(new ImageCycleButtonWidget(width - 42, height - 23, 18, 18, GuiTextures.BUTTON_POWER,
                this::isWorkingEnabled, this::setWorkingEnabled));
        builder.widget(new ImageWidget(width - 42, height - 5, 18, 6, GuiTextures.BUTTON_POWER_DETAIL));
        builder.bindPlayerInventory(entityPlayer.inventory, 125);

        // blueprint slot
        SlotWidgetMentallyStable blueprintSlot = new SlotWidgetMentallyStable(this.rocketBlueprintSlot, 0, 0, 0);

        blueprintSlot.setSelfPosition(new Position(width - 23, height - 23));
        blueprintSlot.setBackgroundTexture(GuiTextures.SLOT_DARK);
        blueprintSlot.setChangeListener(() -> {
            if (this.rocketBlueprintSlot.isEmpty()) {
                this.stats = AFSStats.none();
            }
        });
        builder.widget(blueprintSlot);

        ConditionalWidget mainGroup = new ConditionalWidget(0, 0, width, height, () -> true);
        ConditionalWidget menuGroup = new ConditionalWidget(0, 0, width, height, () -> !this.isActive());
        ConditionalWidget workingGroup = new ConditionalWidget(0, 0, width, height, this::isActive);
        // planet selector
        menuGroup.addWidget(
                new LabelWidget(105, 45, this.getMetaName() + ".gui.planet_selector_label", 0xffffff));
        PhantomSlotWidget planetSlot = new PhantomSlotWidget(this.planetSlot, 0, 0, 0);

        planetSlot.setSelfPosition(new Position(105, 54));
        planetSlot.setBackgroundTexture(GuiTextures.SLOT_DARK);
        menuGroup.addWidget(planetSlot);

        mainGroup.addWidget(menuGroup);
        mainGroup.addWidget(workingGroup);
        // Fuel selector
        menuGroup.addWidget(
                new LabelWidget(10, 45, this.getMetaName() + ".gui.fuel_selector_label", 0xffffff));
        // liquid and solid blueprints each get their own selector; the hidden one keeps
        // its contents so switching blueprints back restores the old selection
        menuGroup.addWidgetWithTest(
                new FuelRegistrySelectorWidget(10, 54, 80, 60, this.fuelList, (fuel) -> setFuelFromData()),
                () -> !this.isSolidBlueprint() && !this.isActive());
        menuGroup.addWidgetWithTest(
                new PhantomSlotWidget(this.solidFuelSlot, 0, 10, 54)
                        .setClearSlotOnRightClick(true)
                        .setBackgroundTexture(GuiTextures.SLOT_DARK),
                () -> this.isSolidBlueprint() && !this.isActive());
        // Turn altitude selector
        menuGroup.addWidget(
                new LabelWidget(10, 80, this.getMetaName() + ".gui.turn_altitude_selector_label", 0xffffff));
        menuGroup.addWidget(
                new TextFieldWidget2(10, 88, 60, 12, () -> Double.valueOf(turnAltitude).toString(), value -> {
                    if (!value.isEmpty()) {
                        try {
                            turnAltitude = Double.parseDouble(value);
                            if (turnAltitude <= 0) {
                                turnAltitude = 50;
                            }
                        } catch (NumberFormatException ignored) {
                            turnAltitude = 50;
                        }
                    }
                }).setAllowedChars(TextFieldWidget2.DECIMALS).setMaxLength(6));
        // Cargo mass selector
        menuGroup.addWidget(
                new LabelWidget(10, 96, this.getMetaName() + ".gui.cargo_mass_selector_label", 0xffffff));
        menuGroup.addWidget(
                new TextFieldWidget2(10, 104, 60, 12, () -> Double.valueOf(cargoMass).toString(), value -> {
                    if (!value.isEmpty()) {
                        try {
                            cargoMass = Double.parseDouble(value);
                            if (cargoMass <= 0) {
                                cargoMass = 0;
                            }
                        } catch (NumberFormatException ignored) {
                            cargoMass = 0;
                        }
                    }
                }).setAllowedChars(TextFieldWidget2.DECIMALS).setMaxLength(9));
        menuGroup.addWidgetWithTest(new AdvancedTextWidget(9, 19, (l) -> {
            AbstractRocketBlueprint bp = this.getBlueprint();
            if (this.hasBlueprint() && bp != null) {
                l.add(new TextComponentTranslation(this.getMetaName() + ".gui.rocket_name",
                        new TextComponentTranslation("susy.rocketry." + bp.name + ".name")));

            }
        }, 0xffffff), () -> !this.isActive() && this.hasBlueprint());

        // multi information
        // these should probably be visible at all times in some different corner
        menuGroup.addWidget(new LabelWidget(width - 170, 9,
                getMetaName() + ".gui.computation_power", 0xffffff, new Object[] { this.getCompute() }));
        menuGroup.addWidget(new LabelWidget(width - 170, 20,
                getMetaName() + ".gui.coolant_flow", 0xffffff, new Object[] { this.getCoolantToConsume() * 20 }));
        menuGroup.addWidget(new DynamicLabelWidget(width - 170, 31,
                () -> I18n.format(getMetaName() + ".gui.energy_consumption", this.getEnergyToConsume()), 0xffffff));
        menuGroup.addWidget(new LabelWidget(9, 9, getMetaFullName(), 0xffffff));

        menuGroup.addWidgetWithTest(
                new LabelWidget(9, height - 80, this.getMetaName() + "gui.cant_improve_error", 0xff0000),
                () -> this.hasBlueprint() && !(this.getBlueprint() instanceof IAFSImprovable));
        // rocket render

        mainGroup.addWidgetConditionalInit(() -> {
            AbstractRocketBlueprint bp = this.getBlueprint();
            if (this.hasBlueprint() && bp != null && bp.isFullBlueprint() && this.isActive()) {
                return true;
            }
            return false;
        }, () -> {
            AbstractRocketBlueprint bp = this.getBlueprint();
            if (bp != null && bp.isFullBlueprint()) {
                ResourceLocation entity_res = bp.relatedEntity;
                DummyWorld world = new DummyWorld();
                Entity rocketentity = this.createEntityByResource(entity_res, world);
                rocketentity.setPosition(0, 0, 0);
                return new RocketRenderWidget(new Size(width - 15, 100), new Position(7, 11), rocketentity);
            }
            SusyLog.logger.fatal("Somehow the blueprint wasn't a full blueprint? bp:{}",
                    bp == null ? "null" : bp.writeToNBT());
            return null;
        });
        builder.widget(mainGroup);
        // Various stats beneath
        workingGroup.addWidgetWithTest(new DynamicLabelWidget(10, 8,
                () -> I18n.format(getMetaName() + ".gui.success_chance",
                        String.format("%.2f%%", 100 * this.stats.success())),
                0x00eeff),
                () -> this.isActive() && !this.stats.isNone());
        workingGroup.addWidgetWithTest(new DynamicLabelWidget(10, 19,
                () -> I18n.format(getMetaName() + ".gui.mass",
                        String.format("%.0f", this.stats.mass())),
                0xffffff),
                () -> this.isActive() && !this.stats.isNone() && this.fuel != null);
        workingGroup.addWidgetWithTest(new DynamicLabelWidget(10, 30,
                () -> I18n.format(getMetaName() + ".gui.fuel_mass",
                        String.format("%.0f", this.stats.fuelMass())),
                0xffffff),
                () -> this.isActive() && !this.stats.isNone() && this.fuel != null);
        workingGroup.addWidgetWithTest(new DynamicLabelWidget(10, 41,
                () -> I18n.format(getMetaName() + ".gui.delta_v",
                        String.format("%.2f", this.stats.deltaV())),
                0xffffff),
                () -> this.isActive() && !this.stats.isNone() && this.fuel != null);
        workingGroup.addWidgetWithTest(new DynamicLabelWidget(10, 52,
                () -> I18n.format(getMetaName() + ".gui.drag_coefficient",
                        String.format("%.2f", this.stats.dragCoefficient())),
                0xffffff),
                () -> this.isActive() && !this.stats.isNone() && this.fuel != null);
        workingGroup.addWidgetWithTest(new DynamicLabelWidget(10, 63,
                () -> I18n.format(getMetaName() + ".gui.first_sep_altitude",
                        String.format("%.2f", this.stats.firstSepAltitude() / 1000)),
                0xffffff),
                () -> this.isActive() && !this.stats.isNone() && this.fuel != null);
        workingGroup.addWidgetWithTest(new DynamicLabelWidget(10, 74,
                () -> I18n.format(getMetaName() + ".gui.first_sep_time",
                        String.format("%.2f", this.stats.firstSepTime())),
                0xffffff),
                () -> this.isActive() && !this.stats.isNone() && this.fuel != null);
        workingGroup.addWidgetWithTest(new DynamicLabelWidget(10, 85,
                () -> I18n.format(getMetaName() + ".gui.second_sep_altitude",
                        String.format("%.2f", this.stats.secondSepAltitude() / 1000)),
                0xffffff),
                () -> this.isActive() && !this.stats.isNone() && this.fuel != null);
        workingGroup.addWidgetWithTest(new DynamicLabelWidget(10, 96,
                () -> I18n.format(getMetaName() + ".gui.second_sep_time",
                        String.format("%.2f", this.stats.secondSepTime())),
                0xffffff),
                () -> this.isActive() && !this.stats.isNone() && this.fuel != null);
        workingGroup.addWidgetWithTest(new DynamicLabelWidget(width - 170, 8,
                () -> I18n.format(getMetaName() + ".gui.third_sep_altitude",
                        String.format("%.2f", this.stats.thirdSepAltitude() / 1000)),
                0xffffff),
                () -> this.isActive() && !this.stats.isNone() && this.fuel != null);
        workingGroup.addWidgetWithTest(new DynamicLabelWidget(width - 170, 19,
                () -> I18n.format(getMetaName() + ".gui.third_sep_time",
                        String.format("%.2f", this.stats.thirdSepTime())),
                0xffffff),
                () -> this.isActive() && !this.stats.isNone() && this.fuel != null);
        workingGroup.addWidgetWithTest(new DynamicLabelWidget(width - 170, 30,
                () -> I18n.format(getMetaName() + ".gui.burnout_speed",
                        String.format("%.2f", this.stats.burnoutSpeed())),
                0xffffff),
                () -> this.isActive() && !this.stats.isNone() && this.fuel != null);
        workingGroup.addWidgetWithTest(new DynamicLabelWidget(width - 170, 41,
                () -> I18n.format(getMetaName() + ".gui.burnout_horizontal_speed",
                        String.format("%.2f", this.stats.burnoutHorizontalSpeed())),
                0xffffff),
                () -> this.isActive() && !this.stats.isNone() && this.fuel != null);
        workingGroup.addWidgetWithTest(new DynamicLabelWidget(width - 170, 52,
                () -> I18n.format(getMetaName() + ".gui.improvement", this.getAugmentation()), 0x00eeff),
                () -> this.isActive() && !this.stats.isNone() && this.fuel != null);
        return builder;
    }

    public long getAugmentation() {
        return this.progress;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(),
                this.isStructureFormed(), true);
    }
}
