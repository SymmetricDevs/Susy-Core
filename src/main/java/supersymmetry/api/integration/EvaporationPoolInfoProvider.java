package supersymmetry.api.integration;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import mcjty.theoneprobe.api.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import supersymmetry.Supersymmetry;
import supersymmetry.common.metatileentities.multi.electric.MetaTileEntityEvaporationPool;

import javax.annotation.Nonnull;

public class EvaporationPoolInfoProvider implements IProbeInfoProvider {

    @Override
    public String getID() {
        return Supersymmetry.MODID + ":evaporation_pool_provider";
    }

    @Override
    public void addProbeInfo(@Nonnull ProbeMode mode, @Nonnull IProbeInfo probeInfo, @Nonnull EntityPlayer player, @Nonnull World world, @Nonnull IBlockState blockState, @Nonnull IProbeHitData data) {
        if (blockState.getBlock().hasTileEntity(blockState)) {
            TileEntity tileEntity = world.getTileEntity(data.getPos());
            if (!(tileEntity instanceof IGregTechTileEntity)) return;

            MetaTileEntity metaTileEntity = ((IGregTechTileEntity) tileEntity).getMetaTileEntity();
            if (metaTileEntity instanceof MetaTileEntityEvaporationPool) {
                MetaTileEntityEvaporationPool evapPool = ((MetaTileEntityEvaporationPool) metaTileEntity);
                probeInfo.text(TextStyleClass.INFO + "{*gregtech.top.evaporation_pool_heated_preface*} " + (evapPool.isHeated ? ( TextFormatting.GREEN + "{*gregtech.top.evaporation_pool_is_heated*} ") : (TextFormatting.RED + "{*gregtech.top.evaporation_pool_not_heated*} ")));
                probeInfo.text(TextStyleClass.INFO + "{*gregtech.top.evaporation_pool.energy_transferred*} " + (TextFormatting.GREEN + (evapPool.getKiloJoules() + "."))
                        + (TextFormatting.GREEN + constLengthToString(evapPool.getJoulesBuffer())) + (TextFormatting.WHITE + ("{*gregtech.top.evaporation_pool.kilojoules*}")));
            }
        }
    }

    public String constLengthToString(int i) {
        String output = Integer.toString(i);
        int length = output.length();

        while (length < 3) {
            output = "0" + output;
            ++length;
        }

        if (length > 3) {
            output = output.substring(length -3);
        }

        return output;
    }
}
