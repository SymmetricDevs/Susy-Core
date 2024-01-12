package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.FuelMultiblockController;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.BlockInfo;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.capability.impl.SuSyTurbineRecipeLogic;
import supersymmetry.common.blocks.BlockAlternatorCoil;
import supersymmetry.common.blocks.BlockTurbineRotor;
import supersymmetry.common.blocks.SuSyBlocks;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;

import static supersymmetry.api.blocks.ISuSyHorizontalOrientable.FACING;

public class MetaTileEntitySUSYLargeTurbine extends FuelMultiblockController implements ITieredMetaTileEntity {

    public final int tier;

    public final IBlockState casingState;
    public final ICubeRenderer casingRenderer;
    public final ICubeRenderer frontOverlay;


    public MetaTileEntitySUSYLargeTurbine(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, int tier, IBlockState casingState, ICubeRenderer casingRenderer, ICubeRenderer frontOverlay) {
        super(metaTileEntityId, recipeMap, tier);
        this.casingState = casingState;
        this.casingRenderer = casingRenderer;
        this.frontOverlay = frontOverlay;
        this.tier = tier;
        this.recipeMapWorkable = new SuSyTurbineRecipeLogic(this);
        this.recipeMapWorkable.setMaximumOverclockVoltage(GTValues.V[tier]);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntitySUSYLargeTurbine(metaTileEntityId, recipeMap, tier, casingState, casingRenderer, frontOverlay);
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        if (isStructureFormed()) {
            FluidStack fuelStack = ((SuSyTurbineRecipeLogic) recipeMapWorkable).getInputFluidStack();
            int fuelAmount = fuelStack == null ? 0 : fuelStack.amount;

            ITextComponent fuelName = new TextComponentTranslation(fuelAmount == 0 ? "gregtech.fluid.empty" : fuelStack.getUnlocalizedName());
            textList.add(new TextComponentTranslation("gregtech.multiblock.turbine.fuel_amount", fuelAmount, fuelName));
        }
        super.addDisplayText(textList);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        TraceabilityPredicate maintenance = autoAbilities(false, true, false, false, false, false, false).setMaxGlobalLimited(1);

        return FactoryBlockPattern.start()
                .aisle("GAAAAAAAO", "GAAAAAAAO", "G   A   O")
                .aisle("GAAAAAAAO", "GDDDDCCCF", "GAAAAAAAO")
                .aisle("GAAAAAAAO", "GSAAAAAAO", "G   A   O")
                .where('S', selfPredicate())
                .where('A', states(MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_TURBINE_CASING))
                       .or(autoAbilities(false, false, false, false, false, false, false))
                       .or(maintenance))
                .where('O', states(MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_TURBINE_CASING))
                        .or(autoAbilities(false, false, false, false, false, true, false))
                        .or(maintenance))
                .where('C', coilOrientation())
                .where('D', rotorOrientation())
                .where('F', abilities(MultiblockAbility.OUTPUT_ENERGY))
                .where('G', states(MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_TURBINE_CASING))
                        .or(autoAbilities(false, false, true, false, true, false, false))
                        .or(maintenance))
                .where(' ', any())
                .build();
    }

    protected TraceabilityPredicate rotorOrientation() {
        Supplier<BlockInfo[]> supplier = () -> new BlockInfo[]{new BlockInfo(steelRotorState().withProperty(FACING, EnumFacing.WEST))};
        return new TraceabilityPredicate(blockWorldState -> {
            IBlockState state = blockWorldState.getBlockState();
            if (!(state.getBlock() instanceof BlockTurbineRotor)) return false;
            EnumFacing facing = MetaTileEntitySUSYLargeTurbine.this.getFrontFacing();
            EnumFacing rotorFacing = EnumFacing.byHorizontalIndex((facing.getHorizontalIndex() +1) % 4).getOpposite();
            //makes sure rotor's front faces direction rotated 90 degrees CW of controller front, then the opposite is gotten such that it faces 90 CCW from controller front
            return state == steelRotorState().withProperty(FACING, rotorFacing) || state == steelRotorState().withProperty(FACING, rotorFacing.getOpposite());
        }, supplier);
    }

    protected TraceabilityPredicate coilOrientation() {
        Supplier<BlockInfo[]> supplier = () -> new BlockInfo[]{new BlockInfo(copperCoilState().withProperty(FACING, EnumFacing.WEST))};
        return new TraceabilityPredicate(blockWorldState -> {
            IBlockState state = blockWorldState.getBlockState();
            if (!(state.getBlock() instanceof BlockAlternatorCoil)) return false;
            EnumFacing facing = MetaTileEntitySUSYLargeTurbine.this.getFrontFacing();
            //has coilFacing as same facing as rotor, but either this or the opposite is acceptable
            EnumFacing coilFacing = EnumFacing.byHorizontalIndex((facing.getHorizontalIndex() +1) % 4);
            return state == copperCoilState().withProperty(FACING, coilFacing) || state == copperCoilState().withProperty(FACING, coilFacing.getOpposite());
        }, supplier);
    }

    protected IBlockState steelRotorState() {
        return SuSyBlocks.TURBINE_ROTOR.getState(BlockTurbineRotor.BlockTurbineRotorType.STEEL);
    }

    protected IBlockState copperCoilState() {
        return SuSyBlocks.ALTERNATOR_COIL.getState(BlockAlternatorCoil.AlternatorCoilType.COPPER);
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
