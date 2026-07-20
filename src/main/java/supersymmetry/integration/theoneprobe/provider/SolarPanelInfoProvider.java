package supersymmetry.integration.theoneprobe.provider;

import gregtech.api.GTValues;
import gregtech.api.util.GTUtility;
import mcjty.theoneprobe.api.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import supersymmetry.Supersymmetry;
import supersymmetry.common.metatileentities.multi.electric.MetaTileEntityEvaporationPool;
import supersymmetry.common.metatileentities.single.electric.MetaTileEntitySolarPanel;

import javax.annotation.Nonnull;

public class SolarPanelInfoProvider implements IProbeInfoProvider {

    @Override
    public String getID() {
        return Supersymmetry.MODID + ":solar_panel_provider";
    }

    @Override
    public void addProbeInfo(@Nonnull ProbeMode mode, @Nonnull IProbeInfo probeInfo, @Nonnull EntityPlayer player,
                             @Nonnull World world, @Nonnull IBlockState blockState, @Nonnull IProbeHitData data) {
        if (GTUtility.getMetaTileEntity(world, data.getPos()) instanceof MetaTileEntitySolarPanel solarPanel &&
                solarPanel.getCurrentProduction(solarPanel.getTier()) > 0) {
            probeInfo.text(TextStyleClass.INFO + "{*susy.top.solar_panel.producing*}" + " " +
                    (TextFormatting.RED + String.valueOf(solarPanel.getCurrentProduction(solarPanel.getTier()))) + TextFormatting.WHITE
                    + " EU/t (" + (GTValues.VNF[solarPanel.getTier()]) + ")");
        }
    }
}
