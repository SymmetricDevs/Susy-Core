package supersymmetry.common.metatileentities.multi.rocket;

import java.util.*;
import java.util.stream.Collectors;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import cam72cam.mod.entity.ModdedEntity;
import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.RelativeDirection;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.MetaBlocks;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import supersymmetry.api.capability.SuSyDataCodes;
import supersymmetry.api.metatileentity.IAnimatableMTE;
import supersymmetry.api.metatileentity.multiblock.SuSyPredicates;
import supersymmetry.api.rocketry.fuels.RocketFuelEntry;
import supersymmetry.api.rocketry.rockets.AbstractRocketBlueprint;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.BlockRocketAssemblerCasing;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.entities.EntityRocket;
import supersymmetry.common.entities.EntityTransporterErector;
import supersymmetry.common.item.SuSyMetaItems;

public class MetaTileEntityLaunchPad extends MultiblockWithDisplayBase implements IAnimatableMTE {

    private AxisAlignedBB trainAABB;
    private EntityTransporterErector selectedErector;
    private EntityRocket selectedRocket;
    private LaunchPadState state = LaunchPadState.EMPTY;
    protected IItemHandlerModifiable inputInventory;
    protected IMultipleTankHandler inputFluidInventory;

    // Animation helpers
    private double supportAngle = Math.PI / 4;

    @SideOnly(Side.CLIENT)
    private BlockPos lightPos;
    @SideOnly(Side.CLIENT)
    private Vec3i transformation;

    @SideOnly(Side.CLIENT)
    private AnimationFactory factory;
    @Nullable
    private Collection<BlockPos> hiddenBlocks;
    private AxisAlignedBB renderBounding;
    private int fuelingProgress;

