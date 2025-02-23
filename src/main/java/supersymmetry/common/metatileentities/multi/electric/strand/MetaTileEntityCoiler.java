package supersymmetry.common.metatileentities.multi.electric.strand;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.pattern.BlockPattern;
import gregtech.client.renderer.ICubeRenderer;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.capability.Strand;

public class MetaTileEntityCoiler extends MetaTileEntityStrandShaper {
    public MetaTileEntityCoiler(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public long getVoltage() {
        return 8;
    }

    @Override
    protected boolean consumeInputsAndSetupRecipe() {
        return false;
    }

    @Override
    protected Strand resultingStrand() {
        return null;
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return null;
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return null;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityCoiler(metaTileEntityId);
    }
}
