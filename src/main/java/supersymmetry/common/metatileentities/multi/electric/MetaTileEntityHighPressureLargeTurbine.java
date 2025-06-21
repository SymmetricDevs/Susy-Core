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
import gregtech.api.util.RelativeDirection;
import gregtech.client.renderer.ICubeRenderer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.capability.impl.SuSyTurbineRecipeLogic;
import supersymmetry.common.blocks.BlockAlternatorCoil;
import supersymmetry.common.blocks.SuSyBlocks;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;

import static supersymmetry.api.blocks.VariantHorizontalRotatableBlock.FACING;

public class MetaTileEntityHighPressureLargeTurbine extends MetaTileEntitySUSYLargeTurbine {

    public MetaTileEntityHighPressureLargeTurbine(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, int tier, IBlockState casingState, IBlockState rotorState, ICubeRenderer casingRenderer, ICubeRenderer frontOverlay) {
        super(metaTileEntityId, recipeMap, tier, casingState, rotorState, casingRenderer, frontOverlay);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityHighPressureLargeTurbine(metaTileEntityId, recipeMap, tier, casingState, rotorState, casingRenderer, frontOverlay);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        // Different characters use common constraints. Copied from GCyM
        TraceabilityPredicate casingPredicate = states(this.casingState).setMinGlobalLimited(52)
                .or(abilities(MultiblockAbility.IMPORT_ITEMS).setPreviewCount(1));
        TraceabilityPredicate maintenance = abilities(MultiblockAbility.MAINTENANCE_HATCH).setMaxGlobalLimited(1);

        return FactoryBlockPattern.start()
                .aisle("GAAAAAAAAAAAAO", "GAAAAAAAAAAAAO", "G     AA     O")
                .aisle("GAAAAAAAAAAAAO", "GDDDDADDDDCCCF", "GAAAAAAAAAAAAO")
                .aisle("GAAAAAAAAAAAAO", "GSAAAAAAAAAAAO", "G     AA     O")
                .where('S', selfPredicate())
                .where('A', casingPredicate
                       .or(autoAbilities(false, false, false, false, false, false, false))
                       .or(maintenance))
                .where('O', casingPredicate
                        .or(autoAbilities(false, false, false, false, false, true, false))
                        .or(maintenance))
                .where('C', coilOrientation())
                .where('D', rotorOrientation())
                .where('F', abilities(MultiblockAbility.OUTPUT_ENERGY))
                .where('G', casingPredicate
                        .or(autoAbilities(false, false, false, false, true, false, false))
                        .or(maintenance))
                .where(' ', any())
                .build();
    }

}