    public MetaTileEntityLaunchPad(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityLaunchPad(metaTileEntityId);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        String allD = "DDDDDDDDDDDDDDDDDDDDDDD"; // 23 D's
        String dcTrack = "DDDDDDDDCCCCCCCDDDDDDDD"; // 8D + 7C + 8D = 23: launch surface
        String dcHoleTrack = "DDDDDDDDCC   CCDDDDDDDD"; // 8D + 7C + 8D = 23: hole
        String ctrlRow = "DDDDDDDDDDDSDDDDDDDDDDD"; // 11D + S + 11D = 23: self-row
        String sp23 = "                       "; // 23 spaces
        String ccc23 = "          CCC          "; // 10sp + 3C + 10sp = 23: reinforced foundation under track
        String supp3 = "     CC   CCC   CC     "; // reinforced foundation under three supports
        String rrr23 = "          RRR          "; // 10sp + 3R + 10sp = 23
        String fff23 = "          LLL          "; // 10sp + 3F + 10sp = 23
        String l23 = "     LL         LL     "; // 6sp + L + 9sp + L + 6sp = 23
        String cSides = "   CCCC         CCCC   "; // reinforced foundation underneath sides
        String dad23 = "DDDDDDDD       DDDDDDDD"; // 8C + 7sp + 8C = 23
        String allC = "CCCCCCCCCCCCCCCCCCCCCCC"; // 23 reinforced foundation
        String clasp = "     LLLLL   LLLLL     ";
        String claspOut = "    LLLLLL   LLLLLL    ";

        FactoryBlockPattern p = FactoryBlockPattern.start();
        // 10 erector approach aisles
        aisleWithSpace(p, sp23, allC, dad23, dad23, dad23, allD, ccc23, ccc23, ccc23, ccc23, ccc23, ccc23, ccc23,
                rrr23);
        aisleWithSpace(p, sp23, allC, dad23, dad23, dad23, allD, ccc23, ccc23, ccc23, ccc23, ccc23, ccc23, ccc23,
                rrr23);
        aisleWithSpace(p, sp23, allC, dad23, dad23, dad23, allD, ccc23, ccc23, ccc23, ccc23, ccc23, ccc23, ccc23,
                rrr23);
        aisleWithSpace(p, sp23, allC, dad23, dad23, dad23, allD, ccc23, ccc23, ccc23, ccc23, ccc23, ccc23, ccc23,
                rrr23);
        aisleWithSpace(p, sp23, allC, dad23, dad23, dad23, allD, ccc23, ccc23, ccc23, ccc23, ccc23, ccc23, ccc23,
                rrr23);
        aisleWithSpace(p, sp23, allC, dad23, dad23, dad23, allD, ccc23, ccc23, ccc23, ccc23, ccc23, ccc23, ccc23,
                rrr23);
        aisleWithSpace(p, sp23, allC, dad23, dad23, dad23, allD, ccc23, ccc23, ccc23, ccc23, ccc23, ccc23, ccc23,
                rrr23);
        aisleWithSpace(p, sp23, allC, dad23, dad23, dad23, allD, ccc23, ccc23, ccc23, ccc23, ccc23, ccc23, ccc23,
                rrr23);
        aisleWithSpace(p, sp23, allC, dad23, dad23, dad23, allD, ccc23, ccc23, ccc23, ccc23, ccc23, ccc23, ccc23,
                rrr23);
        aisleWithSpace(p, sp23, allC, dad23, dad23, dad23, allD, ccc23, ccc23, ccc23, ccc23, ccc23, ccc23, ccc23,
                rrr23);
        // 2 extra tracks with the other two diagonal supports
        aisleWithSpace(p, sp23, allC, dad23, dad23, dad23, allD, supp3, supp3, supp3, supp3, supp3, ccc23, ccc23,
                rrr23);
        aisleWithSpace(p, sp23, allC, dad23, dad23, dad23, allD, supp3, supp3, supp3, supp3, supp3, ccc23, ccc23,
                rrr23);
        // First separator
        aisleWithSpace(p, sp23, allC, dad23, dad23, dad23, allD, sp23, sp23, sp23, sp23, sp23, sp23, sp23, sp23);
        // Main platform (front half)
        aisleWithSpace(p, sp23, allC, sp23, sp23, sp23, dcTrack, sp23, sp23, sp23, sp23, sp23, sp23, sp23, sp23);
        aisleWithSpace(p, sp23, allC, sp23, sp23, sp23, dcTrack, sp23, sp23, sp23, sp23, sp23, sp23, sp23, sp23);
        // Main platform (center — support pillars)
        aisleWithSpace(p, l23, allC, sp23, sp23, sp23, dcHoleTrack, cSides, cSides, cSides, cSides, cSides, cSides, l23,
                l23);
        aisleWithSpace(p, l23, allC, sp23, sp23, sp23, dcHoleTrack, cSides, cSides, cSides, cSides, cSides, cSides, l23,
                l23);
        aisleWithSpace(p, l23, allC, sp23, sp23, sp23, dcHoleTrack, cSides, cSides, cSides, cSides, cSides, cSides, l23,
                l23);
        // Main platform (back half)
        aisleWithSpace(p, sp23, allC, sp23, sp23, sp23, dcTrack, sp23, sp23, sp23, sp23, sp23, sp23, sp23, sp23);
        aisleWithSpace(p, sp23, allC, sp23, sp23, sp23, dcTrack, sp23, sp23, sp23, sp23, sp23, sp23, sp23, sp23);
        // Transition
        aisleWithSpace(p, sp23, allC, dad23, dad23, dad23, allD, sp23, sp23, sp23, sp23, sp23, sp23, sp23, sp23);
        // Back frame
        aisleWithSpaceShort(p, fff23, sp23, allC, dad23, dad23, dad23, allD, supp3, supp3, supp3, supp3, supp3, fff23,
                fff23,
                fff23);
        // One other separator
        aisleWithSpace(p, sp23, allC, dad23, dad23, dad23, allD, supp3, supp3, supp3, supp3, supp3, sp23, sp23, sp23);
        // Controller
        aisleWithSpace(p, sp23, allC, dad23, dad23, dad23, ctrlRow, sp23, sp23, sp23, sp23, sp23, sp23, sp23, sp23);
        return p.where(' ', any())
                .where('A', air())
                .where('S', selfPredicate())
                .where('D', states(getFoundationState()).or(autoAbilities()))
                .where('C', states(getReinforcedFoundationState()))
                .where('F', frames(Materials.Steel))
                .where('R', SuSyPredicates.rails())
                .where('L',
                        SuSyPredicates.hiddenStates(MetaBlocks.FRAMES.get(Materials.Steel).getBlock(Materials.Steel)))
                .build();
    }

