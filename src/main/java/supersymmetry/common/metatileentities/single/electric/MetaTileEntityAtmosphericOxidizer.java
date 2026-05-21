package supersymmetry.common.metatileentities.single.electric;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;

import gregtech.api.GTValues;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;

public class MetaTileEntityAtmosphericOxidizer extends TieredMetaTileEntity {

    private int currentRadius = 0;
    private final IBlockState RADICAL_AIR = stateOf("susy", "radical_air", 0);
    private static final int MAX_RADIUS = 32;

    public MetaTileEntityAtmosphericOxidizer(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tile) {
        return new MetaTileEntityAtmosphericOxidizer(this.metaTileEntityId, this.getTier());
    }

    @Override
    protected ModularUI createUI(EntityPlayer player) { return null; }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("harmRadius", currentRadius);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        currentRadius = data.getInteger("harmRadius");
    }

    @Override
    public void update() {
        super.update();

        this.energyContainer.changeEnergy(GTValues.VH[getTier() - 1]);

        if (this.energyContainer.getEnergyStored() < this.energyContainer.getEnergyCapacity()) return;
        if (getWorld().isRemote) return;

        this.energyContainer.removeEnergy(this.energyContainer.getEnergyCapacity());
        processFloodFill(getWorld(), getPos(), (int) GTValues.V[getTier()]);
    }

    private void processFloodFill(World world, BlockPos origin, int budget) {
        Queue<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();

        queue.add(origin);
        visited.add(origin);

        int filled = 0;

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();

            for (EnumFacing facing : EnumFacing.VALUES) {
                BlockPos neighbor = current.offset(facing);

                if (Math.abs(neighbor.getX() - origin.getX()) > MAX_RADIUS ||
                        Math.abs(neighbor.getY() - origin.getY()) > MAX_RADIUS ||
                        Math.abs(neighbor.getZ() - origin.getZ()) > MAX_RADIUS) {
                    continue;
                }

                if (visited.contains(neighbor)) continue;
                if (!world.isBlockLoaded(neighbor)) continue;

                IBlockState state = world.getBlockState(neighbor);
                if (state == null) continue;


                Block block = state.getBlock();


                if (block == RADICAL_AIR.getBlock()) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                    continue;
                }

                if (world.isAirBlock(neighbor)) {
                    if (filled >= budget) {
                        return;
                    }

                    world.setBlockState(neighbor, RADICAL_AIR, 3);

                    visited.add(neighbor);
                    queue.add(neighbor);
                    filled++;

                    if (filled >= budget) {
                        return;
                    }
                }
            }
        }
    }

    private static IBlockState stateOf(String domain, String path, int meta) {
        Block block = Block.REGISTRY.getObject(new ResourceLocation(domain, path));
        return block != null ? block.getStateFromMeta(meta) : net.minecraft.init.Blocks.AIR.getDefaultState();
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("susy.machine.atmospheric_oxidizer.tooltip.info"));
        tooltip.add(I18n.format("susy.machine.atmospheric_oxidizer.tooltip.description"));
        tooltip.add(I18n.format("susy.machine.atmospheric_oxidizer.tooltip.description1"));
        tooltip.add(I18n.format("susy.machine.atmospheric_oxidizer.tooltip.description2"));
        tooltip.add(I18n.format("susy.machine.generic.tooltip.radius_warning"));
    }

    @Override
    public boolean getIsWeatherOrTerrainResistant() { return true; }
    // -------------------------------------------------------------------------

    public static class IgnitableReplacements {

        private static Map<ResourceLocation, IBlockState> replacements;
        private static Map<String, IBlockState> metaReplacements;

        public static Map<ResourceLocation, IBlockState> getReplacements() {
            if (replacements == null || metaReplacements == null) {
                buildReplacements();
            }
            return replacements;
        }

        public static Map<String, IBlockState> getMetaReplacements() {
            if (replacements == null || metaReplacements == null) {
                buildReplacements();
            }
            return metaReplacements;
        }

        private static IBlockState stateOf(String domain, String path, int meta) {
            Block block = Block.REGISTRY.getObject(new ResourceLocation(domain, path));
            return block != null ? block.getStateFromMeta(meta) : Blocks.AIR.getDefaultState();
        }

        private static void buildReplacements() {
            replacements = new HashMap<>();
            metaReplacements = new HashMap<>();

            IBlockState FIRE = stateOf("minecraft", "fire", 0);
            ResourceLocation torch = new ResourceLocation("minecraft", "torch");
            replacements.put(torch, FIRE);

            // metadata
            // metaReplacements.put("minecraft:torch:0", FIRE);
        }

        private IgnitableReplacements() {}
    }
}
