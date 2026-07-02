package supersymmetry.common.metatileentities.single.electric;

import gregtech.api.GTValues;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supersymmetry.api.SusyLog;
import supersymmetry.common.blocks.BlockSolarPanel;
import supersymmetry.common.blocks.SuSyBlocks;

import java.util.List;

import static gregtech.api.GTValues.LV;
import static net.minecraft.block.Block.getStateById;


public class MetaTileEntitySolarPanel extends TieredMetaTileEntity {

    public MetaTileEntitySolarPanel(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
    }
    IBlockState panelDisplayBlock = SuSyBlocks.SOLAR_PANEL.getState(BlockSolarPanel.SolarPanelType.DEFAULT);

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntitySolarPanel(this.metaTileEntityId, this.getTier());
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
    protected void reinitializeEnergyContainer() {
        super.reinitializeEnergyContainer();
    }

    public static boolean hasSkyAccess(@NotNull World world, @NotNull BlockPos blockPos) {
        BlockPos up = blockPos.up(3);
        return world.canSeeSky(up);
    }

    public boolean hasSolarPanelDisplay(@NotNull World world, @NotNull BlockPos blockPos) {
        BlockPos up = blockPos.up(2);
        return world.getBlockState(up) == panelDisplayBlock;
    }

    public boolean panelIsNotObstructed(@NotNull World world, @NotNull BlockPos blockPos) {
        BlockPos up = blockPos.up();
        return world.getBlockState(up) == getStateById(0);
    }

    public int getNumberOfNearbyPanels(@NotNull World world, @NotNull BlockPos blockPos) {
        int numPanels = 0;
        BlockPos current = blockPos.north().west().up(2);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (world.getBlockState(current) == panelDisplayBlock) {
                    numPanels++;
                }
                current = current.south();
            }
            current = current.north(3);
            current = current.east();
        }
        return numPanels - 1;
    }

    @Override
    public void update() {
        super.update();
        World world = this.getWorld();
        long time = world.getWorldTime() % 24000;
        double multiplier = 0;
        if (hasSkyAccess(world, this.getPos()) && hasSolarPanelDisplay(world, this.getPos()) &&
                panelIsNotObstructed(world, this.getPos())) {
            if (time >= 2000 && time < 10000) { //6000 is noon, 18000 is midnight
                multiplier = 1;
            } else if (time >= 14000 && time <= 22000) {
                multiplier = 0;
            } else if (time >= 10000 && time < 14000) {
                multiplier = (double) (14000 - time) / 4000.0;
            } else if (time < 2000) {
                multiplier = (double) time / 4000.0 + 0.5;
            } else {
                multiplier = (double) (time - 22000) / 4000.0;
            }

            Biome biome = world.getBiome(this.getPos());
            if (world.isRaining() && (biome.canRain() || biome.getEnableSnow())) {
                multiplier = multiplier * 0.5;
            }
            multiplier *= Math.pow(0.5, getNumberOfNearbyPanels(world, this.getPos()));
            multiplier = Math.clamp(multiplier, 0, 1);
        }
        this.energyContainer.changeEnergy(Math.round(GTValues.V[LV] * multiplier * ((double) getTier() / 2 + 0.5)));

    }

    @Override
    protected boolean isEnergyEmitter() {
        return true;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        tooltip.add(I18n.format("susy.machine.solar_panel.tooltip.info"));
        tooltip.add(I18n.format("susy.machine.solar_panel.tooltip.description1"));
        tooltip.add(I18n.format("susy.machine.solar_panel.tooltip.description2"));
        tooltip.add(I18n.format("susy.machine.solar_panel.tooltip.description3"));
        tooltip.add(I18n.format("susy.machine.solar_panel.voltage_produced",
                new Object[] { (Math.toIntExact(Math.round(GTValues.V[LV] * ((double) getTier() / 2 + 0.5)))), GTValues.VNF[this.getTier()] }));
        tooltip.add(I18n.format("gregtech.universal.tooltip.max_voltage_out",
                new Object[] { this.energyContainer.getOutputVoltage(), GTValues.VNF[this.getTier()] }));
        tooltip.add(I18n.format("gregtech.universal.tooltip.energy_storage_capacity",
                new Object[] { this.energyContainer.getEnergyCapacity() }));
    }

    @Override
    public boolean getIsWeatherOrTerrainResistant() {
        return true;
    }

    @Override
    public void onPlacement() {
        World world = this.getWorld();
        super.onPlacement();
        if (world.getBlockState(this.getPos().up(2)) == getStateById(0)) {
            world.setBlockState(this.getPos().up(2), panelDisplayBlock);
        }
    }

    @Override
    public void onRemoval() {
        World world = this.getWorld();
        super.onRemoval();
        if (world.getBlockState(this.getPos().up(2)) == panelDisplayBlock) {
            world.setBlockState(this.getPos().up(2), getStateById(0));
        }
    }
}
