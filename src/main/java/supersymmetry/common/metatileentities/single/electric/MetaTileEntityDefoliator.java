package supersymmetry.common.metatileentities.single.electric;

import com.google.common.collect.ImmutableMap;
import gregtech.api.GTValues;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class MetaTileEntityDefoliator extends TieredMetaTileEntity {

    private int currentRadius = 0;

    public MetaTileEntityDefoliator(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityDefoliator(this.metaTileEntityId, this.getTier());
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

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("defoliatorRadius", currentRadius);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        currentRadius = data.getInteger("defoliatorRadius");
    }

    @Override
    public void update() {
        super.update();

        this.energyContainer.changeEnergy(GTValues.VH[getTier() - 1]);

        if (this.energyContainer.getEnergyStored() < this.energyContainer.getEnergyCapacity()) return;
        if (getWorld().isRemote) return;

        this.energyContainer.removeEnergy(this.energyContainer.getEnergyCapacity());

        processShell(getWorld(), getPos(), currentRadius);

        if (currentRadius > 32) return;
        currentRadius++;
    }


    private void processShell(World world, BlockPos center, int radius) {
        if (radius == 0) {
            return;
        }

        Map<Block, IBlockState> replacements = DefoliatorReplacements.REPLACEMENTS;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos target = center.add(dx, dy, dz);

                    if (!world.isValid(target)) continue;

                    IBlockState state = world.getBlockState(target);
                    Block block = state.getBlock();

                    IBlockState replacement = replacements.get(block);
                    if (replacement != null) {
                        world.setBlockState(target, replacement, 2);
                    }
                }
            }
        }
    }


    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        tooltip.add(I18n.format("susy.machine.defoliator.tooltip.info"));
        tooltip.add(I18n.format("susy.machine.defoliator.tooltip.description"));
    }

    @Override
    public boolean getIsWeatherOrTerrainResistant() {
        return true;
    }

    public class DefoliatorReplacements {

        public static final Map<Block, IBlockState> REPLACEMENTS = ImmutableMap.<Block, IBlockState>builder()
                .put(Blocks.TALLGRASS,      Blocks.AIR.getDefaultState())
                .put(Blocks.YELLOW_FLOWER,  Blocks.AIR.getDefaultState())
                .put(Blocks.RED_FLOWER,     Blocks.AIR.getDefaultState())
                .put(Blocks.DOUBLE_PLANT,   Blocks.AIR.getDefaultState())
                .put(Blocks.DEADBUSH,       Blocks.AIR.getDefaultState())
                .put(Blocks.BROWN_MUSHROOM, Blocks.AIR.getDefaultState())
                .put(Blocks.RED_MUSHROOM,   Blocks.AIR.getDefaultState())
                .put(Blocks.VINE,           Blocks.AIR.getDefaultState())
                .put(Blocks.WATERLILY,      Blocks.AIR.getDefaultState())
                .put(Blocks.CACTUS,         Blocks.AIR.getDefaultState())
                .put(Blocks.REEDS,          Blocks.AIR.getDefaultState())
                .put(Blocks.LEAVES,         Blocks.AIR.getDefaultState())
                .put(Blocks.LEAVES2,        Blocks.AIR.getDefaultState())

                .put(Blocks.GRASS, Blocks.DIRT.getDefaultState()
                        .withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT))

                .build();

        private DefoliatorReplacements() {}
    }
}