    private void aisleWithSpace(FactoryBlockPattern pattern, String repeat,
                                String v1, String v2, String v3, String v4, String v5, String v6, String v7, String v8,
                                String v9, String v10, String v11, String v12, String v13) {
        // Repeat 30 times
        pattern.aisle(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13,
                repeat, repeat, repeat, repeat, repeat,
                repeat, repeat, repeat, repeat, repeat,
                repeat, repeat, repeat, repeat, repeat,
                repeat, repeat, repeat, repeat, repeat,
                repeat, repeat, repeat, repeat, repeat,
                repeat, repeat, repeat, repeat, repeat);
    }

    private void aisleWithSpaceShort(FactoryBlockPattern pattern, String repeat, String repea2,
                                     String v1, String v2, String v3, String v4, String v5, String v6, String v7,
                                     String v8,
                                     String v9, String v10, String v11, String v12, String v13) {
        // Repeat 30 times
        pattern.aisle(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13,
                repeat, repeat, repeat, repeat, repeat,
                repeat, repeat, repeat, repeat, repeat,
                repeat, repeat, repeat, repeat, repeat,
                repeat, repeat, repeat, repeat, repeat,
                repea2, repea2, repea2, repea2, repea2,
                repea2, repea2, repea2, repea2, repea2);
    }

    private void aisleWithClasp(FactoryBlockPattern pattern, String repeat, String clasp,
                                String v1, String v2, String v3, String v4, String v5, String v6, String v7,
                                String v8,
                                String v9, String v10, String v11, String v12, String v13) {
        // Repeat 30 times
        pattern.aisle(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13,
                repeat, repeat, repeat, repeat, repeat,
                repeat, repeat, repeat, repeat, repeat,
                repeat, repeat, repeat, repeat, repeat,
                repeat, repeat, repeat, repeat, repeat,
                repeat, repeat, repeat, repeat, repeat,
                repeat, repeat, repeat, repeat, repeat);
    }

    public TraceabilityPredicate autoAbilities() {
        return autoAbilities(true, true)
                .or(abilities(MultiblockAbility.IMPORT_ITEMS).setMaxGlobalLimited(1))
                .or(abilities(MultiblockAbility.IMPORT_FLUIDS).setMaxGlobalLimited(4));
    }

    public IBlockState getFoundationState() {
        return SuSyBlocks.ROCKET_ASSEMBLER_CASING
                .getState(BlockRocketAssemblerCasing.RocketAssemblerCasingType.FOUNDATION);
    }

