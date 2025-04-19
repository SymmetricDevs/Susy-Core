package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.recipes.RecipeMap;
import gregtech.client.renderer.ICubeRenderer;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.recipes.SuSyRecipeMaps;

public class MetaTileEntityGasAtomizer extends RecipeMapMultiblockController {

    public MetaTileEntityGasAtomizer(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.GAS_ATOMIZER);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("  P  ", "  P  ", "  M  ", "     ", "     ", " EIE ", "     ")
                .aisle("CCPCC", "CCCCC", " HHH ", " HHH ", " HHH ", " HXH ", "CHHHC")
                .aisle("CCPCC", "CHHHC", "HHHHH", "HXXXH", "HXXXH", "HXXXH", "HHPHH")
                .aisle("CCOCC", "CHXHC", "HHXHH", "HXXXH", "HXXXH", "HXXXH", "HPJPH")
                .aisle("CCCCC", "CHHHC", "HHHHH", "HXXXH", "HXXXH", "HXXXH", "HHPHH")
                .aisle("CCCCC", "CCCCC", " HHH ", " HHH ", " HHH ", " HHH ", "CCCCC")
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return null;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityGasAtomizer(metaTileEntityId);
    }
}
