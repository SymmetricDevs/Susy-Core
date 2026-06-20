package supersymmetry.common.metatileentities.multi.rocket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import cam72cam.mod.entity.ModdedEntity;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
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
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.BlockRocketAssemblerCasing;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.entities.EntityRocket;
import supersymmetry.common.entities.EntityTransporterErector;

public class MetaTileEntityLaunchPad extends MultiblockWithDisplayBase implements IAnimatableMTE {

    private AxisAlignedBB trainAABB;
    private EntityTransporterErector selectedErector;
    private EntityRocket selectedRocket;
    private LaunchPadState state = LaunchPadState.EMPTY;
    protected IItemHandlerModifiable inputInventory;
    protected IMultipleTankHandler inputFluidInventory;

    // Animation helpers
    private double supportAngle = Math.PI / 4;
    private int reinitializationTimer = 0;
    private boolean needsReinitialization = false;

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
        String dcTrack = "DDDDDDDDCCCCCCCDDDDDDDD"; // 8D + 7C + 8D = 23
        String ctrlRow = "DDDDDDDDDDDSDDDDDDDDDDD"; // 11D + S + 11D = 23
        String sp23 = "                       "; // 23 spaces
        String ccc23 = "          CCC          "; // 10sp + 3C + 10sp = 23
        String rrr23 = "          RRR          "; // 10sp + 3R + 10sp = 23
        String fff23 = "          LLL          "; // 10sp + 3F + 10sp = 23
        String l23 = "      L         L      "; // 6sp + L + 9sp + L + 6sp = 23

