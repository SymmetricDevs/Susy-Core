package susycore.common.blocks;

import gregtech.api.block.IHeatingCoilBlockStats;
import gregtech.api.block.VariantActiveBlock;
import gregtech.api.block.VariantItemBlock;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import susycore.SusyMaterials;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class BlockCoolingCoil extends VariantActiveBlock<BlockCoolingCoil.CoolingCoilType> {

    public Material ManganeseIronArsenicPhosphide;
    public Material PraseodymiumNickel;

    public BlockCoolingCoil() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("cooling_coil");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(CoolingCoilType.MANGANESEIRONARSENICPHOSPHIDE));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@Nonnull ItemStack itemStack, @Nullable World worldIn, List<String> lines, @Nonnull ITooltipFlag tooltipFlag) {
        super.addInformation(itemStack, worldIn, lines, tooltipFlag);

        // noinspection rawtypes, unchecked
        VariantItemBlock itemBlock = (VariantItemBlock<CoolingCoilType, BlockCoolingCoil>) itemStack.getItem();
        IBlockState stackState = itemBlock.getBlockState(itemStack);
        CoolingCoilType coolingCoilType = getState(stackState);

        lines.add(I18n.format("tile.cooling_coil.tooltip_heat", coolingCoilType.coilTemperature));
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    public enum CoolingCoilType implements IStringSerializable, IHeatingCoilBlockStats {

        MANGANESEIRONARSENICPHOSPHIDE("manganese_iron_arsenic_phosphide", 160, 1, 1, SusyMaterials.ManganeseIronArsenicPhosphide),
        PRASEODYMIUMNICKEL("praseodymium_nickel", 50, 2, 1, SusyMaterials.PraseodymiumNickel),
        GADOLINIUMSILICONGERMANIUM("gadolinium_silicon_germanium", 3, 3, 1, SusyMaterials.GadoliniumSiliconGermanium);

        private final String name;
        //electric blast furnace properties
        private final int coilTemperature;
        //multi smelter properties
        private final int level;
        private final int energyDiscount;
        private final Material material;

        CoolingCoilType(String name, int coilTemperature, int level, int energyDiscount, Material material) {
            this.name = name;
            this.coilTemperature = coilTemperature;
            this.level = level;
            this.energyDiscount = energyDiscount;
            this.material = material;
        }

        @Nonnull
        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public int getCoilTemperature() {
            return coilTemperature;
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
            return this.ordinal();
        }

        @Nullable
        @Override
        public Material getMaterial() {
            return material;
        }

        @Nonnull
        @Override
        public String toString() {
            return getName();
        }
    }
}