    public IBlockState getReinforcedFoundationState() {
        return SuSyBlocks.ROCKET_ASSEMBLER_CASING
                .getState(BlockRocketAssemblerCasing.RocketAssemblerCasingType.REINFORCED_FOUNDATION);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        setAABBs();

        this.hiddenBlocks = context.getOrDefault("Hidden", new ArrayList<>());
        World world = getWorld();

        // This will only be called on a server side world
        // so actually no need to check !world.isRemote
        if (world != null) {
            disableBlockRendering(true);
        }

        findRocket();
        if (this.selectedRocket != null) {
            this.setLaunchPadState(LaunchPadState.LOADED);
        }

        this.inputInventory = new ItemHandlerList(getAbilities(MultiblockAbility.IMPORT_ITEMS));
        this.inputFluidInventory = new FluidTankList(false,
                getAbilities(MultiblockAbility.IMPORT_FLUIDS));
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        disableBlockRendering(false);
        this.trainAABB = null;
        this.state = LaunchPadState.INITIALIZING;
        this.inputInventory = new GTItemStackHandler(this, 0);
        this.inputFluidInventory = new FluidTankList(true);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    public void setAABBs() {
        // Had to make it overshoot a little :(
        BlockPos offsetBottomLeft = new BlockPos(6, 5, 12);
        BlockPos offsetTopRight = new BlockPos(-6, 20, 18);

        switch (this.getFrontFacing()) {
            case EAST:
                offsetBottomLeft = offsetBottomLeft.rotate(Rotation.CLOCKWISE_90);
                offsetTopRight = offsetTopRight.rotate(Rotation.CLOCKWISE_90);
                break;
            case SOUTH:
                offsetBottomLeft = offsetBottomLeft.rotate(Rotation.CLOCKWISE_180);
                offsetTopRight = offsetTopRight.rotate(Rotation.CLOCKWISE_180);
                break;
            case WEST:
                offsetBottomLeft = offsetBottomLeft.rotate(Rotation.COUNTERCLOCKWISE_90);
                offsetTopRight = offsetTopRight.rotate(Rotation.COUNTERCLOCKWISE_90);
                break;
            default:
                break;
        }

        this.trainAABB = new AxisAlignedBB(getPos().add(offsetBottomLeft), getPos().add(offsetTopRight));
    }

    public Vec3d getLaunchPosition() {
        Vec3d offset = new Vec3d(0.5, 1, 7.5);
        switch (this.getFrontFacing()) {
            case EAST:
                offset.rotateYaw((float) (-Math.PI / 2));
                break;
            case SOUTH:
                offset.rotateYaw((float) Math.PI);
                break;
            case WEST:
                offset.rotateYaw((float) (Math.PI / 2));
                break;
            default:
                break;
        }
        return new Vec3d(this.getPos()).add(offset);
    }

    public AxisAlignedBB getRocketAABB() {
        Vec3d launchPosition = getLaunchPosition();
        return new AxisAlignedBB(launchPosition, launchPosition).expand(2, 8, 2);
    }

    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }

    @Override
    protected void updateFormedValid() {
        if (this.isFirstTick()) {
            this.setLaunchPadState(LaunchPadState.INITIALIZING);
        }
        switch (this.state) {
            case INITIALIZING:
                // Run mini versions of the later logic just to run through everything
                updateSelectedErector();
                findRocket();
                if (checkRocket()) {
                    if (selectedRocket.isCountdownStarted() &&
                            selectedRocket.getLaunchTime() <= this.getWorld().getTotalWorldTime()) {
                        this.setLaunchPadState(LaunchPadState.LAUNCHING);
                    } else {
                        this.setLaunchPadState(LaunchPadState.LOADED);
                    }
                    break;
                }
                if (checkErector() && selectedErector.isRocketLoaded()) {
                    this.setLaunchPadState(LaunchPadState.LOADING);
                    this.selectedErector.setLiftingMode(EntityTransporterErector.LiftingMode.UP);
                }
                this.setLaunchPadState(LaunchPadState.EMPTY);
            case EMPTY:
                if (this.getOffsetTimer() % 5 == 0) {
                    updateSelectedErector();
                    if (checkErector() && selectedErector.isRocketLoaded()) {
                        this.setLaunchPadState(LaunchPadState.LOADING);
                        this.selectedErector.setLiftingMode(EntityTransporterErector.LiftingMode.UP);
                    }
                } else {
                    break;
                }
            case LOADING:
                if (!checkErector() || !this.selectedErector.isRocketLoaded()) {
                    this.setLaunchPadState(LaunchPadState.EMPTY);
                    break;
                }
                this.supportAngle = this.selectedErector.getLifterAngle();
                if (this.selectedErector.getLifterAngle() >= Math.PI / 2) {
                    this.selectedErector.setRocketLoaded(false);
                    spawnRocket(this.selectedErector.getRocketNBT());
                    setFuelingProgress(0);
                    this.setLaunchPadState(LaunchPadState.LOADED);
                } else {
                    break;
                }
            case LOADED:
                if (!checkRocket()) {
                    findRocket();
                    if (!checkRocket()) {
                        this.setLaunchPadState(LaunchPadState.EMPTY);
                        break;
                    }
                }
                if (!loadCargo() || this.getInputRedstoneSignal(this.getFrontFacing(), false) == 0) {
                    break;
                }
                this.setLaunchPadState(LaunchPadState.LAUNCHING);
            case LAUNCHING:
                updateSelectedErector();
                if (checkErector()) {
                    this.selectedErector.setLiftingMode(EntityTransporterErector.LiftingMode.DOWN);
                }
                if (!checkRocket()) {
                    findRocket();
                    if (!checkRocket()) {
                        this.setLaunchPadState(LaunchPadState.EMPTY);
                        break;
                    }
                }
                this.supportAngle = Math.max(Math.PI / 4, this.supportAngle - (0.087 / 20));
                if (this.supportAngle <= Math.PI / 4 && !this.selectedRocket.isCountdownStarted()) {
                    this.selectedRocket.startCountdown(200);
                }
                if (this.selectedRocket.posY > this.getLaunchPosition().y + 40) {
                    this.setLaunchPadState(LaunchPadState.EMPTY);
                }
                break;
        }
    }

