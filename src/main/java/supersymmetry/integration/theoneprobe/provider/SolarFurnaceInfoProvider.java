package supersymmetry.integration.theoneprobe.provider;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import org.jspecify.annotations.NonNull;

import gregtech.api.util.GTUtility;
import mcjty.theoneprobe.api.*;
import supersymmetry.Supersymmetry;
import supersymmetry.common.metatileentities.multi.electric.MetaTileEntitySolarFurnace;

public class SolarFurnaceInfoProvider implements IProbeInfoProvider {

    @Override
    public String getID() {
        return Supersymmetry.MODID + ":solar_furnace_provider";
    }

    @Override
    public void addProbeInfo(@NonNull ProbeMode mode, @NonNull IProbeInfo probeInfo, @NonNull EntityPlayer player,
                             @NonNull World world, @NonNull IBlockState blockState, @NonNull IProbeHitData data) {
        if (GTUtility.getMetaTileEntity(world, data.getPos()) instanceof MetaTileEntitySolarFurnace solarFurnace) {
            probeInfo.text(TextStyleClass.INFO + "{*susy.top.solar_furnace.num_heliostats*}" + " " +
                    (TextFormatting.AQUA + String.valueOf(solarFurnace.getCurrentValidHeliostats())));
            probeInfo.text(TextStyleClass.INFO + "{*susy.top.solar_furnace.current_power*}" + " " +
                    (TextFormatting.RED + String.valueOf(solarFurnace.getCurrentPower() / 1000)) +
                    TextFormatting.WHITE + " kW");
            probeInfo.text(TextStyleClass.INFO + "{*susy.top.solar_furnace.average_speed*}" + " " +
                    (TextFormatting.AQUA + String.valueOf(solarFurnace.getAverageSpeed())) + "x");
        }
    }
}
