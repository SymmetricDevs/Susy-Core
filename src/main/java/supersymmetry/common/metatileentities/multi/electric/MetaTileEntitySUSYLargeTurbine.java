package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.GTValues;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.FuelMultiblockController;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.RecipeMap;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import gregtech.common.metatileentities.multi.electric.generator.*;
import supersymmetry.common.blocks.BlockAlternatorCoil;
import supersymmetry.common.blocks.BlockTurbineRotor;
import supersymmetry.common.blocks.SuSyBlocks;

import javax.annotation.Nonnull;
import java.util.List;

public class MetaTileEntitySUSYLargeTurbine extends FuelMultiblockController implements ITieredMetaTileEntity {

    public final int tier;

    public final IBlockState casingState;
    public final ICubeRenderer casingRenderer;
    public final ICubeRenderer frontOverlay;

    public IFluidHandler exportFluidHandler;

    public MetaTileEntitySUSYLargeTurbine(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, int tier, IBlockState casingState, ICubeRenderer casingRenderer, ICubeRenderer frontOverlay) {
        super(metaTileEntityId, recipeMap, tier);
        this.casingState = casingState;
        this.casingRenderer = casingRenderer;
        this.frontOverlay = frontOverlay;
        this.tier = tier;
        this.recipeMapWorkable = new LargeTurbineWorkableHandler(this, tier);
        this.recipeMapWorkable.setMaximumOverclockVoltage(GTValues.V[tier]);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntitySUSYLargeTurbine(metaTileEntityId, recipeMap, tier, casingState, casingRenderer, frontOverlay);
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.exportFluidHandler = null;
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.exportFluidHandler = new FluidTankList(true, getAbilities(MultiblockAbility.EXPORT_FLUIDS));
        ((LargeTurbineWorkableHandler) this.recipeMapWorkable).updateTanks();
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        if (isStructureFormed()) {
            FluidStack fuelStack = ((LargeTurbineWorkableHandler) recipeMapWorkable).getInputFluidStack();
            int fuelAmount = fuelStack == null ? 0 : fuelStack.amount;

            ITextComponent fuelName = new TextComponentTranslation(fuelAmount == 0 ? "gregtech.fluid.empty" : fuelStack.getUnlocalizedName());
            textList.add(new TextComponentTranslation("gregtech.multiblock.turbine.fuel_amount", fuelAmount, fuelName));
        }
        super.addDisplayText(textList);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("GAAAAAAAA", "GAAAAAAAA", "G   A   A")
                .aisle("GAAAAAAAA", "GDDDDCCCF", "GAAAAAAAA")
                .aisle("GAAAAAAAA", "GSAAAAAAA", "G   A   A")
                .where('S', selfPredicate())
                .where('A', states(new IBlockState[]{MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_TURBINE_CASING)})
                        .or(autoAbilities(false, true, false, false, false, true, false)))
                .where('C', states(new IBlockState[]{SuSyBlocks.ALTERNATOR_COIL.getState(BlockAlternatorCoil.AlternatorCoilType.COPPER)}))
                .where('D', states(new IBlockState[]{SuSyBlocks.TURBINE_ROTOR.getState(BlockTurbineRotor.BlockTurbineRotorType.STEEL)}))
                .where('F', abilities(MultiblockAbility.OUTPUT_ENERGY))
                .where('G', states(new IBlockState[]{MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_TURBINE_CASING)})
                        .or(autoAbilities(false, false, false, false, true, false, false)))
                .where(' ', any())
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return casingRenderer;
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return frontOverlay;
    }

    @Override
    public boolean hasMufflerMechanics() {
        return false;
    }

    @Override
    public int getTier() {
        return tier;
    }

    @Override
    public boolean canVoidRecipeItemOutputs() {
        return true;
    }

    @Override
    public boolean canVoidRecipeFluidOutputs() {
        return true;
    }

    @Override
    protected boolean shouldShowVoidingModeButton() {
        return false;
    }
}