        FactoryBlockPattern p = FactoryBlockPattern.start();
        // 11 erector approach aisles (original 6 + 5 new)
        aisleWithSpace(p, sp23, allD, ccc23, ccc23, ccc23, ccc23, ccc23, ccc23, ccc23, rrr23);
        aisleWithSpace(p, sp23, allD, ccc23, ccc23, ccc23, ccc23, ccc23, ccc23, ccc23, rrr23);
        aisleWithSpace(p, sp23, allD, ccc23, ccc23, ccc23, ccc23, ccc23, ccc23, ccc23, rrr23);
        aisleWithSpace(p, sp23, allD, ccc23, ccc23, ccc23, ccc23, ccc23, ccc23, ccc23, rrr23);
        aisleWithSpace(p, sp23, allD, ccc23, ccc23, ccc23, ccc23, ccc23, ccc23, ccc23, rrr23);
        aisleWithSpace(p, sp23, allD, ccc23, ccc23, ccc23, ccc23, ccc23, ccc23, ccc23, rrr23);
        aisleWithSpace(p, sp23, allD, ccc23, ccc23, ccc23, ccc23, ccc23, ccc23, ccc23, rrr23);
        aisleWithSpace(p, sp23, allD, ccc23, ccc23, ccc23, ccc23, ccc23, ccc23, ccc23, rrr23);
        aisleWithSpace(p, sp23, allD, ccc23, ccc23, ccc23, ccc23, ccc23, ccc23, ccc23, rrr23);
        aisleWithSpace(p, sp23, allD, ccc23, ccc23, ccc23, ccc23, ccc23, ccc23, ccc23, rrr23);
        aisleWithSpace(p, sp23, allD, ccc23, ccc23, ccc23, ccc23, ccc23, ccc23, ccc23, rrr23);
        aisleWithSpace(p, sp23, allD, ccc23, ccc23, ccc23, ccc23, ccc23, ccc23, ccc23, rrr23);
        // Main platform (front half)
        aisleWithSpace(p, sp23, dcTrack, sp23, sp23, sp23, sp23, sp23, sp23, sp23, sp23);
        aisleWithSpace(p, sp23, dcTrack, sp23, sp23, sp23, sp23, sp23, sp23, sp23, sp23);
        // Main platform (center — support pillars)
        aisleWithSpace(p, l23, dcTrack, l23, l23, l23, l23, l23, l23, l23, l23);
        aisleWithSpace(p, l23, dcTrack, l23, l23, l23, l23, l23, l23, l23, l23);
        aisleWithSpace(p, l23, dcTrack, l23, l23, l23, l23, l23, l23, l23, l23);
        // Main platform (back half)
        aisleWithSpace(p, sp23, dcTrack, sp23, sp23, sp23, sp23, sp23, sp23, sp23, sp23);
        aisleWithSpace(p, sp23, dcTrack, sp23, sp23, sp23, sp23, sp23, sp23, sp23, sp23);
        // Transition
        aisleWithSpace(p, sp23, allD, sp23, sp23, sp23, sp23, sp23, sp23, sp23, sp23);
        // Back frame separator
        aisleWithSpace(p, fff23, allD, fff23, fff23, fff23, fff23, fff23, fff23, fff23, fff23);
        // Controller
        aisleWithSpace(p, sp23, ctrlRow, sp23, sp23, sp23, sp23, sp23, sp23, sp23, sp23);
        return p.where(' ', any())
                .where('A', air())
                .where('S', selfPredicate())
                .where('D', states(getFoundationState()).or(autoAbilities()))
                .where('C', states(getReinforcedFoundationState()))
                .where('F', frames(Materials.Steel))
                .where('R', SuSyPredicates.rails())
                .where('L', SuSyPredicates.hiddenStates(SuSyBlocks.SUPPORT.getDefaultState()))
                .build();
    }

    private void aisleWithSpace(FactoryBlockPattern pattern, String repeat,
                                String v1, String v2, String v3, String v4, String v5, String v6, String v7, String v8,
                                String v9) {
        // Repeat 30 times
        pattern.aisle(v1, v2, v3, v4, v5, v6, v7, v8, v9,
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
        setTrainAABB();

        this.hiddenBlocks = context.getOrDefault("Hidden", new ArrayList<>());
        World world = getWorld();

        // This will only be called on a server side world
        // so actually no need to check !world.isRemote
        if (world != null) {
            disableBlockRendering(true);
        }
        if (this.needsReinitialization) {
            this.setLaunchPadState(LaunchPadState.INITIALIZING);
        } else {
            findRocket();
            if (this.selectedRocket != null) {
                this.setLaunchPadState(LaunchPadState.LOADED);
            }
        }

        this.inputInventory = new ItemHandlerList(getAbilities(MultiblockAbility.IMPORT_ITEMS));
        this.inputFluidInventory = new FluidTankList(false,
                getAbilities(MultiblockAbility.IMPORT_FLUIDS));
    }

    @Override
    public void onPlacement() {
        super.onPlacement();
        this.needsReinitialization = true;
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        disableBlockRendering(false);
        this.trainAABB = null;
        this.needsReinitialization = true;
        this.inputInventory = new GTItemStackHandler(this, 0);
        this.inputFluidInventory = new FluidTankList(true);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    public void setTrainAABB() {
        // Had to make it overshoot a little :(
        BlockPos offsetBottomLeft = new BlockPos(6, 5, 9);
        BlockPos offsetTopRight = new BlockPos(-6, 20, 17);

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
        Vec3d offset = new Vec3d(0, 1, 6);
        switch (this.getFrontFacing()) {
            case EAST:
                offset = new Vec3d(-6, 1, 0);
                break;
            case SOUTH:
                offset = new Vec3d(0, 1, -6);
                break;
            case WEST:
                offset = new Vec3d(6, 1, 0);
                break;
            default:
                break;
        }
        return new Vec3d(this.getPos()).add(offset);
    }

    public AxisAlignedBB getRocketAABB() {
        Vec3d launchPosition = getLaunchPosition();
        return new AxisAlignedBB(launchPosition, launchPosition).expand(2, 2, 2);
    }

    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }

    @Override
    protected void updateFormedValid() {
        switch (this.state) {
            case INITIALIZING:
                reinitializationTimer++;
                if (reinitializationTimer >= 20) {
                    this.needsReinitialization = false;
                    this.setLaunchPadState(LaunchPadState.EMPTY);
                }
                break;
            case EMPTY:
                if (this.getOffsetTimer() % 20 == 0) {
                    updateSelectedErector();
                    if (this.selectedErector != null) {
                        this.setLaunchPadState(LaunchPadState.LOADING);
                        this.selectedErector.setLiftingMode(EntityTransporterErector.LiftingMode.UP);
                    }
                } else {
                    break;
                }
            case LOADING:
                if (this.selectedErector == null || this.selectedErector.isDead()) {
                    this.setLaunchPadState(LaunchPadState.EMPTY);
                    break;
                }
                this.supportAngle = this.selectedErector.getLifterAngle();
                if (this.selectedErector.getLifterAngle() >= Math.PI / 2) {
                    this.selectedErector.setRocketLoaded(false);
                    spawnRocket();
                    setFuelingProgress(0);
                    this.setLaunchPadState(LaunchPadState.LOADED);
                } else {
                    break;
                }
            case LOADED:
                if (this.selectedRocket == null || this.selectedRocket.isDead) {
                    findRocket();
                    if (this.selectedRocket == null || this.selectedRocket.isDead) {
                        this.setLaunchPadState(LaunchPadState.EMPTY);
                        break;
                    }
                }
                loadCargo();
                if (this.getInputRedstoneSignal(this.getFrontFacing(), false) == 0) {
                    break;
                }
                this.setLaunchPadState(LaunchPadState.LAUNCHING);
            case LAUNCHING:
                if (this.selectedErector != null) {
                    this.selectedErector.setLiftingMode(EntityTransporterErector.LiftingMode.DOWN);
                }
                if (this.selectedRocket == null || this.selectedRocket.isDead) {
                    findRocket();
                    if (this.selectedRocket == null || this.selectedRocket.isDead) {
                        this.setLaunchPadState(LaunchPadState.EMPTY);
                        break;
                    }
                }
                this.supportAngle = Math.max(Math.PI / 4, this.supportAngle - (0.087 / 20));
                if (this.supportAngle <= Math.PI / 4 && !this.selectedRocket.isCountDownStarted()) {
                    this.selectedRocket.startCountdown(200);
                }
                if (this.selectedRocket.posY > this.getLaunchPosition().y + 40) {
                    this.setLaunchPadState(LaunchPadState.EMPTY);
                }
                break;
        }
    }

    // In liters per second
    private static final int MAX_FUELING_SPEED = 100;

    private void loadCargo() {
        GTTransferUtils.moveInventoryItems(this.inputInventory, selectedRocket.cargo);

        RocketFuelEntry fuelEntry = selectedRocket.getFuel();
        if (fuelEntry == null) {
            return;
        }
        var composition = fuelEntry.getComposition();
        int unitsDrained = Math.min(selectedRocket.getFuelVolume() - this.fuelingProgress, MAX_FUELING_SPEED);
        for (var comp : composition) {
            FluidStack drained = new FluidStack(comp.getFirst(), MAX_FUELING_SPEED);
            int amount = drained == null ? 0 : drained.amount;
            // Intentional integer division moment
            unitsDrained = Math.min(amount, unitsDrained / comp.getSecond());
        }
        setFuelingProgress(this.fuelingProgress + unitsDrained);
        for (var comp : composition) {
            inputFluidInventory.drain(new FluidStack(comp.getFirst(), (comp.getSecond() * unitsDrained)), true);
        }
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
        if (this.selectedErector == null) {
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

    public void spawnRocket() {
        Vec3d position = this.getLaunchPosition();
        this.selectedRocket = new EntityRocket(this.getWorld(), position, this.getFrontFacing().getHorizontalAngle());
        if (this.selectedErector.getRocketNBT() != null) {
            // Copy in all tags
            for (Map.Entry<String, NBTBase> tag : selectedErector.getRocketNBT().tagMap.entrySet()) {
                this.selectedRocket.getEntityData().setTag(tag.getKey(), tag.getValue());
            }
            this.selectedRocket.initializeLaunch();
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
            var v2 = pos.offset(left, 10).offset(up, 31).offset(front.getOpposite(), 17);
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
        if (this.state == LaunchPadState.INITIALIZING) {
            event.getController().setAnimation(new AnimationBuilder()
                    .addAnimation("initialize", ILoopType.EDefaultLoopTypes.HOLD_ON_LAST_FRAME));
            return PlayState.CONTINUE;
        }
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
    }

    public enum LaunchPadState {
        INITIALIZING, // The launch pad is going through its initial animation of the supports coming out of the ground.
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

            int xOff = back.getXOffset() * 6 + up.getXOffset() * -8;
            int yOff = back.getYOffset() * 6 + up.getYOffset() * -8;
            int zOff = back.getZOffset() * 6 + up.getZOffset() * -8;

            this.transformation = new Vec3i(xOff, yOff, zOff);
        }
        return transformation;
    }
}
