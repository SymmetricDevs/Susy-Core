package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.Recipe;
import gregtech.api.util.GTTransferUtils;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.SusyLog;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.common.blocks.BlockSuSyMultiblockCasing;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.entities.EntityDrone;

public class MetaTileEntityDronePad extends RecipeMapMultiblockController {

    private AxisAlignedBB landingAreaBB;
    public EntityDrone drone;

    public MetaTileEntityDronePad(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.DRONE_PAD);
        this.recipeMapWorkable = new DronePadWorkable(this);
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return false;
    }

    @NotNull
    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle(" CCC ", "     ", "     ")
                .aisle("CPPPC", " AAA ", " AAA ")
                .aisle("CPPPC", " AAA ", " AAA ")
                .aisle("CPPPC", " AAA ", " AAA ")
                .aisle(" CSC ", "     ", "     ")
                .where(' ', any())
                .where('A', air())
                .where('S', this.selfPredicate())
                .where('C', states(this.getCasingState()).or(autoAbilities(true, false, true, true, false, false, false)))
                .where('P', states(this.getPadState()))
                .build();
    }

    protected IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    protected IBlockState getPadState() {
        return SuSyBlocks.MULTIBLOCK_CASING.getState(BlockSuSyMultiblockCasing.CasingType.DRONE_PAD);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityDronePad(this.metaTileEntityId);
    }

    public boolean hasDrone() {
        return this.drone != null && !this.drone.isDead;
    }

    public void spawnDroneEntity() {
        drone = new EntityDrone(this.getWorld(), this.getDroneSpawnPosition());
        this.getWorld().spawnEntity(drone);
    }

    public BlockPos getDroneSpawnPosition() {
        net.minecraft.util.math.BlockPos offset = new net.minecraft.util.math.BlockPos(0, 1, 1.5);

        switch (this.getFrontFacing()) {
            case EAST -> {
                offset = offset.rotate(Rotation.CLOCKWISE_90);
            }
            case SOUTH -> {
                offset = offset.rotate(Rotation.CLOCKWISE_180);
            }
            case WEST -> {
                offset = offset.rotate(Rotation.COUNTERCLOCKWISE_90);
            }
            default -> {
            }
        }

        return getPos().add(offset);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.setStructureAABB();
    }

    public void setStructureAABB() {
        // Had to make it overshoot a little :(
        net.minecraft.util.math.BlockPos offsetBottomLeft = new net.minecraft.util.math.BlockPos(-1, 1, -1);
        net.minecraft.util.math.BlockPos offsetTopRight = new net.minecraft.util.math.BlockPos(1, 2, -3);

        switch (this.getFrontFacing()) {
            case EAST -> {
                offsetBottomLeft = offsetBottomLeft.rotate(Rotation.CLOCKWISE_90);
                offsetTopRight = offsetTopRight.rotate(Rotation.CLOCKWISE_90);
            }
            case SOUTH -> {
                offsetBottomLeft = offsetBottomLeft.rotate(Rotation.CLOCKWISE_180);
                offsetTopRight = offsetTopRight.rotate(Rotation.CLOCKWISE_180);
            }
            case WEST -> {
                offsetBottomLeft = offsetBottomLeft.rotate(Rotation.COUNTERCLOCKWISE_90);
                offsetTopRight = offsetTopRight.rotate(Rotation.COUNTERCLOCKWISE_90);
            }
            default -> {
            }
        }

        this.landingAreaBB = new AxisAlignedBB(getPos().add(offsetBottomLeft), getPos().add(offsetTopRight));
    }

    public static class DronePadWorkable extends MultiblockRecipeLogic {

        public DronePadWorkable(RecipeMapMultiblockController tileEntity) {
            super(tileEntity);
        }

        @NotNull
        @Override
        public MetaTileEntityDronePad getMetaTileEntity() {
            return (MetaTileEntityDronePad) super.getMetaTileEntity();
        }

        @Override
        protected void setupRecipe(Recipe recipe) {
            super.setupRecipe(recipe);
            SusyLog.logger.info("Spawning Drone");
            this.getMetaTileEntity().spawnDroneEntity();
        }

        @Override
        protected void completeRecipe() {
            if (this.getMetaTileEntity().hasDrone()) {
                super.completeRecipe();
            } else {
                this.performMaintenanceMufflerOperations();
                this.progressTime = 0;
                this.setMaxProgress(0);
                this.recipeEUt = 0;
                this.fluidOutputs = null;
                this.itemOutputs = null;
                this.hasNotEnoughEnergy = false;
                this.wasActiveAndNeedsUpdate = true;
                this.parallelRecipesPerformed = 0;
                this.overclockResults = new int[]{0, 0};
            }
        }
    }

}
