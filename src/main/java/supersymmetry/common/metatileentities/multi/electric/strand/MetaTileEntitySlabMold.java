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
                .aisle("#CCCCC#", "#CCCCC#", "#CCCCC#", "#CCCCC#", "#CCCCC#")
                .aisle("COOOOOC", "CPPPPPC", "CPPPPPC", "CPPPPPC", "CIIIIIC")
                .aisle("CO M OC", "CP   PC", "CP   PC", "CP   PC", "CIIIIIC")
                .aisle("COOOOOC", "CPPPPPC", "CPPPPPC", "CPPPPPC", "CIIIIIC")
                .aisle("#CCCCC#", "#CCCCC#", "#CCSCC#", "#CCCCC#", "#CCCCC#")
                .where('C', states(getCasingState()).or(autoAbilities()))
                .where('P', states(getPipeCasingState()))
                .where('M', abilities(SuSyMultiblockAbilities.STRAND_EXPORT))
                .where('I', abilities(MultiblockAbility.IMPORT_FLUIDS).setPreviewCount(2).or(states(getPipeCasingState())))
                .where('O', abilities(MultiblockAbility.EXPORT_FLUIDS).setPreviewCount(1).or(states(getPipeCasingState())))
                .where('S', selfPredicate())
                .where(' ', air())
                .where('#', any())
                .build();

    }
    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntitySlabMold(metaTileEntityId);
    }

    @Override
    protected @NotNull ICubeRenderer getFrontOverlay() {
        return SusyTextures.SLAB_MOLD_OVERLAY;
    }
}