    // In liters per second
    private static final int MAX_FUELING_SPEED = 8000;

    private boolean loadCargo() {
        GTTransferUtils.moveInventoryItems(this.getImportItems(), selectedRocket.getInventory());
        if (this.fuelingProgress >= selectedRocket.getFuelVolume()) {
            return true;
        }
        RocketFuelEntry fuelEntry = selectedRocket.getFuel();

        if (fuelEntry == null) {
            List<Fluid> fluids = this.inputFluidInventory.getFluidTanks().stream()
                    .map((tank) -> tank.getFluid() == null ? null : tank.getFluid().getFluid())
                    .distinct()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            Optional<RocketFuelEntry> possibleEntry = RocketFuelEntry.search(fluids);
            if (possibleEntry.isEmpty()) {
                return false;
            }
            fuelEntry = possibleEntry.get();
            selectedRocket.setFuel(fuelEntry);
        }
        var composition = fuelEntry.getComposition();
        // Round up for the composition
        int totalMBPerUnit = composition.stream().mapToInt(Tuple::getSecond).sum();
        int maxFuelingProgress = selectedRocket.getFuelVolume() + totalMBPerUnit - 1;
        int unitsDrained = Math.min(maxFuelingProgress - this.fuelingProgress, MAX_FUELING_SPEED / totalMBPerUnit);
        for (var comp : composition) {
            FluidStack tryToDrain = new FluidStack(comp.getFirst(), MAX_FUELING_SPEED);
            FluidStack drained = inputFluidInventory.drain(tryToDrain, false);
            // Intentional integer division moment
            unitsDrained = Math.min(drained.amount / comp.getSecond(), unitsDrained);
        }
        setFuelingProgress(this.fuelingProgress + (unitsDrained * totalMBPerUnit));
        for (var comp : composition) {
            inputFluidInventory.drain(new FluidStack(comp.getFirst(), (comp.getSecond() * unitsDrained)), true);
        }

        return this.fuelingProgress >= selectedRocket.getFuelVolume();
    }

    private void setFuelingProgress(int fuelingProgress) {
        this.fuelingProgress = fuelingProgress;
        writeCustomData(SuSyDataCodes.UPDATE_FUEL_PROGRESS, (buf) -> buf.writeInt(fuelingProgress));
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
    }

    @Override
    protected boolean canMachineConnectRedstone(EnumFacing side) {
        return side == this.getFrontFacing();
    }

    private void updateSelectedErector() {
        if (!checkErector()) {
            List<ModdedEntity> trains = getWorld().getEntitiesWithinAABB(ModdedEntity.class, this.trainAABB);

            if (!trains.isEmpty()) {
                for (ModdedEntity forgeTrainEntity : trains) {
                    if (forgeTrainEntity.getSelf() instanceof EntityTransporterErector rollingStock &&
                            rollingStock.isRocketLoaded()) {
                        // Dot product check to make sure it's facing the right way
                        Vec3d toController = new Vec3d(getPos()).subtract(rollingStock.internal.getPositionVector());
                        double dot = toController.dotProduct(rollingStock.internal.getLookVec());
                        if (dot > 0) {
                            this.selectedErector = rollingStock;
                        }
                    }
                }
            }
        } else {
            if (!this.selectedErector.internal.getEntityBoundingBox().intersects(this.trainAABB)) {
                this.selectedErector = null;
            }
        }
    }

