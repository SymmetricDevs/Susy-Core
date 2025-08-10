package supersymmetry.common.metatileentities.multi.rocket;

import cam72cam.mod.entity.ModdedEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.metatileentity.multiblock.SuSyPredicates;
import supersymmetry.common.blocks.BlockRocketAssemblerCasing;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.entities.EntityRocket;
import supersymmetry.common.entities.EntityTransporterErector;

import java.util.List;

public class MetaTileEntityLaunchPad extends MultiblockWithDisplayBase {
    private AxisAlignedBB trainAABB;
    private EntityTransporterErector selectedErector;
    private EntityRocket selectedRocket;
    private LaunchPadState state = LaunchPadState.EMPTY;
    private double supportAngle = Math.PI / 4;

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
                .aisle("DDDCCCCCCCDDD", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ")
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
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.trainAABB = null;
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
            case EMPTY:
                if (this.getOffsetTimer() % 20 == 0) {
                    updateSelectedErector();
                    if (this.selectedErector != null) {
                        this.state = LaunchPadState.LOADING;
                        this.selectedErector.setLiftingMode(EntityTransporterErector.LiftingMode.UP);
                    }
                } else {
                    break;
                }
            case LOADING:
                if (this.selectedErector == null || this.selectedErector.isDead()) {
                    this.state = LaunchPadState.EMPTY;
                    break;
                }
                this.supportAngle = this.selectedErector.getLifterAngle();
                if (this.selectedErector.getLifterAngle() >= Math.PI / 2) {
                    this.selectedErector.setRocketLoaded(false);
                    spawnRocket();
                    this.state = LaunchPadState.LOADED;
                } else {
                    break;
                }
            case LOADED:
                if (this.selectedRocket == null || this.selectedRocket.isDead) {
                    findRocket();
                    if (this.selectedRocket == null || this.selectedRocket.isDead) {
                        this.state = LaunchPadState.EMPTY;
                        break;
                    }
                }
                if (this.getInputRedstoneSignal(this.getFrontFacing(), false) == 0) {
                    break;
                }
                this.state = LaunchPadState.LAUNCHING;
            case LAUNCHING:
                if (this.selectedErector != null) {
                    this.selectedErector.setLiftingMode(EntityTransporterErector.LiftingMode.DOWN);
                }
                if (this.selectedRocket == null || this.selectedRocket.isDead) {
                    findRocket();
                    if (this.selectedRocket == null || this.selectedRocket.isDead) {
                        this.state = LaunchPadState.EMPTY;
                        break;
                    }
                }
                this.supportAngle = Math.max(Math.PI / 4, this.supportAngle - (0.087 / 20));
                if (this.supportAngle <= Math.PI / 4 && !this.selectedRocket.isCountDownStarted()) {
                    this.selectedRocket.startCountdown();
                }
                if (this.selectedRocket.posY > this.getLaunchPosition().y + 40) {
                    this.state = LaunchPadState.EMPTY;
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

    public enum LaunchPadState {
        EMPTY, // No rocket transporter has been selected, nor is there any rocket in the launch pad.
        LOADING, // A rocket transporter has been selected, causing it to begin the erecting process.
        LOADED, // A rocket has been loaded into the launch pad. Players should be able to enter through physical rocket supports and remotely launch the rocket.
        LAUNCHING // The rocket supports retract and the engines are turned on.
    }
}
