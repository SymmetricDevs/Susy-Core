package supersymmetry.common.metatileentities.multi.rocket;

import cam72cam.immersiverailroading.entity.EntityRollingStock;
import cam72cam.mod.entity.ModdedEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.metatileentity.multiblock.SuSyPredicates;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.common.blocks.BlockRocketAssemblerCasing;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.entities.EntityTransporterErector;

import java.util.List;

public class MetaTileEntityLaunchPad extends RecipeMapMultiblockController {
    private AxisAlignedBB trainAABB;
    private EntityRollingStock selectedRollingStock;
    private EntityTransporterErector selectedErector;

    public MetaTileEntityLaunchPad(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.ROCKET_LAUNCH_PAD);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityLaunchPad(metaTileEntityId);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("DDDDDDDDDDDDD", "     RRR     ")
                .aisle("DDDDDDDDDDDDD", "     RRR     ")
                .aisle("DDDDDDDDDDDDD", "     RRR     ")
                .aisle("DDDDDDDDDDDDD", "     RRR     ")
                .aisle("DDDDDDDDDDDDD", "     RRR     ")
                .aisle("DDDDDDDDDDDDD", "     RRR     ")
                .aisle("DDDDDDDDDDDDD", "     RRR     ")
                .aisle("DDDCCCCCCCDDD", "             ")
                .aisle("DDDCCCCCCCDDD", "             ")
                .aisle("DDDCCCCCCCDDD", "             ")
                .aisle("DDDCCCCCCCDDD", "             ")
                .aisle("DDDCCCCCCCDDD", "             ")
                .aisle("DDDCCCCCCCDDD", "             ")
                .aisle("DDDCCCCCCCDDD", "             ")
                .aisle("DDDDDDDDDDDDD", "             ")
                .aisle("DDDDDDDDDDDDD", "     FFF     ")
                .aisle("DDDDDDSDDDDDD", "             ")
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
        net.minecraft.util.math.BlockPos offsetBottomLeft = new net.minecraft.util.math.BlockPos(6, 1, 9);
        net.minecraft.util.math.BlockPos offsetTopRight = new net.minecraft.util.math.BlockPos(-6, 3, 17);

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


    @Override
    protected void updateFormedValid() {
        super.updateFormedValid();

        if (this.getOffsetTimer() % 20 == 0) {
            updateSelectedErector();
        }
    }

    private void updateSelectedErector() {
        if (this.selectedErector == null) {
            List<ModdedEntity> trains = getWorld().getEntitiesWithinAABB(ModdedEntity.class, this.trainAABB);

            if (!trains.isEmpty()) {
                for (ModdedEntity forgeTrainEntity : trains) {
                    if (forgeTrainEntity.getSelf() instanceof EntityTransporterErector rollingStock) {
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
}
