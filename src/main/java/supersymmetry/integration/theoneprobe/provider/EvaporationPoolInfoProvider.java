package supersymmetry.integration.theoneprobe.provider;

import gregtech.api.util.GTUtility;
import mcjty.theoneprobe.api.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
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
    public void addProbeInfo(@Nonnull ProbeMode mode, @Nonnull IProbeInfo probeInfo, @Nonnull EntityPlayer player,
                             @Nonnull World world, @Nonnull IBlockState blockState, @Nonnull IProbeHitData data) {
        if (GTUtility.getMetaTileEntity(world, data.getPos()) instanceof MetaTileEntityEvaporationPool evapPool) {
            probeInfo.text(TextStyleClass.INFO + "{*gregtech.top.evaporation_pool_heated_preface*}" + " " + (evapPool.isHeating() ?
                    (TextFormatting.GREEN + "{*gregtech.multiblock.evaporation_pool.is_heating*}") :
                    (TextFormatting.RED + "{*gregtech.multiblock.evaporation_pool.is_not_heating*}")));
            probeInfo.text(TextStyleClass.INFO + "{*gregtech.top.evaporation_pool.exposed_blocks*}" + " "
                    + (TextFormatting.YELLOW + String.valueOf(evapPool.getExposedBlocks())));
            probeInfo.text(TextStyleClass.INFO + "{*gregtech.top.evaporation_pool.average_speed*}" + " "
                    + (TextFormatting.AQUA + (String.format("%.2f", evapPool.getAverageSpeed()))) + (TextFormatting.WHITE + "x"));

        }
    }
}
