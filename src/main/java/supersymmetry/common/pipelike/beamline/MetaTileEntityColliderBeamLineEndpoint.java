package supersymmetry.common.pipelike.beamline;

import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import supersymmetry.common.metatileentities.storage.MetaTileEntityBeamLineEndpoint;

public class MetaTileEntityColliderBeamLineEndpoint extends MetaTileEntityBeamLineEndpoint {


    public MetaTileEntityColliderBeamLineEndpoint(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, ColliderBeamLineType.INSTANCE);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityColliderBeamLineEndpoint(this.metaTileEntityId);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }
}
