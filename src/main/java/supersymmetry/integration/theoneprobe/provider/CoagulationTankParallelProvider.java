package supersymmetry.integration.theoneprobe.provider;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.TextComponentUtil;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import supersymmetry.Supersymmetry;
import supersymmetry.common.metatileentities.multi.primitive.MetaTileEntityCoagulationTank;

public class CoagulationTankParallelProvider implements IProbeInfoProvider {
    @Override
    public String getID() {
        return Supersymmetry.MODID + ":coagulation_tank_parallel_provider";
    }

    public void addProbeInfo(@NotNull ProbeMode mode, @NotNull IProbeInfo probeInfo, @NotNull EntityPlayer player,
                             @NotNull World world, @NotNull IBlockState blockState, @NotNull IProbeHitData data) {
        if (blockState.getBlock().hasTileEntity(blockState)) {
            TileEntity tileEntity = world.getTileEntity(data.getPos());
            if (!(tileEntity instanceof IGregTechTileEntity)) return;

            MetaTileEntity metaTileEntity = ((IGregTechTileEntity) tileEntity).getMetaTileEntity();
            if (metaTileEntity instanceof MetaTileEntityCoagulationTank && metaTileEntity.isActive()) {
                ITextComponent componentParallelAmount = TextComponentUtil.translationWithColor(TextFormatting.AQUA,
                        String.valueOf(((MetaTileEntityCoagulationTank) metaTileEntity).getSize()));
                probeInfo.text(TextComponentUtil.translationWithColor(TextFormatting.WHITE,
                        "susy.top.coagulation_tank_parallel",
                        componentParallelAmount).getFormattedText());
            }
        }
    }
}
