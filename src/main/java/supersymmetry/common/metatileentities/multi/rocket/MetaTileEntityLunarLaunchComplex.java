package supersymmetry.common.metatileentities.multi.rocket;

import java.util.*;
import java.util.stream.Collectors;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

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
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.Recipe;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
import supersymmetry.api.SusyLog;
import supersymmetry.api.capability.SuSyDataCodes;
import supersymmetry.api.gui.SusyGuiTextures;
import supersymmetry.api.items.CargoItemStackHandler;
import supersymmetry.api.metatileentity.multiblock.IRedstoneControllable;
import supersymmetry.api.metatileentity.multiblock.IRocketAssemblyController;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.api.recipes.logic.RocketAssemblerLogic;
import supersymmetry.api.rocketry.components.AbstractComponent;
import supersymmetry.api.rocketry.fuels.RocketFuelEntry;
import supersymmetry.api.rocketry.rockets.AbstractRocketBlueprint;
import supersymmetry.api.space.CelestialObjects;
import supersymmetry.api.util.DataStorageLoader;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.BlockLunarConcrete;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.entities.EntityLunarRocket;
import supersymmetry.common.item.SuSyMetaItems;
import supersymmetry.common.metatileentities.multiblockpart.MetaTileEntityComponentRedstoneController;
import supersymmetry.common.mui.widget.ItemCostWidget;
import supersymmetry.common.mui.widget.SlotWidgetMentallyStable;
import supersymmetry.common.rocketry.RocketConfigurerHandler;
import supersymmetry.common.rocketry.SusyRocketComponents;

/**
 * The rocket assembler and launch pad rolled into one. Lunar gravity is weak
 * enough that the rocket does not need the transporter erector and the separate
 * pad the Soyuz does, so this multiblock builds the rocket component by
 * component (using {@link RocketAssemblerLogic}, exactly like
 * {@link MetaTileEntityRocketAssembler}), spawns it in place once assembly
 * finishes, then fuels, loads and launches it.
 */
