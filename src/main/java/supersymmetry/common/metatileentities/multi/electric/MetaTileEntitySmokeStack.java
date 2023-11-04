package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
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

import java.util.List;

import static gregtech.api.util.RelativeDirection.*;

public class MetaTileEntitySmokeStack extends RecipeMapMultiblockController {

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

        //stop if not loaded and ensure only occurs every second
        if(this.getWorld().isRemote || !(this.getOffsetTimer() == 0)  || this.inputFluidInventory == null ) { return; }

        //get tanks to drain
        List<IMultipleTankHandler.MultiFluidTankEntry> tanksToDrain = this.getInputFluidInventory().getFluidTanks();

        //if gas is not found and is not present in name [regex checks for gas not preceded or followed by any letters]
        for (IMultipleTankHandler.MultiFluidTankEntry currTank : tanksToDrain) {
            //if no fluids in tank, or if it's not gaseous in name or based on forge continue
            if (currTank.getFluid() == null ||
                    //checks if fluid is not considered gaseous by forge and if it also doesn't have gas in its name
                    (!currTank.getFluid().getFluid().isGaseous() &&
                    !currTank.getFluid().getUnlocalizedName().matches("(?<![A-Za-z])(gas)(?![A-Za-z])")) ||
                    //checks if any fluid input from any flare stack recipe matches current fluid
                    SuSyRecipeMaps.FLARE_STACK.getRecipeList().stream().anyMatch(recipe -> recipe.getFluidInputs().stream().anyMatch(gtRecipeInput -> gtRecipeInput.getInputFluidStack() == currTank.getFluid()))) {
                continue;
            }

            //perform draining if and only if fluid is a gas
            currTank.drain(currTank.getFluidAmount(), true);
        }
    }

    public void formStructure(PatternMatchContext context) {
        super.formStructure(context);
    }

}


