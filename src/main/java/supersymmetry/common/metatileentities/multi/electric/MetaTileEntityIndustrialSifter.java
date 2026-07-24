package supersymmetry.common.metatileentities.multi.electric;

import static supersymmetry.api.metatileentity.multiblock.SuSyPredicates.hiddenGearTooth;

import java.util.List;

import javax.annotation.Nullable;

import gregtech.api.recipes.RecipeMap;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.RelativeDirection;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.BlockGrinderCasing;
import supersymmetry.common.blocks.BlockSuSyMultiblockCasing;
import supersymmetry.common.blocks.SuSyBlocks;

public class MetaTileEntityIndustrialSifter extends RecipeMapMultiblockController {

    private static final int PARALLEL_LIMIT = 16;

    public MetaTileEntityIndustrialSifter(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMap.getByName("sifter"));
        this.recipeMapWorkable.setParallelLimit(PARALLEL_LIMIT);
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityIndustrialSifter(this.metaTileEntityId);
    }

    protected static IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STAINLESS_CLEAN);
    }

    private static IBlockState getGearboxState() {
        return MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STAINLESS_STEEL_GEARBOX);
    }

    protected static IBlockState getSieveState() {
        return SuSyBlocks.MULTIBLOCK_CASING.getState(BlockSuSyMultiblockCasing.CasingType.SIEVE_TRAY);
    }

    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("       ", "       ", "  CCC  ", "       ")
                .aisle("       ", "  CCC  ", " C$$$C ", "  CCC  ")
                .aisle("  F F  ", " CCCCC ", "X$$$$$X", " CCCCC ")
                .aisle("       ", " CCCCC ", "X$$G$$X", " CCCCC ")
                .aisle("  F F  ", " CCCCC ", "X$$$$$X", " CCCCC ")
                .aisle("       ", "  CCC  ", " C$$$C ", "  CCC  ")
                .aisle("       ", "       ", "  CSC  ", "       ")
                .where('C', states(getCasingState()).or(autoAbilities(
                        true, true, false,
                        false, false, false, false)))
                .where('X', states(getCasingState()).or(autoAbilities(
                        false, false, true,
                        true, true, true, false)))
                .where('G', states(getGearboxState()))
                .where('F', frames(Materials.StainlessSteel))
                .where('$', states(getSieveState()))
                .where('S', selfPredicate())
                .where(' ', any())
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart multiblockPart) {
        return Textures.CLEAN_STAINLESS_STEEL_CASING;
    }

    @SideOnly(Side.CLIENT)
    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.SIFTER_OVERLAY;
    }

    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("susy.machine.parallel_pure", 16));
    }
}