    private void findRocket() {
        List<EntityRocket> rockets = getWorld().getEntitiesWithinAABB(EntityRocket.class, getRocketAABB());
        if (!rockets.isEmpty()) {
            this.selectedRocket = rockets.get(0);
        }
    }

    public void spawnRocket(NBTTagCompound tag) {
        Vec3d position = this.getLaunchPosition();
        this.selectedRocket = new EntityRocket(this.getWorld(), position, this.getFrontFacing().getHorizontalAngle());
        if (tag != null) {
            // Copy in all tags
            for (Map.Entry<String, NBTBase> info : tag.tagMap.entrySet()) {
                this.selectedRocket.getEntityData().setTag(info.getKey(), info.getValue());
            }
        }
        this.getWorld().spawnEntity(this.selectedRocket);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.state = LaunchPadState.valueOf(data.getString("state"));
        this.fuelingProgress = data.getInteger("fuelingProgress");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setString("state", this.state.name());
        data.setInteger("fuelProgress", this.fuelingProgress);
        return super.writeToNBT(data);
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        if (this.isStructureFormed()) {
            findRocket();
            updateSelectedErector();
        }
        buf.writeEnumValue(this.state);
        buf.writeInt(this.fuelingProgress);
        World world = getWorld();
        if (world != null && !world.isRemote) {
            disableBlockRendering(isStructureFormed());
        }
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.state = buf.readEnumValue(LaunchPadState.class);
        this.fuelingProgress = buf.readInt();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        if (dataId == SuSyDataCodes.RESET_RENDER_FIELDS) {
            this.lightPos = null;
            this.renderBounding = null;
            this.transformation = null;
        } else if (dataId == SuSyDataCodes.UPDATE_RENDER_STATE) {
            this.state = buf.readEnumValue(LaunchPadState.class);
        } else if (dataId == SuSyDataCodes.UPDATE_FUEL_PROGRESS) {
            this.fuelingProgress = buf.readInt();
        } else {
            super.receiveCustomData(dataId, buf);
        }
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if (this.renderBounding == null) {
            EnumFacing front = getFrontFacing();
            // The left side of the controller, not from the player's perspective
            EnumFacing left = RelativeDirection.LEFT.getRelativeFacing(front, getUpwardsFacing(), isFlipped());
            EnumFacing up = RelativeDirection.UP.getRelativeFacing(front, getUpwardsFacing(), isFlipped());

            BlockPos pos = getPos();

            var v1 = pos.offset(left.getOpposite(), 10).offset(up.getOpposite());
            var v2 = pos.offset(left, 10).offset(up, 32).offset(front.getOpposite(), 17);
            this.renderBounding = new AxisAlignedBB(v1, v2);
        }
        return renderBounding;
    }

