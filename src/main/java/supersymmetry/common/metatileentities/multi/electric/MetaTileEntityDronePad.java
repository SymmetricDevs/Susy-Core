package supersymmetry.common.metatileentities.multi.electric;

import javax.annotation.Nonnull;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
import it.unimi.dsi.fastutil.ints.IntLists;
import supersymmetry.api.SusyLog;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.api.recipes.properties.DimensionProperty;
import supersymmetry.common.blocks.BlockSuSyMultiblockCasing;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.entities.EntityDrone;

public class MetaTileEntityDronePad extends RecipeMapMultiblockController {

    private AxisAlignedBB landingAreaBB;
    private EntityDrone drone = null;
    public boolean droneReachedSky;

    public MetaTileEntityDronePad(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.DRONE_PAD);
        this.recipeMapWorkable = new DronePadWorkable(this);
    }

    protected static IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    protected static IBlockState getPadState() {
        return SuSyBlocks.MULTIBLOCK_CASING.getState(BlockSuSyMultiblockCasing.CasingType.DRONE_PAD);
    }

    @NotNull
    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle(" CCC ")
                .aisle("CPPPC")
                .aisle("CPPPC")
                .aisle("CPPPC")
                .aisle(" CSC ")
                .where(' ', any())
                .where('S', selfPredicate())
                .where('C', states(getCasingState()).setMinGlobalLimited(6)
                        .or(autoAbilities(true, false, true, true, false, false, false)))
                .where('P', states(getPadState()))
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityDronePad(metaTileEntityId);
    }

    @Nullable
    public EntityDrone getDrone() {
        return this.drone;
    }

    public void setDrone(@Nullable EntityDrone drone) {
        this.drone = drone;
    }

    public void setDroneDead(boolean setReachedSky) {
        if (this.drone != null) {
            this.drone.setDead();
            this.drone = null;
        }
        this.droneReachedSky = setReachedSky;
    }

    public boolean hasDrone() {
        if (getDrone() != null && !getDrone().isDead) {
            for (EntityDrone entity : this.getWorld().getEntitiesWithinAABB(EntityDrone.class, this.landingAreaBB)) {
                if (entity == getDrone()) {
                    return true;
                }
            }
        }
        return false;
    }

    public void spawnDroneEntity(boolean descending) {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        nbttagcompound.setString("id", "susy:drone");
        Vec3d pos = this.getDroneSpawnPosition(descending);

        EntityDrone drone = ((EntityDrone) AnvilChunkLoader.readWorldEntityPos(nbttagcompound, this.getWorld(), pos.x,
                pos.y, pos.z, true));

        if (drone != null) {
            setDrone(drone.withPadPos(getPos()));
        }

        if (getDrone() != null) {
            getDrone().setRotationFromFacing(this.getFrontFacing());
            if (descending) {
                getDrone().setDescendingMode();
                getDrone().setPadAltitude(this.getPos().getY());
            }
        } else {
            SusyLog.logger.error("Failed to spawn drone entity at: {}", pos);
        }
    }

    public Vec3d getDroneSpawnPosition(boolean descending) {
        double altitude = descending ? 296.D : this.getPos().getY() + 1.;

        switch (this.getFrontFacing()) {
            case EAST -> {
                return new Vec3d(this.getPos().getX() - 1.5, altitude, this.getPos().getZ() + 0.5);
            }
            case SOUTH -> {
                return new Vec3d(this.getPos().getX() + 0.5, altitude, this.getPos().getZ() - 1.5);
            }
            case WEST -> {
                return new Vec3d(this.getPos().getX() + 2.5, altitude, this.getPos().getZ() + 0.5);
            }
            default -> {
                return new Vec3d(this.getPos().getX() + 0.5, altitude, this.getPos().getZ() + 2.5);
            }
        }
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.setStructureAABB();
    }

    public void setStructureAABB() {
        double x = this.getPos().getX();
        double y = this.getPos().getY();
        double z = this.getPos().getZ();

        switch (this.getFrontFacing()) {

            case EAST -> {
                this.landingAreaBB = new AxisAlignedBB(x - 1, y + 1, z + 1, x - 3, y + 2, z - 1);
            }
            case SOUTH -> {
                this.landingAreaBB = new AxisAlignedBB(x - 1, y + 1, z - 1, x + 1, y + 2, z - 3);
            }
            case WEST -> {
                this.landingAreaBB = new AxisAlignedBB(x + 1, y + 1, z - 1, x + 3, y + 2, z + 1);
            }
            default -> {
                this.landingAreaBB = new AxisAlignedBB(x + 1, y + 1, z + 1, x - 1, y + 2, z + 3);
            }
        }
    }

    public boolean checkRecipe(@NotNull Recipe recipe) {
        for (int dimension : recipe.getProperty(DimensionProperty.getInstance(), IntLists.EMPTY_LIST)) {
            if (dimension == this.getWorld().provider.getDimension()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public NBTTagCompound writeToNBT(@NotNull NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("droneReachedSky", this.droneReachedSky);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.droneReachedSky = data.getBoolean("droneReachedSky");
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(this.droneReachedSky);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.droneReachedSky = buf.readBoolean();
    }

    @Override
    public boolean isMultiblockPartWeatherResistant(@Nonnull IMultiblockPart part) {
        return true;
    }

    @Override
    public boolean getIsWeatherOrTerrainResistant() {
        return true;
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return false;
    }

    @Override
    public boolean allowsExtendedFacing() {
        return false;
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
        public boolean isAllowOverclocking() {
            return false;
        }

        @Override
        public boolean checkRecipe(@NotNull Recipe recipe) {
            return ((MetaTileEntityDronePad) metaTileEntity).checkRecipe(recipe) && super.checkRecipe(recipe);
        }

        @Override
        protected void setupRecipe(Recipe recipe) {
            super.setupRecipe(recipe);
            this.getMetaTileEntity().spawnDroneEntity(false);
        }

        @Override
        protected void updateRecipeProgress() {
            super.updateRecipeProgress();

            if (!this.getMetaTileEntity().droneReachedSky && this.getMetaTileEntity().getDrone() != null &&
                    this.getMetaTileEntity().getDrone().reachedSky()) {
                this.getMetaTileEntity().setDroneDead(true);
            }

            if (this.getMetaTileEntity().getDrone() == null && !this.getMetaTileEntity().droneReachedSky) {
                this.invalidate();
            }

            if (maxProgressTime - progressTime == 240 && this.getMetaTileEntity().droneReachedSky) {
                this.getMetaTileEntity().spawnDroneEntity(true);
            }
        }

        @Override
        protected void completeRecipe() {
            if (this.getMetaTileEntity().hasDrone()) {
                super.completeRecipe();
            } else {
                this.progressTime = 0;
                this.setMaxProgress(0);
                this.recipeEUt = 0;
                this.fluidOutputs = null;
                this.itemOutputs = null;
                this.hasNotEnoughEnergy = false;
                this.wasActiveAndNeedsUpdate = true;
                this.parallelRecipesPerformed = 0;
                this.overclockResults = new int[] { 0, 0 };
            }
            this.getMetaTileEntity().setDroneDead(false);
        }
    }
}
