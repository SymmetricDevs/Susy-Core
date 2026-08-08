package supersymmetry.common.blocks;

import java.util.List;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.block.IHeatingCoilBlockStats;
import gregtech.api.block.VariantActiveBlock;
import gregtech.api.block.VariantItemBlock;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityMultiSmelter;
import supersymmetry.common.materials.SusyMaterials;

public class BlockHeatingCoil
                              extends VariantActiveBlock<BlockHeatingCoil.CoilType> {

    public BlockHeatingCoil() {
        super(net.minecraft.block.material.Material.IRON);

        setTranslationKey("heating_coil");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel(ToolClasses.WRENCH, 2);
        setDefaultState(getState(CoilType.MOLYBDENUM_DISILICIDE));
    }

    public enum CoilType implements IStringSerializable, IHeatingCoilBlockStats {

        MOLYBDENUM_DISILICIDE("molybdenum_disilicide", 2100, 4, 2, 3, SusyMaterials.MolybdenumDisilicide),
        TUNGSTEN("tungsten", 3000, 4, 4, 4, Materials.Tungsten);

        private final String name;
        private final int temperature;
        private final int level;
        private final int energyDiscount;
        private final int tier;
        private final Material material;

        CoilType(String name,
                 int temperature,
                 int level,
                 int energyDiscount,
                 int tier,
                 Material material) {
            this.name = name;
            this.temperature = temperature;
            this.level = level;
            this.energyDiscount = energyDiscount;
            this.tier = tier;
            this.material = material;
        }

        @NotNull @Override
        public String getName() {
            return name;
        }

        @Override
        public int getCoilTemperature() {
            return temperature;
        }

        @Override
        public int getLevel() {
            return level;
        }

        @Override
        public int getEnergyDiscount() {
            return energyDiscount;
        }

        @Override
        public int getTier() {
            return tier;
        }

        @Nullable @Override
        public Material getMaterial() {
            return material;
        }
    }

    @NotNull @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.SOLID;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@NotNull ItemStack itemStack,
                               @Nullable World worldIn,
                               @NotNull List<String> lines,
                               @NotNull ITooltipFlag tooltipFlag) {
        super.addInformation(itemStack, worldIn, lines, tooltipFlag);

        // noinspection rawtypes, unchecked
        VariantItemBlock itemBlock = (VariantItemBlock<CoilType, BlockHeatingCoil>) itemStack.getItem();

        IBlockState stackState = itemBlock.getBlockState(itemStack);
        CoilType coilType = getState(stackState);

        // Temperature
        lines.add(I18n.format(
                "tile.wire_coil.tooltip_heat",
                coilType.getCoilTemperature()));

        if (TooltipHelper.isShiftDown()) {
            int coilTier = coilType.getTier();

            // Multi Smelter
            lines.add(I18n.format("tile.wire_coil.tooltip_smelter"));

            lines.add(I18n.format(
                    "tile.wire_coil.tooltip_parallel_smelter",
                    coilType.getLevel() * 32));

            int EUt = MetaTileEntityMultiSmelter.getEUtForParallel(
                    MetaTileEntityMultiSmelter.getMaxParallel(coilType.getLevel()),
                    coilType.getEnergyDiscount());

            lines.add(I18n.format(
                    "tile.wire_coil.tooltip_energy_smelter",
                    EUt));

            // Pyrolyse Oven
            lines.add(I18n.format("tile.wire_coil.tooltip_pyro"));

            lines.add(I18n.format(
                    "tile.wire_coil.tooltip_speed_pyro",
                    coilTier == 0 ? 75 : 50 * (coilTier + 1)));

            // Cracking Unit
            lines.add(I18n.format("tile.wire_coil.tooltip_cracking"));

            lines.add(I18n.format(
                    "tile.wire_coil.tooltip_energy_cracking",
                    100 - 10 * coilTier));
        } else {
            lines.add(I18n.format(
                    "tile.wire_coil.tooltip_extended_info"));
        }
    }
}