    @Override
    @Nullable
    public Collection<BlockPos> getHiddenBlocks() {
        return hiddenBlocks;
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController<>(this, "controller", 0.0F, this::predicate));
    }

    public void setLaunchPadState(LaunchPadState state) {
        if (this.state != state) {
            this.state = state;
            this.writeCustomData(SuSyDataCodes.UPDATE_RENDER_STATE, buf -> buf.writeEnumValue(state));
        }
    }

    @SideOnly(Side.CLIENT)
    private <T extends MetaTileEntity & IAnimatableMTE> PlayState predicate(AnimationEvent<T> event) {
        if (this.state == LaunchPadState.LOADING) {
            event.getController().setAnimation(new AnimationBuilder()
                    .addAnimation("protract", ILoopType.EDefaultLoopTypes.HOLD_ON_LAST_FRAME));
            return PlayState.CONTINUE;
        }
        if (this.state == LaunchPadState.LAUNCHING) {
            event.getController().setAnimation(new AnimationBuilder()
                    .addAnimation("retract", ILoopType.EDefaultLoopTypes.HOLD_ON_LAST_FRAME));
            return PlayState.CONTINUE;
        }
        return isStructureFormed() ? PlayState.CONTINUE : PlayState.STOP;
    }

    @Override
    public AnimationFactory getFactory() {
        if (this.factory == null) {
            this.factory = new AnimationFactory(this);
        }
        return this.factory;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderMetaTileEntity(double x, double y, double z, float partialTicks) {
        if (isStructureFormed()) {
            IAnimatableMTE.super.renderMetaTileEntity(x, y, z, partialTicks);
        }
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        textList.add(new TextComponentTranslation("susy.launch_pad." + this.state.name().toLowerCase()));
        if (this.state == LaunchPadState.LOADED && this.selectedRocket != null) {
            int maxFuelingProgress = this.selectedRocket.getFuelVolume();
            textList.add(new TextComponentTranslation("susy.launch_pad.gui.fuel_progress", this.fuelingProgress,
                    maxFuelingProgress));
        }
    }

    public enum LaunchPadState {
        INITIALIZING, // The launch pad is literally just checking for existing entities.
        EMPTY, // No rocket transporter has been selected, nor is there any rocket in the launch pad.
        LOADING, // A rocket transporter has been selected, causing it to begin the erecting process.
        LOADED, // A rocket has been loaded into the launch pad. Players should be able to enter through physical rocket
        // supports and remotely launch the rocket.
        LAUNCHING // The rocket supports retract and the engines are turned on.
    }

    @Override
    public @NotNull ICubeRenderer getFrontOverlay() {
        return SusyTextures.LAUNCH_PAD_OVERLAY;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(),
                this.isStructureFormed(), this.isStructureFormed());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockPos getLightPos() {
        if (this.lightPos == null) {
            this.lightPos = getPos().offset(EnumFacing.UP, 6);
        }
        return lightPos;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Vec3i getTransformation() {
        if (this.transformation == null) {
            EnumFacing front = getFrontFacing();
            EnumFacing upwards = getUpwardsFacing();
            boolean flipped = isFlipped();
            EnumFacing back = front.getOpposite();
            EnumFacing up = RelativeDirection.UP.getRelativeFacing(front, upwards, flipped);

            int xOff = back.getXOffset() * 7 + up.getXOffset() * -3;
            int yOff = back.getYOffset() * 7 + up.getYOffset() * -3;
            int zOff = back.getZOffset() * 7 + up.getZOffset() * -3;

            this.transformation = new Vec3i(xOff, yOff, zOff);
        }
        return transformation;
    }

    @Override
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                CuboidRayTraceResult hitResult) {
        if (this.state == LaunchPadState.EMPTY && playerIn.isCreative() &&
                playerIn.getHeldItem(hand).isItemEqual(SuSyMetaItems.DATA_CARD_MASTER_BLUEPRINT.getStackForm())) {
            NBTTagCompound tag = playerIn.getHeldItem(hand).getTagCompound();
            if (tag != null) {
                AbstractRocketBlueprint bp = AbstractRocketBlueprint.getCopyOf(tag.getString("name"));
                bp.readFromNBT(tag);
                NBTTagCompound rocketTag = new NBTTagCompound();
                rocketTag.setLong("assemblerPosition", BlockPos.ORIGIN.toLong());
                rocketTag.setTag("rocket", bp.writeToNBT());
                spawnRocket(rocketTag);
                setFuelingProgress(0);
                setLaunchPadState(LaunchPadState.LOADED);
            }
        }
        return super.onRightClick(playerIn, hand, facing, hitResult);
    }

    public boolean checkErector() {
        return this.selectedErector != null && !this.selectedErector.isDead();
    }

    public boolean checkRocket() {
        return this.selectedRocket != null && !this.selectedRocket.isDead;
    }

    // stupid annoying issue with part checking on chunk reloads
    @Override
    public void checkStructurePattern() {
        TileEntity te = this.getWorld().getTileEntity(getPos());
        if (te != this.getHolder()) {
            if (te instanceof MetaTileEntityHolder holder &&
                    holder.getMetaTileEntity() instanceof MetaTileEntityLaunchPad launchPad) {
                launchPad.invalidateStructure();
            }
        }
        super.checkStructurePattern();
    }
}
