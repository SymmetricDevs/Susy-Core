package supersymmetry.integration.theoneprobe.provider;

import org.jspecify.annotations.NonNull;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import gregtech.api.util.GTUtility;
import mcjty.theoneprobe.api.*;
import supersymmetry.Supersymmetry;
import supersymmetry.common.metatileentities.multi.electric.MetaTileEntityEvaporationPool;

public class EvaporationPoolInfoProvider implements IProbeInfoProvider {

    @Override
    public String getID() {
        return Supersymmetry.MODID + ":evaporation_pool_provider";
    }

    @Override
    public void addProbeInfo(@NonNull ProbeMode mode, @NonNull IProbeInfo probeInfo, @NonNull EntityPlayer player,
                             @NonNull World world, @NonNull IBlockState blockState, @NonNull IProbeHitData data) {
        if (GTUtility.getMetaTileEntity(world, data.getPos()) instanceof MetaTileEntityEvaporationPool evapPool) {
            probeInfo.text(TextStyleClass.INFO + "{*susy.top.evaporation_pool_heated_preface*}" + " " +
                    (evapPool.isHeating() ?
                            (TextFormatting.GREEN + "{*susy.multiblock.evaporation_pool.is_heating*}") :
                            (TextFormatting.RED + "{*susy.multiblock.evaporation_pool.is_not_heating*}")));
            probeInfo.text(TextStyleClass.INFO + "{*susy.top.evaporation_pool.exposed_blocks*}" + " " +
                    (TextFormatting.YELLOW + String.valueOf(evapPool.getExposedBlocks())));
            probeInfo.text(TextStyleClass.INFO + "{*susy.top.evaporation_pool.average_speed*}" + " " +
                    (TextFormatting.AQUA + (String.format("%.2f", evapPool.getAverageSpeed()))) +
                    (TextFormatting.WHITE + "x"));

        }
    }
}
