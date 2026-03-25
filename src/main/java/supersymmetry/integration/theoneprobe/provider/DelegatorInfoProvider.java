package supersymmetry.integration.theoneprobe.provider;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import mcjty.theoneprobe.api.*;
import supersymmetry.Supersymmetry;
import supersymmetry.api.metatileentity.logistics.IDelegator;

public class DelegatorInfoProvider implements IProbeInfoProvider {

    @Override
    public String getID() {
        return Supersymmetry.MODID + ":delegator_info_provider";
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState state,
                             IProbeHitData data) {
        if (state.getBlock().hasTileEntity(state)) {
            TileEntity te = world.getTileEntity(data.getPos());
            if (te instanceof IGregTechTileEntity igtte) {
                MetaTileEntity mte = igtte.getMetaTileEntity();
                if (mte instanceof IDelegator delegator) {
                    probeInfo.text(TextStyleClass.INFO + "{*susy.top.delegator.delegating_face*}" +
                            delegator.getDelegatingFacing(data.getSideHit()));
                }
            }
        }
    }
}
