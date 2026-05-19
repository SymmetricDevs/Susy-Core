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
        processShell(getWorld(), getPos(), currentRadius);

        if (currentRadius <= 32) currentRadius++;
    }

    private void processShell(World world, BlockPos center, int radius) {
        if (radius == 0) return;

        Map<ResourceLocation, IBlockState> replacements = IgnitableReplacements.getReplacements();
        Map<String, IBlockState> metaReplacements = IgnitableReplacements.getMetaReplacements();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {

                    BlockPos target = center.add(dx, dy, dz);

                    if (!world.isBlockLoaded(target)) continue;

                    IBlockState state = world.getBlockState(target);
                    if (state == null) continue;

                    Block block = state.getBlock();
                    if (block == null) continue;

                    ResourceLocation blockName = ForgeRegistries.BLOCKS.getKey(block);
                    if (blockName == null) continue;

                    String metaKey = blockName.toString() + ":" + block.getMetaFromState(state);

                    IBlockState replacement = metaReplacements.get(metaKey);
                    if (replacement == null) {
                        replacement = replacements.get(blockName);
                    }

                    if (replacement != null) {

                        // Fire is :dimbass:, requires another block under it to work
                        if (replacement.getBlock() == Blocks.FIRE) {
                            BlockPos below = target.down();
                            if (!world.isBlockLoaded(below)) continue;

                            IBlockState belowState = world.getBlockState(below);
                            if (!belowState.isSideSolid(world, below, net.minecraft.util.EnumFacing.UP)) {
                                continue;
                            }
                        }

                        world.setBlockState(target, replacement, 2);
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

            //metadata
            //metaReplacements.put("minecraft:torch:0", FIRE);
        }

        private IgnitableReplacements() {}
    }
}
