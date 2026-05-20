package supersymmetry.common.metatileentities.single.electric;

import gregtech.api.GTValues;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetaTileEntityAtmosphericOxidizer extends TieredMetaTileEntity {

    private int currentRadius = 0;
    IBlockState RADICAL_AIR = IgnitableReplacements.stateOf("susy", "radical_air", 0);

    public MetaTileEntityAtmosphericOxidizer(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityAtmosphericOxidizer(this.metaTileEntityId, this.getTier());
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) { return null; }

    @Override
    protected boolean openGUIOnRightClick() { return false; }

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
        processFloodFill(getWorld(), getPos(), GTValues.VH[getTier()]);
    }

    private static final int MAX_RADIUS = 32;

    private void processFloodFill(World world, BlockPos origin, int budget) {
        Map<ResourceLocation, IBlockState> replacements = IgnitableReplacements.getReplacements();
        Map<String, IBlockState> metaReplacements = IgnitableReplacements.getMetaReplacements();

        Queue<BlockPos> queue = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();

        queue.add(origin);
        visited.add(origin);

        int filled = 0;

        while (!queue.isEmpty() && filled < budget) {
            BlockPos current = queue.poll();

            for (EnumFacing facing : EnumFacing.VALUES) {
                BlockPos neighbor = current.offset(facing);

                // Enforce max radius
                if (Math.abs(neighbor.getX() - origin.getX()) > MAX_RADIUS) continue;
                if (Math.abs(neighbor.getY() - origin.getY()) > MAX_RADIUS) continue;
                if (Math.abs(neighbor.getZ() - origin.getZ()) > MAX_RADIUS) continue;

                if (visited.contains(neighbor)) continue;
                visited.add(neighbor);

                if (!world.isBlockLoaded(neighbor)) continue;

                IBlockState state = world.getBlockState(neighbor);
                if (state == null) continue;

                Block block = state.getBlock();
                if (block == null) continue;

                // Radical air is already filled, keep propagating through it
                if (state == RADICAL_AIR) {
                    queue.add(neighbor);
                    continue;
                }

                ResourceLocation blockName = ForgeRegistries.BLOCKS.getKey(block);
                if (blockName == null) continue;

                String metaKey = blockName.toString() + ":" + block.getMetaFromState(state);

                IBlockState replacement = metaReplacements.get(metaKey);
                if (replacement == null) {
                    replacement = replacements.get(blockName);
                }

                if (replacement != null) {
                    if (replacement.getBlock() == Blocks.FIRE) {
                        BlockPos below = neighbor.down();
                        if (!world.isBlockLoaded(below)) continue;
                        IBlockState belowState = world.getBlockState(below);
                        if (!belowState.isSideSolid(world, below, EnumFacing.UP)) continue;
                    }

                    world.setBlockState(neighbor, replacement, 2);
                    filled++;

                    // If we turned this block into radical air, keep spreading through it
                    if (replacement == RADICAL_AIR) {
                        queue.add(neighbor);
                    }
                }
            }
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("susy.machine.atmospheric_oxidizer.tooltip.info"));
        tooltip.add(I18n.format("susy.machine.atmospheric_oxidizer.tooltip.description"));
        tooltip.add(I18n.format("susy.machine.atmospheric_oxidizer.tooltip.description1"));
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

        public static IBlockState stateOf(String domain, String path, int meta) {
            Block block = Block.REGISTRY.getObject(new ResourceLocation(domain, path));
            return block != null ? block.getStateFromMeta(meta) : Blocks.AIR.getDefaultState();
        }

        private static void buildReplacements() {

            replacements = new HashMap<>();
            metaReplacements = new HashMap<>();

            IBlockState FIRE = stateOf("minecraft", "fire", 0);
            ResourceLocation torch = new ResourceLocation("minecraft", "torch");
            IBlockState RADICAL_ODIXDE_AIR = stateOf("susy","radical_air",0);
            ResourceLocation air = new ResourceLocation("minecraft","air");

            replacements.put(torch, FIRE);
            replacements.put(air,RADICAL_ODIXDE_AIR);

            //metadata
            //metaReplacements.put("minecraft:torch:0", FIRE);
        }

        private IgnitableReplacements() {}
    }
}
