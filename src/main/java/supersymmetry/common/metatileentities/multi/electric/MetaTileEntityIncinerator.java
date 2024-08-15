package supersymmetry.common.metatileentities.multi.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.metatileentities.SuSyMetaTileEntities;
import supersymmetry.common.metatileentities.multi.ItemVoidingMultiblockBase;

import java.util.List;

public class MetaTileEntityIncinerator extends ItemVoidingMultiblockBase {
    public static final int VOID_ONE_ITEM_EU = 10_000;
    public MetaTileEntityIncinerator(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }
    double currentFuelUsed = 0.0;
    double currentOxidizerUsed = 0.0;
    Recipe previous = null;
    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("CCC", "CFC", "CFC", " B ", " B ", " B ")
                .aisle("CGC", "F#F", "F#F", "BPB", "BPB", "BMB")
                .aisle("CSC", "CFC", "CFC", " B ", " B ", " B ")
                .where('C', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID)).or(abilities(MultiblockAbility.IMPORT_ITEMS).setExactLimit(1)).or(abilities(MultiblockAbility.IMPORT_FLUIDS).setExactLimit(2)))
                .where('F', states(MetaBlocks.BOILER_FIREBOX_CASING.getState(BlockFireboxCasing.FireboxCasingType.STEEL_FIREBOX)) )
                .where('B', frames(Materials.Aluminium))
                .where('G', states(MetaBlocks.MULTIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.GRATE_CASING)))
                .where('P', states(MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.STEEL_PIPE)))
                .where('M', abilities(MultiblockAbility.MUFFLER_HATCH))
                .where('S', selfPredicate())
                .where(' ', any())
                .where('#', air())
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityIncinerator(this.metaTileEntityId);
    }

    @Override
    protected @NotNull ICubeRenderer getFrontOverlay() {
        return SusyTextures.FLARE_STACK_OVERLAY;
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);

    }

    @Override
    public void doVoiding() {
        // do base voiding first
        super.doVoiding();

        var fluids = new FluidTankList(false, getAbilities(MultiblockAbility.IMPORT_FLUIDS));
        //Need ghost circuit 1 to boost with LGT fuels due to recipe map
        var items = getAbilities(MultiblockAbility.IMPORT_ITEMS).get(0);
        //TODO some sort of caching (recipemap lookups aren't exactly cheap)
        var rec = SuSyMetaTileEntities.BASIC_GAS_TURBINE.recipeMap.findRecipe(Integer.MAX_VALUE, items, fluids);
        if (previous != null && previous != rec) {
            currentFuelUsed = 0.0;
            currentOxidizerUsed = 0.0;
        }
        previous = rec;
        if(rec == null) {
            return;
        }
        int recipeTotalEu = rec.getEUt() * rec.getDuration();
        List<GTRecipeInput> fluidsIn = rec.getFluidInputs();
        if(fluidsIn.size() != 2) {
            return;
        }

        GTRecipeInput fuel = fluidsIn.get(0);
        GTRecipeInput oxidizer = fluidsIn.get(1);

        // Using double here is not exactly ideal, but it keeps integer rounding from causing issues when we divide things
        double fuelPerVoid = (double)VOID_ONE_ITEM_EU / ((double) recipeTotalEu / (double) fuel.getAmount());

        double fuelPerOxidizer = (double) fuel.getAmount() / (double) oxidizer.getAmount();

        int fuelAmount = fluids.getFluidTanks().get(fluids.getIndexOfFluid(fuel.getInputFluidStack())).getFluidAmount();
        int oxidizerAmount = fluids.getFluidTanks().get(fluids.getIndexOfFluid(fuel.getInputFluidStack())).getFluidAmount();

        double usableFuelAmount = Double.min(fuelAmount, fuelPerOxidizer * (double) oxidizerAmount);

        // Round down to the nearest even multiple of fuelPerVoid to avoid consuming a partial void worth of inputs
        usableFuelAmount -= usableFuelAmount % fuelPerVoid;
        // Floor here to ensure we don't overshoot the allowed bonus void count when rounding
        int maximumBonusVoids = (int) Math.floor(usableFuelAmount / fuelPerVoid);

        int numVoided = voidItems(maximumBonusVoids);
        double fuelUsed = ((double) numVoided) * fuelPerVoid;
        currentFuelUsed += fuelUsed;
        currentOxidizerUsed += fuelUsed / fuelPerOxidizer;

        // remove accumulated fuel consumption (to prevent cheese via using less than one unit of fuel/oxidizer per cycle for a slight free boost)
        if (currentFuelUsed >= 1.0) {
            fluids.drain(new FluidStack(fuel.getInputFluidStack().getFluid(), (int) currentFuelUsed), true);
            currentFuelUsed -= (int) currentFuelUsed;
        }

        if (currentOxidizerUsed >= 1.0) {
            fluids.drain(new FluidStack(oxidizer.getInputFluidStack().getFluid(), (int) currentOxidizerUsed), true);
            currentOxidizerUsed -= (int) currentOxidizerUsed;
        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(),
                this.isActive(), true);
    }

    @Override
    public boolean hasMufflerMechanics() {
        return false;
    }

    @Override
    protected boolean shouldShowVoidingModeButton() {
        return false;
    }
}
