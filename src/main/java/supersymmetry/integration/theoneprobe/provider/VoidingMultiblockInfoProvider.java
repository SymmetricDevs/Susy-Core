package supersymmetry.integration.theoneprobe.provider;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.TextComponentUtil;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import mcjty.theoneprobe.api.*;
import org.jetbrains.annotations.NotNull;
import supersymmetry.Supersymmetry;
import supersymmetry.common.metatileentities.multi.VoidingMultiblockBase;

public class VoidingMultiblockInfoProvider implements IProbeInfoProvider {

    @Override
    public String getID() {
        return Supersymmetry.MODID + ":voiding_multiblock_provider";
    }

    @Override
    public void addProbeInfo(@NotNull ProbeMode mode, @NotNull IProbeInfo probeInfo, @NotNull EntityPlayer player,
                             @NotNull World world, @NotNull IBlockState blockState, @NotNull IProbeHitData data) {
        if (blockState.getBlock().hasTileEntity(blockState)) {
            TileEntity tileEntity = world.getTileEntity(data.getPos());
            if (!(tileEntity instanceof IGregTechTileEntity)) return;

            MetaTileEntity metaTileEntity = ((IGregTechTileEntity) tileEntity).getMetaTileEntity();
            if (metaTileEntity instanceof VoidingMultiblockBase && metaTileEntity.isActive()) {
                ITextComponent componentVoidingRate = TextComponentUtil.translationWithColor(TextFormatting.RED,
                        String.valueOf(((VoidingMultiblockBase) metaTileEntity).getActualVoidingRate()));
                probeInfo.text(TextComponentUtil.translationWithColor(TextFormatting.GRAY,
                        "susy.top.voiding_multiblock",
                        componentVoidingRate).getFormattedText());
            }
        }
    }
}
