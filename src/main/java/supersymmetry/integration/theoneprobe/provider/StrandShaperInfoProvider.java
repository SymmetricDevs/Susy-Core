package supersymmetry.integration.theoneprobe.provider;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.api.util.TextFormattingUtil;
import mcjty.theoneprobe.api.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import supersymmetry.Supersymmetry;
import supersymmetry.common.metatileentities.multi.electric.strand.MetaTileEntityStrandShaper;

public class StrandShaperInfoProvider implements IProbeInfoProvider {
    @Override
    public String getID() {
        return Supersymmetry.MODID + ":strand_shaper_info_provider";
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo probeInfo, EntityPlayer entityPlayer, World world, IBlockState blockState, IProbeHitData data) {
        if (blockState.getBlock().hasTileEntity(blockState)) {
            TileEntity tileEntity = world.getTileEntity(data.getPos());
            if (!(tileEntity instanceof IGregTechTileEntity)) return;

            MetaTileEntity metaTileEntity = ((IGregTechTileEntity) tileEntity).getMetaTileEntity();
            if (metaTileEntity instanceof MetaTileEntityStrandShaper shaper) {
                long EUt = shaper.getVoltage();
                String text = TextFormatting.RED + TextFormattingUtil.formatNumbers(EUt) + TextStyleClass.INFO +
                        " EU/t" + TextFormatting.GREEN +
                        " (" + GTValues.VN[GTUtility.getTierByVoltage(EUt)] + TextFormatting.GREEN + ")";
                probeInfo.text(TextStyleClass.INFO + "{*gregtech.top.energy_consumption*} " + text);
            }
        }
    }
}
