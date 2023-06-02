package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.blocks.BlockBoilerCasing.BoilerCasingType;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.StoneVariantBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.common.blocks.BlockMultiblockTank;
import supersymmetry.common.blocks.SuSyBlocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityMultiStageFlashDistiller extends RecipeMapMultiblockController {
    public MetaTileEntityMultiStageFlashDistiller(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.MULTI_STAGE_FLASH_DISTILLATION);
        this.recipeMapWorkable = new MultiblockRecipeLogic(this, true);
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityMultiStageFlashDistiller(this.metaTileEntityId);
    }

    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle(" EEEB", " BEEB", " BEEB", " EEEB", "  BBB")
                .aisle(" AAA ", " B#B ", " B#BB", " AAA ", "  B  ")
                .aisle("CAAAB", "CAAAB", "CAAAB", "CAAAC", "CCBCC")
                .aisle(" DDD ", " DDD ", " DDD ", " DDD ", "  B  ")
                .aisle(" DDD ", " D#D ", " D#D ", " DDD ", "  B  ")
                .aisle("CDDDC", "CDDDC", "CDDDC", "CDDDC", "CCBCC")
                .aisle(" AAA ", " BAB ", " BAB ", " AAA ", "  B  ")
                .aisle(" AAA ", " B#B ", " B#B ", " AAA ", "  B  ")
                .aisle("CAAAC", "CAAAC", "CAAAC", "CAAAC", "CCBCC")
                .aisle(" DDD ", " DDD ", " DDD ", " DDD ", "  B  ")
                .aisle(" DDD ", " D#D ", " D#D ", " DDD ", "  B  ")
                .aisle("CDDDC", "CDDDC", "CDDDC", "CDDDC", "CCBCC")
                .aisle(" AAAB", " BABB", " BABB", " AAAB", "  BBB")
                .aisle(" AAAC", " B#BC", " B#BC", " AAAC", "  BCC")
                .aisle(" FFF ", " FSF ", " FFF ", " FFF ", "  B  ")
                .where('S', selfPredicate())
                .where('A', states(MetaBlocks.METAL_CASING.getState(MetalCasingType.STEEL_SOLID))
                        .or(autoAbilities(true, true, false, false, false, false, false)))
                .where('B', states(MetaBlocks.BOILER_CASING.getState((BoilerCasingType.STEEL_PIPE))))
                .where('C', states(MetaBlocks.FRAMES.get(Materials.Steel).getBlock(Materials.Steel)))
                .where('D', states(MetaBlocks.METAL_CASING.getState(MetalCasingType.STAINLESS_CLEAN))
                        .or(autoAbilities(true, true, false, false, false, false, false)))
                .where('E', states(MetaBlocks.METAL_CASING.getState(MetalCasingType.STEEL_SOLID))
                        .or(autoAbilities(true, true, false, false, false, true, false)))
                .where('F', states(MetaBlocks.METAL_CASING.getState(MetalCasingType.STEEL_SOLID))
                        .or(autoAbilities(true, true, false, false, true, false, false)))
                .where(' ', any())
                .where('#', air())
                .build();
    }
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.SOLID_STEEL_CASING;
    }

    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(TooltipHelper.RAINBOW_SLOW + I18n.format("gregtech.machine.perfect_oc", new Object[0]));
    }

    @Nonnull
    protected ICubeRenderer getFrontOverlay() {
        return Textures.BLAST_FURNACE_OVERLAY;
    }
}