public class MetaTileEntityLunarLaunchComplex extends RecipeMapMultiblockController
                                              implements
                                              IProgressBarMultiblock,
                                              IRedstoneControllable,
                                              IRocketAssemblyController {

    /** In liters per second, matching the launch pad. */
    private static final int MAX_FUELING_SPEED = 8000;

    private static final int ROCKET_OFFSET_BACK = 4;
    private static final int ROCKET_OFFSET_UP = 1;

    public DataStorageLoader blueprintSlot = new DataStorageLoader(this, x -> {
        if (x.hasTagCompound()) {
            NBTTagCompound tag = x.getTagCompound();
            AbstractRocketBlueprint bp = AbstractRocketBlueprint.getCopyOf(tag.getString("name"));
            return bp != null && bp.readFromNBT(tag) &&
                    bp.getName().equals(SusyRocketComponents.ROCKET_LUNAR_BLUEPRINT_DEFAULT.getName()) &&
                    bp.isFullBlueprint();
        }
        return false;
    });

    // Every component that has to be constructed, and how far through that list we
    // are.
    public List<AbstractComponent<?>> componentList = new ArrayList<>();
    public int componentIndex = 0;
    public boolean isAssemblyWorking = false;

    private final List<Runnable> signalActions = new ArrayList<>();

    private LaunchComplexState state = LaunchComplexState.IDLE;
    private EntityLunarRocket selectedRocket;
    private AxisAlignedBB rocketAABB;
    private int fuelingProgress;

    /**
     * Optional. Nothing rolls through a rocket programmer on the way here, so this
     * is the only way to give a rocket built by the complex a mission list.
     */
    public final RocketConfigurerHandler configurerSlot = new RocketConfigurerHandler(this);
    private boolean configWithinBudget = true;

    /**
     * Latched by the launch redstone signal. The front face signal is polled
     * directly instead, so it does not need latching.
     */
    private boolean launchRequested;

    public MetaTileEntityLunarLaunchComplex(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.ROCKET_ASSEMBLER);
        signalActions.add(() -> {
            if (!this.blueprintSlot.isEmpty() && this.componentList.isEmpty()) {
                this.startAssembly(this.getCurrentBlueprint());
            }
        });
        signalActions.add(this::abortAssembly);
        signalActions.add(() -> {
            if (this.state == LaunchComplexState.LOADED) {
                this.launchRequested = true;
            }
        });
        this.recipeMapWorkable = new RocketAssemblerLogic(this);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityLunarLaunchComplex(this.metaTileEntityId);
    }

    // --- Assembly
    // ------------------------------------------------------------------------------------------------

    public AbstractRocketBlueprint getCurrentBlueprint() {
        if (blueprintSlot.isEmpty())
            return null;
        NBTTagCompound tag = blueprintSlot.getStackInSlot(0).getTagCompound();
        AbstractRocketBlueprint bp = AbstractRocketBlueprint.getCopyOf(tag.getString("name"));
        if (bp != null && bp.readFromNBT(tag)) {
            return bp;
        }
        SusyLog.logger.error("failed to read a blueprint {}", tag);
        return null;
        // hopefully never happens since its checked when the item is inserted
    }

    public Recipe getCurrentRecipe() {
        return isAssemblyWorking ? ((RocketAssemblerLogic) this.recipeMapWorkable).getRecipe(100000) : null;
    }

    public void startAssembly(AbstractRocketBlueprint bp) {
        // Only one rocket fits on the pad at a time, so the previous one has to have
        // flown before we build another.
        if (bp == null || this.state != LaunchComplexState.IDLE || checkRocket() || findRocket())
            return;

        ((RocketAssemblerLogic) this.recipeMapWorkable).setInputsValid();
        this.componentIndex = 0;
        this.isAssemblyWorking = true;
        this.componentList = bp.getStages().stream().flatMap(x -> x.getComponents().values().stream())
                .flatMap(List::stream).collect(Collectors.toList());
        this.blueprintSlot.setLocked(true);
        setComplexState(LaunchComplexState.ASSEMBLING);
    }

    public void abortAssembly() {
        this.blueprintSlot.setLocked(false);
        this.isAssemblyWorking = false;
        this.componentIndex = 0;
        this.componentList.clear();
        if (this.state == LaunchComplexState.ASSEMBLING) {
            setComplexState(LaunchComplexState.IDLE);
        }
        this.recipeMapWorkable.invalidate(); // this can break some things
    }

    @Override
    public void finishAssembly() {
        AbstractRocketBlueprint blueprint = getCurrentBlueprint();
        this.blueprintSlot.setLocked(false);
        this.isAssemblyWorking = false;
        this.componentIndex = 0;
        this.componentList.clear();

        if (blueprint == null) {
            setComplexState(LaunchComplexState.IDLE);
            return;
        }
        NBTTagCompound rocketNBT = new NBTTagCompound();
        // The complex assembles and launches from the same spot by design, so it must
        // not be flagged as the
        // "launched while still on the assembler" failure case that EntitySoyuzBasic
        // checks for.
        rocketNBT.setLong("assemblerPosition", BlockPos.ORIGIN.toLong());
        rocketNBT.setTag("rocket", blueprint.writeToNBT());
        spawnRocket(rocketNBT);
        setFuelingProgress(0);
        setComplexState(LaunchComplexState.LOADED);
    }

    @Override
    public AbstractComponent<?> getCurrentCraftTarget() {
        if (isAssemblyWorking && componentList.size() >= componentIndex + 1) {
            return this.componentList.get(this.componentIndex);
        } else {
            abortAssembly();
        }
        return null;
    }

    // meant to be called after a recipe is done
    @Override
    public void nextComponent() {
        if (!isAssemblyWorking)
            return;
        this.componentIndex++;
    }

    @Override
    public boolean isAssemblyWorking() {
        return this.isAssemblyWorking;
    }

    @Override
    public int getComponentIndex() {
        return this.componentIndex;
    }

    @Override
    public int getComponentCount() {
        return this.componentList.size();
    }

    /**
     * The pad is the assembly site, and it is only clear while we are assembling —
     * entering any later state means a rocket is standing on it.
     */
    @Override
    public boolean isAssemblySiteAvailable() {
        return this.state == LaunchComplexState.ASSEMBLING;
    }

    @Override
    public boolean isAssemblySiteReady() {
        return this.state == LaunchComplexState.ASSEMBLING;
    }

    // --- The rocket
    // ----------------------------------------------------------------------------------------------

    public Vec3d getLaunchPosition() {
        BlockPos base = getPos().offset(getFrontFacing().getOpposite(), ROCKET_OFFSET_BACK).up(ROCKET_OFFSET_UP);
        return new Vec3d(base.getX() + 0.5, base.getY(), base.getZ() + 0.5);
    }

    public AxisAlignedBB getRocketAABB() {
        Vec3d launchPosition = getLaunchPosition();
        return new AxisAlignedBB(new BlockPos(launchPosition)).grow(3, 0, 3).expand(0, 40, 0);
    }

    public void spawnRocket(NBTTagCompound tag) {
        Vec3d position = getLaunchPosition();
        this.selectedRocket = new EntityLunarRocket(getWorld(), position, getFrontFacing().getHorizontalAngle());
        if (tag != null) {
            // Copy in all tags
            for (Map.Entry<String, NBTBase> info : tag.tagMap.entrySet()) {
                this.selectedRocket.getEntityData().setTag(info.getKey(), info.getValue());
            }
        }
        getWorld().spawnEntity(this.selectedRocket);
    }

    /**
     * Looks for a rocket already standing on the pad, e.g. after a world reload.
     */
    private boolean findRocket() {
        if (getWorld() == null || this.rocketAABB == null)
            return false;
        List<EntityLunarRocket> rockets = getWorld().getEntitiesWithinAABB(EntityLunarRocket.class, this.rocketAABB);
        if (rockets.isEmpty())
            return false;
        this.selectedRocket = rockets.get(0);
        return true;
    }

    public boolean checkRocket() {
        return this.selectedRocket != null && !this.selectedRocket.isDead;
    }

    // --- State machine
    // -------------------------------------------------------------------------------------------

    @Override
    protected void updateFormedValid() {
        super.updateFormedValid(); // drives the assembly recipe logic
        if (getWorld().isRemote)
            return;

        switch (this.state) {
            case IDLE:
                // Nothing is being built and nothing is on the pad. Sweep occasionally in case
                // a rocket outlived a
                // reload, or was spawned by something other than us.
                if (getOffsetTimer() % 20 == 0 && findRocket()) {
                    setComplexState(LaunchComplexState.LOADED);
                }
                break;
            case ASSEMBLING:
                // RocketAssemblerLogic drives this; finishAssembly() takes us to LOADED.
                if (!this.isAssemblyWorking) {
                    setComplexState(LaunchComplexState.IDLE);
                }
                break;
            case LOADED:
                if (!checkRocket() && !findRocket()) {
                    setFuelingProgress(0);
                    setComplexState(LaunchComplexState.IDLE);
                    break;
                }
                if (getOffsetTimer() % 4 == 0) {
                    setConfigWithinBudget(this.configurerSlot.program(this.selectedRocket));
                }
                if (!loadCargo() || !isLaunchAuthorized()) {
                    break;
                }
                setComplexState(LaunchComplexState.LAUNCHING);
                // fall through — start the countdown on the same tick
            case LAUNCHING:
                if (!checkRocket() && !findRocket()) {
                    setComplexState(LaunchComplexState.IDLE);
                    break;
                }
                if (!this.selectedRocket.isCountdownStarted()) {
                    this.selectedRocket.startCountdown(200);
                }
                if (this.selectedRocket.posY > getLaunchPosition().y + 40) {
                    this.selectedRocket = null;
                    this.launchRequested = false;
                    setFuelingProgress(0);
                    setComplexState(LaunchComplexState.IDLE);
                }
                break;
        }
    }

    private boolean isLaunchAuthorized() {
        return this.launchRequested || getInputRedstoneSignal(getFrontFacing(), false) != 0;
    }

    @Override
    protected boolean canMachineConnectRedstone(EnumFacing side) {
        return side == getFrontFacing();
    }

    /**
     * Moves everything in the import buses that is not an assembly consumable into
     * the rocket, then tops up its fuel.
     *
     * @return true once the rocket is fully fuelled
     */
    private boolean loadCargo() {
        loadRocketCargo();
        if (this.fuelingProgress >= this.selectedRocket.getFuelVolume()) {
            return true;
        }
        RocketFuelEntry fuelEntry = this.selectedRocket.getFuel();

        if (fuelEntry == null) {
            List<Fluid> fluids = getInputFluidInventory().getFluidTanks().stream()
                    .map(tank -> tank.getFluid() == null ? null : tank.getFluid().getFluid()).distinct()
                    .filter(Objects::nonNull).collect(Collectors.toList());

            Optional<RocketFuelEntry> possibleEntry = RocketFuelEntry.search(fluids);
            if (possibleEntry.isEmpty()) {
                return false;
            }
            fuelEntry = possibleEntry.get();
            this.selectedRocket.setFuel(fuelEntry);
        }

        var composition = fuelEntry.getComposition();
        int totalMBPerUnit = composition.stream().mapToInt(Tuple::getSecond).sum();
        if (totalMBPerUnit <= 0)
            return false;
        // Round up so the last, partial unit still finishes the tank off.
        int remaining = this.selectedRocket.getFuelVolume() + totalMBPerUnit - 1 - this.fuelingProgress;
        int unitsDrained = Math.min(remaining / totalMBPerUnit, MAX_FUELING_SPEED / totalMBPerUnit);
        for (var comp : composition) {
            FluidStack drained = getInputFluidInventory().drain(new FluidStack(comp.getFirst(), MAX_FUELING_SPEED),
                    false);
            if (drained == null)
                return false;
            // Intentional integer division moment
            unitsDrained = Math.min(drained.amount / comp.getSecond(), unitsDrained);
        }
        if (unitsDrained > 0) {
            for (var comp : composition) {
                getInputFluidInventory().drain(new FluidStack(comp.getFirst(), comp.getSecond() * unitsDrained), true);
            }
            setFuelingProgress(this.fuelingProgress + (unitsDrained * totalMBPerUnit));
        }

        return this.fuelingProgress >= this.selectedRocket.getFuelVolume();
    }

    private void loadRocketCargo() {
        CargoItemStackHandler cargo = this.selectedRocket.getInventory();
        if (cargo == null)
            return;
        IItemHandlerModifiable imports = getInputInventory();
        for (int i = 0; i < imports.getSlots(); i++) {
            ItemStack stack = imports.getStackInSlot(i);
            if (stack.isEmpty() || isAssemblyConsumable(stack))
                continue;
            imports.setStackInSlot(i, ItemHandlerHelper.insertItemStacked(cargo, stack, false));
        }
    }

    /**
     * Electrodes are spent building the rocket, not flown in it, so they stay
     * behind for the next one.
     */
    private static boolean isAssemblyConsumable(ItemStack stack) {
        return SuSyMetaItems.TUNGSTEN_ELECTRODE.getStackForm().isItemEqual(stack);
    }

    private void setComplexState(LaunchComplexState state) {
        if (this.state != state) {
            this.state = state;
            writeCustomData(SuSyDataCodes.UPDATE_RENDER_STATE, buf -> buf.writeEnumValue(state));
        }
    }

    private void setFuelingProgress(int fuelingProgress) {
        this.fuelingProgress = fuelingProgress;
        writeCustomData(SuSyDataCodes.UPDATE_FUEL_PROGRESS, buf -> buf.writeInt(fuelingProgress));
    }

    private void setConfigWithinBudget(boolean withinBudget) {
        if (this.configWithinBudget != withinBudget) {
            this.configWithinBudget = withinBudget;
            writeCustomData(SuSyDataCodes.UPDATE_CAN_HANDLE_FULL_CONFIG, buf -> buf.writeBoolean(withinBudget));
        }
    }

    // --- Redstone
    // ------------------------------------------------------------------------------------------------

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

    // --- Structure
    // -----------------------------------------------------------------------------------------------

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        if (this.getWorld().provider.getDimension() != CelestialObjects.MOON.getDimension()) {
            invalidateStructure();
            return;
        }
        this.rocketAABB = getRocketAABB();
        if (findRocket()) {
            setComplexState(LaunchComplexState.LOADED);
        }
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.rocketAABB = null;
        this.selectedRocket = null;
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        // Placeholder geometry: a flat 9x9 pad with the controller centred on the front
        // edge, sized only to host the
        // abilities. Replace with the real complex, and retune ROCKET_OFFSET_BACK/UP to
        // match.
        String edgee = "EEEEEEEEE";
        String floor = "ECCCCCCCE";
        String floh1 = "ECCC CCCE";
        String floh2 = "ECC   CCE";
        String side1 = "E       E";
        String cran1 = "   TGT   ";
        String cran2 = "   TFT   ";
        String cran3 = "    F    ";
        String airrr = "         ";
        String selfp = "EEEESEEEE";
        return FactoryBlockPattern.start()
                .aisle(airrr, airrr, cran1, cran2, cran2, cran2, cran2, cran2, cran2, cran1, airrr, airrr)
                .aisle(edgee, edgee, cran1, cran2, cran2, cran2, cran2, cran2, cran2, airrr, cran1, airrr)
                .aisle(floor, side1, airrr, airrr, airrr, airrr, airrr, airrr, airrr, airrr, airrr, cran1)
                .aisle(floor, side1, airrr, airrr, airrr, airrr, airrr, cran1, cran3, cran3, cran3, cran2)
                .aisle(floh1, side1, airrr, airrr, airrr, airrr, airrr, airrr, airrr, airrr, airrr, airrr)
                .aisle(floh2, side1, airrr, airrr, airrr, airrr, airrr, airrr, airrr, airrr, airrr, airrr)
                .aisle(floh1, side1, airrr, airrr, airrr, airrr, airrr, airrr, airrr, airrr, airrr, airrr)
                .aisle(floor, side1, airrr, airrr, airrr, airrr, airrr, airrr, airrr, airrr, airrr, airrr)
                .aisle(floor, side1, airrr, airrr, airrr, airrr, airrr, airrr, airrr, airrr, airrr, airrr)
                .aisle(selfp, edgee, airrr, airrr, airrr, airrr, airrr, airrr, airrr, airrr, airrr, airrr)
                .where('S', selfPredicate()).where('C', states(getFoundationState()))
                .where('E', states(getFoundationState()).setMinGlobalLimited(30).or(autoAbilities())
                        .or(MetaTileEntityComponentRedstoneController.controllerPredicate().setMaxGlobalLimited(2))
                        .or(abilities(MultiblockAbility.IMPORT_ITEMS).setPreviewCount(1).setMinGlobalLimited(1)
                                .setMaxGlobalLimited(2))
                        .or(abilities(MultiblockAbility.IMPORT_FLUIDS).setPreviewCount(1).setMinGlobalLimited(1)
                                .setMaxGlobalLimited(4)))
                .where('T', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.TITANIUM_STABLE)))
                .where('G',
                        states(MetaBlocks.TURBINE_CASING
                                .getState(BlockTurbineCasing.TurbineCasingType.TITANIUM_GEARBOX)))
                .where('F', frames(Materials.Titanium)).build();
    }

    public IBlockState getFoundationState() {
        return SuSyBlocks.LUNAR_CONCRETE.getState(BlockLunarConcrete.LunarConcreteType.LUNAR_CONCRETE_SMOOTH);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.STABLE_TITANIUM_CASING;
    }

    @NonNull @Override
    protected ICubeRenderer getFrontOverlay() {
        return SusyTextures.LAUNCH_PAD_OVERLAY;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(), isStructureFormed(),
                true);
    }

    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }

    @Override
    protected boolean shouldShowVoidingModeButton() {
        return false;
    }

    // --- Persistence and sync
    // ------------------------------------------------------------------------------------

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data = super.writeToNBT(data);
        data.setTag("blueprint", this.blueprintSlot.getStackInSlot(0).writeToNBT(new NBTTagCompound()));
        data.setBoolean("isWorking", this.isAssemblyWorking);
        data.setInteger("componentIndex", this.componentIndex);
        data.setString("state", this.state.name());
        data.setInteger("fuelingProgress", this.fuelingProgress);
        data.setTag("configurer", this.configurerSlot.serializeNBT());
        return data;
    }

    @Override
    public void clearMachineInventory(NonNullList<ItemStack> itemBuffer) {
        super.clearMachineInventory(itemBuffer);
        clearInventory(itemBuffer, this.configurerSlot);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.isAssemblyWorking = data.getBoolean("isWorking");
        NBTTagCompound stackData = data.getCompoundTag("blueprint");
        if (!stackData.isEmpty()) {
            this.blueprintSlot.setStackInSlot(0, new ItemStack(stackData));
        }
        this.componentIndex = data.getInteger("componentIndex");
        this.blueprintSlot.setLocked(this.isAssemblyWorking);
        this.fuelingProgress = data.getInteger("fuelingProgress");
        this.configurerSlot.deserializeNBT(data.getCompoundTag("configurer"));
        try {
            this.state = LaunchComplexState.valueOf(data.getString("state"));
        } catch (IllegalArgumentException e) {
            this.state = LaunchComplexState.IDLE;
        }
        // Rebuild the component list without going back through startAssembly, which
        // would reset the index.
        if (this.isAssemblyWorking && !this.blueprintSlot.isEmpty()) {
            AbstractRocketBlueprint bp = getCurrentBlueprint();
            if (bp != null) {
                this.componentList = bp.getStages().stream().flatMap(x -> x.getComponents().values().stream())
                        .flatMap(List::stream).collect(Collectors.toList());
            }
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(this.isAssemblyWorking);
        buf.writeInt(this.componentIndex);
        buf.writeEnumValue(this.state);
        buf.writeInt(this.fuelingProgress);
        buf.writeBoolean(this.configWithinBudget);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.isAssemblyWorking = buf.readBoolean();
        this.componentIndex = buf.readInt();
        this.state = buf.readEnumValue(LaunchComplexState.class);
        this.fuelingProgress = buf.readInt();
        this.configWithinBudget = buf.readBoolean();
        this.blueprintSlot.setLocked(this.isAssemblyWorking);
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        if (dataId == SuSyDataCodes.UPDATE_RENDER_STATE) {
            this.state = buf.readEnumValue(LaunchComplexState.class);
        } else if (dataId == SuSyDataCodes.UPDATE_FUEL_PROGRESS) {
            this.fuelingProgress = buf.readInt();
        } else if (dataId == SuSyDataCodes.UPDATE_CAN_HANDLE_FULL_CONFIG) {
            this.configWithinBudget = buf.readBoolean();
        } else {
            super.receiveCustomData(dataId, buf);
            if (dataId == GregtechDataCodes.LOCK_OBJECT_HOLDER) {
                this.blueprintSlot.setLocked(buf.readBoolean());
            }
        }
    }

    // --- GUI
    // -----------------------------------------------------------------------------------------------------

    @Override
    public int getNumProgressBars() {
        return 2;
    }

    @Override
    public double getFillPercentage(int index) {
        if (!isStructureFormed())
            return 0;
        if (index == 1 && isAssemblyWorking && !this.componentList.isEmpty()) {
            return (float) (this.componentIndex + 1) / (float) this.componentList.size();
        }
        if (index == 0 && isAssemblyWorking && this.recipeMapWorkable.isWorking()) {
            return (float) this.recipeMapWorkable.getProgress() / (float) this.recipeMapWorkable.getMaxProgress();
        }
        return 0;
    }

    @Override
    public TextureArea getProgressBarTexture(int index) {
        return index == 0 ? SusyGuiTextures.PROGRESS_BAR_ROCKET_ASSEMBLER_COMPONENT :
                SusyGuiTextures.PROGRESS_BAR_ROCKET_ASSEMBLER_OVERALL;
    }

    private void addBarHoverText(List<ITextComponent> textList) {
        if (componentList.isEmpty())
            return;
        textList.add(new TextComponentTranslation("susy.machine.rocket_assembler.gui.overall_progress",
                this.componentIndex, this.componentList.size(),
                String.format("%.1f", (double) 100 * this.componentIndex / this.componentList.size())));
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        if (this.state != LaunchComplexState.ASSEMBLING) {
            textList.add(new TextComponentTranslation(
                    "susy.lunar_launch_complex." + this.state.name().toLowerCase(Locale.ROOT)));
        }
        if (this.state == LaunchComplexState.LOADED && this.selectedRocket != null) {
            textList.add(new TextComponentTranslation("susy.launch_pad.gui.fuel_progress", this.fuelingProgress,
                    this.selectedRocket.getFuelVolume()));
        }
        if (!this.configWithinBudget) {
            textList.add(new TextComponentTranslation("susy.rocket_programmer.not_enough_budget"));
        }
    }

    @Override
    protected void addWarningText(List<ITextComponent> textList) {
        super.addWarningText(textList);
        if (isAssemblyWorking && !((RocketAssemblerLogic) recipeMapWorkable).hasEnoughElectrodes) {
            textList.add(new TextComponentTranslation("susy.machine.rocket_assembler.warning.no_electrodes"));
        }
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return createUITemplate(entityPlayer).build(getHolder(), entityPlayer);
    }

    protected ModularUI.Builder createUITemplate(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 198, 216);
        builder.image(4, 4, 190, 117, GuiTextures.DISPLAY)
                .widget(new ProgressWidget(() -> this.getFillPercentage(0), 4, 123, 94, 7,
                        this.getProgressBarTexture(0), ProgressWidget.MoveType.HORIZONTAL))
                .widget(new ProgressWidget(() -> this.getFillPercentage(1), 100, 123, 94, 7,
                        this.getProgressBarTexture(1), ProgressWidget.MoveType.HORIZONTAL)
                        .setHoverTextConsumer(this::addBarHoverText));
        builder.widget(new IndicatorImageWidget(174, 101, 17, 17, getLogo())
                .setWarningStatus(getWarningLogo(), this::addWarningText)
                .setErrorStatus(getErrorLogo(), this::addErrorText));

        builder.label(9, 9, getMetaFullName(), 0xFFFFFF);
        builder.widget(new AdvancedTextWidget(9, 20, this::addDisplayText, 0xFFFFFF).setMaxWidthLimit(181)
                .setClickHandler(this::handleDisplayClick));

        // Power Button
        IControllable controllable = getCapability(GregtechTileCapabilities.CAPABILITY_CONTROLLABLE, null);
        if (controllable != null) {
            builder.widget(new ImageCycleButtonWidget(173, 191, 18, 18, GuiTextures.BUTTON_POWER,
                    controllable::isWorkingEnabled, controllable::setWorkingEnabled));
            builder.widget(new ImageWidget(173, 209, 18, 6, GuiTextures.BUTTON_POWER_DETAIL));
        }

        // start button
        builder.widget(new ClickButtonWidget(173, 151, 18, 18, "", clickData -> {
            if (!this.blueprintSlot.isEmpty() && this.componentList.isEmpty()) {
                this.startAssembly(this.getCurrentBlueprint());
            }
        }).setTooltipText("susy.machine.rocket_assembler.gui.start")
                .setButtonTexture(SusyGuiTextures.ROCKET_ASSEMBLER_BUTTON_START));
        // stop button
        builder.widget(new ClickButtonWidget(173, 133, 18, 18, "", clickData -> this.abortAssembly())
                .setButtonTexture(SusyGuiTextures.ROCKET_ASSEMBLER_BUTTON_STOP)
                .setTooltipText("susy.machine.rocket_assembler.gui.stop"));
        builder.dynamicLabel(40, 79,
                () -> !blueprintSlot.isEmpty() ? "" : I18n.format(this.getMetaName() + ".blueprint_slot.name"),
                0x404040);
        SlotWidgetMentallyStable blueprintSlotWidget = new SlotWidgetMentallyStable(this.blueprintSlot, 0, 173, 79);
        blueprintSlotWidget.setBackgroundTexture(GuiTextures.SLOT_DARK);
        blueprintSlotWidget.setChangeListener(() -> {
            if (blueprintSlot.isEmpty()) {
                this.abortAssembly();
            }
        });
        builder.widget(blueprintSlotWidget);
        builder.widget(new SlotWidget(this.configurerSlot, 0, 173, 61).setBackgroundTexture(GuiTextures.SLOT_DARK)
                .setTooltipText("susy.launch_pad.gui.configurer_slot"));
        builder.widget(new ItemCostWidget(new Size(158, 50), new Position(9, 70), this::getCurrentRecipe,
                () -> this.blueprintSlot.isLocked()));

        builder.bindPlayerInventory(entityPlayer.inventory, 133);
        return builder;
    }

    @Override
    protected @NotNull Widget getFlexButton(int x, int y, int width, int height) {
        return new ClickButtonWidget(x, y, width, height, "", clickData -> this.abortAssembly())
                .setButtonTexture(SusyGuiTextures.ROCKET_ASSEMBLER_BUTTON_STOP)
                .setTooltipText("susy.machine.rocket_assembler.gui.stop");
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

    public enum LaunchComplexState {
        IDLE, // Nothing being built, nothing on the pad.
        ASSEMBLING, // Working through the blueprint's components.
        LOADED, // The rocket stands on the pad, taking on cargo and fuel.
        LAUNCHING // Countdown running.
    }
}
