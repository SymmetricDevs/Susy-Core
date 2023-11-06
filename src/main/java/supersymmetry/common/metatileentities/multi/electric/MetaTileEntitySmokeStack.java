package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockBoilerCasing.BoilerCasingType;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

import supersymmetry.api.capability.impl.NoEnergyMultiblockRecipeLogic;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.textures.SusyTextures;

import javax.annotation.Nonnull;

import java.util.Collections;

import static gregtech.api.util.RelativeDirection.*;

public class MetaTileEntitySmokeStack extends RecipeMapMultiblockController {

    //isActive check prevents running while mufflerTier is invalid
    //int mufflerTier = 1;

    public MetaTileEntitySmokeStack(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.SMOKE_STACK);
        this.recipeMapWorkable = new NoEnergyMultiblockRecipeLogic(this);
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntitySmokeStack(this.metaTileEntityId);
    }

    @Nonnull
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(FRONT, RIGHT, UP)
                .aisle("S")
                .aisle("P").setRepeatable(2,6)
                .aisle("F")
                .where('S', this.selfPredicate())
                .where('P', states(this.getPipeCasingState())
                        .or(abilities(MultiblockAbility.IMPORT_FLUIDS).setExactLimit(1)))
                .where('F', abilities(MultiblockAbility.MUFFLER_HATCH).setExactLimit(1))
                .build();
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return false;
    }

    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.SOLID_STEEL_CASING;
    }
    protected IBlockState getPipeCasingState() {
        return MetaBlocks.BOILER_CASING.getState(BoilerCasingType.STEEL_PIPE);
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return SusyTextures.SMOKE_STACK_OVERLAY;
    }

    @Override
    public boolean hasMufflerMechanics() {
        return true;
    }

    @Override
    public boolean getIsWeatherOrTerrainResistant() {
        return true;
    }

    public void update() {
        super.update();

        //skip processing if unloaded, inactive, or if no fluid inventory. Also limits speed through offset timer
        if(this.getWorld().isRemote || !this.isActive() || !(this.getOffsetTimer() % 100 == 0)  || this.inputFluidInventory == null) { return; }

        //passes over all tanks in fluid input inventory
        for (IMultipleTankHandler.MultiFluidTankEntry currTank : this.getInputFluidInventory().getFluidTanks()) {
            //checks for fluid existence, gaseousness, and absence in recipes of flare stack
            if (currTank.getFluid() == null || !currTank.getFluid().getFluid().isGaseous() || SuSyRecipeMaps.FLARE_STACK.findRecipe(Integer.MAX_VALUE, Collections.emptyList(), Collections.singletonList(currTank.getFluid())) != null) {
                continue;
            }

            //perform draining if and only if all above conditions are false
            currTank.drain(currTank.getFluidAmount(), true);
        }
    }

    /* I can't figure out how to tell if something is a muffler hatch or not
    public void formStructure(PatternMatchContext context) {
        super.formStructure(context);

        //don't look for muffler in unformed structure
        if (!this.isStructureFormed()) { return; }

        //go up structure until muffler hatch is found
        BlockPos currPos = this.getPos();
        while (!(this.getWorld().getTileEntity(currPos) == GregTechAPI.MTE_REGISTRY.) && currPos.getY() < this.getPos().getY() + 9) { currPos.add(0, 1, 0); }
        TileEntity.getKey(GregTechAPI)
        this.mufflerTier = ((MetaTileEntityMufflerHatch) this.getWorld().getBlockState(currPos)).getTier()

    }
     */

}


