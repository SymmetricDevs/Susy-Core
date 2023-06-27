package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.blocks.BlockBoilerCasing.BoilerCasingType;
import gregtech.common.blocks.BlockGlassCasing;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiFluidHatch;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import supersymmetry.api.recipes.SuSyRecipeMaps;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static gregtech.api.util.RelativeDirection.*;

public class MetaTileEntityCryogenicDistillationPlant extends RecipeMapMultiblockController {
    public MetaTileEntityCryogenicDistillationPlant(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.CRYOGENIC_DISTILLATION);
        this.recipeMapWorkable = new MultiblockRecipeLogic(this, false);
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityCryogenicDistillationPlant(this.metaTileEntityId);
    }

    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(RIGHT, FRONT, UP)
                .aisle(new String[]{"CCC", "CCC", "CCC"})
                .aisle(new String[]{"CSC", "CFC", "CCC"})
                .aisle(new String[]{"XXX", "XFX", "XXX"}).setRepeatable(1,16)
                .aisle(new String[]{"DDD", "DDD", "DDD"})
                .where('S', this.selfPredicate())
                .where('C', states(new IBlockState[]{this.getCasingState()})
                        .or(abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(3))
                        .or(abilities(MultiblockAbility.IMPORT_ITEMS).setMaxGlobalLimited(1))
                        .or(abilities(MultiblockAbility.MAINTENANCE_HATCH).setMaxGlobalLimited(1)))
                .where('F', states(getFrameState()))
                .where('X', states(getCasingState())
                        .or(metaTileEntities(MultiblockAbility.REGISTRY.get(MultiblockAbility.EXPORT_FLUIDS).stream()
                                .filter(mte->!(mte instanceof MetaTileEntityMultiFluidHatch))
                                .toArray(MetaTileEntity[]::new))
                                .setMaxLayerLimited(1))
                        .or(metaTileEntities(MultiblockAbility.REGISTRY.get(MultiblockAbility.IMPORT_FLUIDS).stream()
                                .filter(mte->!(mte instanceof MetaTileEntityMultiFluidHatch))
                                .toArray(MetaTileEntity[]::new))
                                .setMaxLayerLimited(1)))
                .where('D', states(new IBlockState[]{this.getCasingState()}))
                .where('#', air())
                .build();
    }

    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.FROST_PROOF_CASING;
    }

    protected IBlockState getFrameState() {
        return MetaBlocks.FRAMES.get(Materials.StainlessSteel).getBlock(Materials.StainlessSteel);
    }

    protected IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(MetalCasingType.ALUMINIUM_FROSTPROOF);
    }

    @Nonnull
    protected ICubeRenderer getFrontOverlay() {
        return Textures.BLAST_FURNACE_OVERLAY;
    }
}
