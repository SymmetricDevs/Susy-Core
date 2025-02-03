package supersymmetry.common.metatileentities.multiblockpart;

import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import supersymmetry.api.capability.IStrandProvider;
import supersymmetry.api.capability.Strand;
import supersymmetry.api.capability.SuSyCapabilities;
import supersymmetry.api.metatileentity.multiblock.SuSyMultiblockAbilities;

import java.util.List;

public class MetaTileEntityStrandBus extends MetaTileEntityMultiblockPart implements IStrandProvider, IMultiblockAbilityPart<IStrandProvider> {
    private Strand strand;
    private boolean isExport;

    public MetaTileEntityStrandBus(ResourceLocation metaTileEntityId, boolean isExport) {
        super(metaTileEntityId, 4);
        this.isExport = isExport;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityStrandBus(metaTileEntityId, isExport);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == SuSyCapabilities.STRAND_PROVIDER) {
            return SuSyCapabilities.STRAND_PROVIDER.cast(this);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public Strand getStrand() {
        return strand;
    }

    @Override
    public Strand take() {
        Strand transfer = this.strand; // Need to keep the reference
        this.strand = null;
        return transfer;
    }

    @Override
    public MultiblockAbility<IStrandProvider> getAbility() {
        return isExport ? SuSyMultiblockAbilities.STRAND_EXPORT : SuSyMultiblockAbilities.STRAND_IMPORT;
    }

    @Override
    public void registerAbilities(List<IStrandProvider> abilityList) {
        abilityList.add(this);
    }
}
