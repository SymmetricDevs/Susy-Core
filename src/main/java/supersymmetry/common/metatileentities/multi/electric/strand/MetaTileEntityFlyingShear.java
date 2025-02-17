package supersymmetry.common.metatileentities.multi.electric.strand;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.pattern.BlockPattern;
import gregtech.client.renderer.ICubeRenderer;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.capability.Strand;

public class MetaTileEntityFlyingShear extends MetaTileEntityStrandShaper {
    public MetaTileEntityFlyingShear(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    protected long getVoltage() {
        return 64;
    }

    @Override
    protected void consumeInputsAndSetupRecipe() {

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
        return new MetaTileEntityFlyingShear(metaTileEntityId);
    }
}
