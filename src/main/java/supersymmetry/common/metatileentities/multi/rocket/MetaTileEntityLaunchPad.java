package supersymmetry.common.metatileentities.multi.rocket;

import cam72cam.mod.entity.ModdedEntity;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.RelativeDirection;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
import supersymmetry.common.blocks.BlockRocketAssemblerCasing;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.entities.EntityRocket;
import supersymmetry.common.entities.EntityTransporterErector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MetaTileEntityLaunchPad extends MultiblockWithDisplayBase implements IAnimatableMTE {
    private AxisAlignedBB trainAABB;
    private EntityTransporterErector selectedErector;
    private EntityRocket selectedRocket;
    private LaunchPadState state = LaunchPadState.EMPTY;

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


    public MetaTileEntityLaunchPad(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityLaunchPad(metaTileEntityId);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("DDDDDDDDDDDDD", "     CCC     ", "     CCC     ", "     CCC     ", "     CCC     ", "     CCC     ", "     CCC     ", "     CCC     ", "     RRR     ")
                .aisle("DDDDDDDDDDDDD", "     CCC     ", "     CCC     ", "     CCC     ", "     CCC     ", "     CCC     ", "     CCC     ", "     CCC     ", "     RRR     ")
                .aisle("DDDDDDDDDDDDD", "     CCC     ", "     CCC     ", "     CCC     ", "     CCC     ", "     CCC     ", "     CCC     ", "     CCC     ", "     RRR     ")
                .aisle("DDDDDDDDDDDDD", "     CCC     ", "     CCC     ", "     CCC     ", "     CCC     ", "     CCC     ", "     CCC     ", "     CCC     ", "     RRR     ")
                .aisle("DDDDDDDDDDDDD", "     CCC     ", "     CCC     ", "     CCC     ", "     CCC     ", "     CCC     ", "     CCC     ", "     CCC     ", "     RRR     ")
                .aisle("DDDDDDDDDDDDD", "     CCC     ", "     CCC     ", "     CCC     ", "     CCC     ", "     CCC     ", "     CCC     ", "     CCC     ", "     RRR     ")
                .aisle("DDDDDDDDDDDDD", "     FFF     ", "     FFF     ", "     FFF     ", "     FFF     ", "     FFF     ", "     FFF     ", "     FFF     ", "     FFF     ")
                .aisle("DDDCCCCCCCDDD", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ")
                .aisle("DDDCCCCCCCDDD", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ")
                .aisle("DDDCCCCCCCDDD", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ")
                .aisle("DDDCCCCCCCDDD", " L         L ", " L         L ", " L         L ", " L         L ", " L         L ", " L         L ", " L         L ", " L         L ")
                .aisle("DDDCCCCCCCDDD", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ")
                .aisle("DDDCCCCCCCDDD", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ")
                .aisle("DDDCCCCCCCDDD", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ")
                .aisle("DDDDDDDDDDDDD", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ")
                .aisle("DDDDDDDDDDDDD", "     FFF     ", "     FFF     ", "     FFF     ", "     FFF     ", "     FFF     ", "     FFF     ", "     FFF     ", "     FFF     ")
                .aisle("DDDDDDSDDDDDD", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ")
                .where(' ', any())
                .where('A', air())
                .where('S', selfPredicate())
                .where('D', states(getFoundationState()).or(autoAbilities()))
                .where('C', states(getReinforcedFoundationState()))
                .where('F', frames(Materials.Steel))
                .where('R', SuSyPredicates.rails())
                .where('L', SuSyPredicates.hiddenStates(Blocks.AIR.getDefaultState(),
                        SuSyBlocks.SUPPORT.getDefaultState()))
                .build();
    }

    public IBlockState getFoundationState() {
        return SuSyBlocks.ROCKET_ASSEMBLER_CASING.getState(BlockRocketAssemblerCasing.RocketAssemblerCasingType.FOUNDATION);
    }

    public IBlockState getReinforcedFoundationState() {
        return SuSyBlocks.ROCKET_ASSEMBLER_CASING.getState(BlockRocketAssemblerCasing.RocketAssemblerCasingType.REINFORCED_FOUNDATION);
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
            this.fillHiddenBlocksWith(SuSyBlocks.SUPPORT.getDefaultState());
        }
        if (this.needsReinitialization) {
            this.setLaunchPadState(LaunchPadState.INITIALIZING);
        }
    }

    @Override
    public void onPlacement() {
        super.onPlacement();
        this.needsReinitialization = true;
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.fillHiddenBlocksWith(Blocks.AIR.getDefaultState());
        this.trainAABB = null;
        this.needsReinitialization = true;
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
                    this.selectedRocket.startCountdown();
                }
                if (this.selectedRocket.posY > this.getLaunchPosition().y + 40) {
                    this.setLaunchPadState(LaunchPadState.EMPTY);
                }
                break;
        }
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
                    if (forgeTrainEntity.getSelf() instanceof EntityTransporterErector rollingStock && rollingStock.isRocketLoaded()) {
                        this.selectedErector = rollingStock;
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
        this.getWorld().spawnEntity(this.selectedRocket);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.state = LaunchPadState.valueOf(data.getString("state"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setString("state", this.state.name());
        return super.writeToNBT(data);
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeEnumValue(this.state);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.state = buf.readEnumValue(LaunchPadState.class);
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        if (dataId == SuSyDataCodes.RESET_RENDER_FIELDS) {
            this.lightPos = null;
            this.renderBounding = null;
            this.transformation = null;
        } else if (dataId == SuSyDataCodes.UPDATE_RENDER_STATE) {
            this.state = buf.readEnumValue(LaunchPadState.class);
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

    protected void fillHiddenBlocksWith(IBlockState state) {
        if (this.hiddenBlocks == null) {
            return;
        }
        for (BlockPos pos : this.hiddenBlocks) {
            getWorld().setBlockState(pos, state);
        }
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

    public enum LaunchPadState {
        INITIALIZING, // The launch pad is going through its initial animation of the supports coming out of the ground.
        EMPTY, // No rocket transporter has been selected, nor is there any rocket in the launch pad.
        LOADING, // A rocket transporter has been selected, causing it to begin the erecting process.
        LOADED, // A rocket has been loaded into the launch pad. Players should be able to enter through physical rocket supports and remotely launch the rocket.
        LAUNCHING // The rocket supports retract and the engines are turned on.
    }
}
