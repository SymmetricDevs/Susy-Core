package supersymmetry.common.metatileentities.multi.electric.strand;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.metatileentity.multiblock.SuSyMultiblockAbilities;

public class MetaTileEntitySlabMold extends MetaTileEntityStrandMold {
    public MetaTileEntitySlabMold(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    protected int getRequiredMetal() {
        return 2592;
    }

    @Override
    protected double getOutputThickness() {
        return 0.5F;
    }

    @Override
    protected double getOutputWidth() {
        return 2F;
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("CCCCC", "CCCCC", "CCCCC", "CCCCC", "CCCCC")
                .aisle("COOOC", "CPPPC", "CPPPC", "CPPPC", "CIIIC")
                .aisle("COMOC", "CP PC", "CP PC", "CP PC", "CIIIC")
                .aisle("COOOC", "CPPPC", "CPPPC", "CPPPC", "CIIIC")
                .aisle("CCCCC", "CCCCC", "CCSCC", "CCCCC", "CCCCC")
                .where('C', states(getCasingState()).or(autoAbilities()))
                .where('P', states(getPipeCasingState()))
                .where('M', abilities(SuSyMultiblockAbilities.STRAND_EXPORT))
                .where('I', abilities(MultiblockAbility.IMPORT_FLUIDS).or(states(getPipeCasingState())))
                .where('O', abilities(MultiblockAbility.EXPORT_FLUIDS).or(states(getPipeCasingState())))
                .where('S', selfPredicate())
                .where(' ', air())
                .build();
    }
    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntitySlabMold(metaTileEntityId);
    }
}
