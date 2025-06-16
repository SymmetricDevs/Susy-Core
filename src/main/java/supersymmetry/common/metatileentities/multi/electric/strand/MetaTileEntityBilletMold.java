package supersymmetry.common.metatileentities.multi.electric.strand;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.client.renderer.ICubeRenderer;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.metatileentity.multiblock.SuSyMultiblockAbilities;
import supersymmetry.client.renderer.textures.SusyTextures;

public class MetaTileEntityBilletMold extends MetaTileEntityStrandMold {
    public MetaTileEntityBilletMold(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    protected int getRequiredMetal() {
        return 2592;
    }

    @Override
    protected double getOutputThickness() {
        return 1 / 3.;
    }

    @Override
    protected double getOutputWidth() {
        return 1 / 3.;
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("#CCC#", "#CCC#", "#CCC#", "#CCC#", "#CCC#")
                .aisle("COOOC", "CPPPC", "CPPPC", "CPPPC", "CIIIC")
                .aisle("COMOC", "CP PC", "CP PC", "CP PC", "CIIIC")
                .aisle("COOOC", "CPPPC", "CPPPC", "CPPPC", "CIIIC")
                .aisle("#CCC#", "#CCC#", "#CCC#", "#CCC#", "#CCC#")
                .where('C', states(getCasingState()).or(autoAbilities()))
                .where('P', states(getPipeCasingState()))
                .where('M', abilities(SuSyMultiblockAbilities.STRAND_EXPORT))
                .where('I', abilities(MultiblockAbility.IMPORT_FLUIDS).setPreviewCount(1).or(states(getPipeCasingState())))
                .where('O', abilities(MultiblockAbility.EXPORT_FLUIDS).setPreviewCount(1).or(states(getPipeCasingState())))
                .where('S', selfPredicate())
                .where(' ', air())
                .build();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityBilletMold(metaTileEntityId);
    }

    @Override
    protected @NotNull ICubeRenderer getFrontOverlay() {
        return SusyTextures.BILLET_MOLD_OVERLAY;
    }
}
