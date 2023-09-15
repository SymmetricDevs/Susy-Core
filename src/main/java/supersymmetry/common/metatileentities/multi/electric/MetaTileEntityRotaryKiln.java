package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.TraceabilityPredicate;
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
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.BlockMultiblockTank;
import supersymmetry.common.blocks.SuSyBlocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityRotaryKiln extends RecipeMapMultiblockController {
    public MetaTileEntityRotaryKiln(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.ROTARY_KILN);
        this.recipeMapWorkable = new MultiblockRecipeLogic(this);
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityRotaryKiln(this.metaTileEntityId);
    }

    protected BlockPattern createStructurePattern() {
        TraceabilityPredicate maintenance = autoAbilities(false, true, false, false, false, false, false).setMaxGlobalLimited(1);

        return FactoryBlockPattern.start()
                .aisle("A    A    A", "A    A    A", "L    A    R", "LCCCCMCCCCR", "L    A    R")
                .aisle("A    A    A", "A    A    A", "LCCCCMCCCCR", "L#########R", "LCCCCMCCCCR")
                .aisle("A    A    A", "A    A    A", "L    A    R", "LCCCCSCCCCR", "L    A    R")
                .where('S', selfPredicate())
                .where('A', states(MetaBlocks.FRAMES.get(Materials.Steel).getBlock(Materials.Steel)))
                .where('C', states(MetaBlocks.STONE_BLOCKS.get(StoneVariantBlock.StoneVariant.SMOOTH).getState(StoneVariantBlock.StoneType.CONCRETE_LIGHT)))
                .where('L', states(MetaBlocks.METAL_CASING.getState(MetalCasingType.STEEL_SOLID))
                        .or(autoAbilities(false, false, true, false, false, true, false))
                        .or(autoAbilities(true, false, false, false, false, false, false).setMinGlobalLimited(0))
                        .or(maintenance))
                .where('R', states(MetaBlocks.METAL_CASING.getState(MetalCasingType.STEEL_SOLID))
                        .or(autoAbilities(false, false, false, true, true, false, false))
                        .or(autoAbilities(true, false, false, false, false, false, false).setMinGlobalLimited(0))
                        .or(maintenance))
                .where('M', states(MetaBlocks.METAL_CASING.getState(MetalCasingType.STEEL_SOLID))
                        .or(maintenance))
                .where(' ', any())
                .where('#', air())
                .build();
    }

    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return SusyTextures.ROTARY_KILN_OVERLAY;
    }
}
