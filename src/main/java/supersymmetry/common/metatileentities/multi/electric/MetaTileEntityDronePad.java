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
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.common.blocks.BlockSuSyMultiblockCasing;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.entities.EntityDrone;

public class MetaTileEntityDronePad extends RecipeMapMultiblockController {

    private AxisAlignedBB landingAreaBB;
    public EntityDrone drone = null;

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

        NBTTagCompound nbttagcompound = new NBTTagCompound();
        nbttagcompound.setString("id", "susy:drone");
        Vec3d pos = this.getDroneSpawnPosition();

        drone = (EntityDrone) AnvilChunkLoader.readWorldEntityPos(nbttagcompound, this.getWorld(), pos.x, pos.y, pos.z, true);

    }

    public Vec3d getDroneSpawnPosition() {

        switch (this.getFrontFacing()) {
            case EAST -> {
                return new Vec3d(this.getPos().getX() - 1.5, this.getPos().getY() + 1., this.getPos().getZ() + 0.5);
            }
            case SOUTH -> {
                return new Vec3d(this.getPos().getX() + 0.5, this.getPos().getY() + 1., this.getPos().getZ() - 1.5);
            }
            case WEST -> {
                return new Vec3d(this.getPos().getX() + 2.5, this.getPos().getY() + 1., this.getPos().getZ() + 0.5);
            }
            default -> {
                return new Vec3d(this.getPos().getX() + 0.5, this.getPos().getY() + 1., this.getPos().getZ() + 2.5);
            }
        }

    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.setStructureAABB();
    }

    public void setStructureAABB() {
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
