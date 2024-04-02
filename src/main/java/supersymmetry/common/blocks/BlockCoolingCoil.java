package supersymmetry.common.blocks;

import gregtech.api.block.VariantActiveBlock;
import gregtech.api.block.VariantItemBlock;
import gregtech.api.unification.material.Material;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import static net.minecraft.block.material.Material.IRON;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supersymmetry.common.materials.SusyMaterials;

import java.util.List;

public class BlockCoolingCoil extends VariantActiveBlock<BlockCoolingCoil.CoolingCoilType> {

    public BlockCoolingCoil() {
        super(IRON);
        setTranslationKey("cooling_coil");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(CoolingCoilType.MANGANESE_IRON_ARSENIC_PHOSPHIDE));
    }

    @NotNull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.SOLID;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@NotNull ItemStack itemStack, @Nullable World worldIn, List<String> lines, @NotNull ITooltipFlag tooltipFlag) {
        super.addInformation(itemStack, worldIn, lines, tooltipFlag);

        // noinspection rawtypes, unchecked
        VariantItemBlock itemBlock = (VariantItemBlock<CoolingCoilType, BlockCoolingCoil>) itemStack.getItem();
        IBlockState stackState = itemBlock.getBlockState(itemStack);
        CoolingCoilType coolingCoilType = getState(stackState);

        lines.add(I18n.format("tile.cooling_coil.tooltip_temperature", coolingCoilType.coilTemperature));
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos, @NotNull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    public enum CoolingCoilType implements IStringSerializable {

        MANGANESE_IRON_ARSENIC_PHOSPHIDE("manganese_iron_arsenic_phosphide", 160, SusyMaterials.ManganeseIronArsenicPhosphide),
        PRASEODYMIUM_NICKEL("praseodymium_nickel", 50, SusyMaterials.PraseodymiumNickel),
        GADOLINIUM_SILICON_GERMANIUM("gadolinium_silicon_germanium", 1, SusyMaterials.GadoliniumSiliconGermanium);

        public final String name;
        public final int coilTemperature;
        public final Material material;

        CoolingCoilType(String name, int coilTemperature, Material material) {
            this.name = name;
            this.coilTemperature = coilTemperature;
            this.material = material;
        }

        @NotNull
        @Override
        public String getName() {
            return this.name;
        }

        @NotNull
        @Override
        public String toString() {
            return getName();
        }
    }
}
