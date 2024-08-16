package supersymmetry.common.metatileentities.multi.primitive;

import com.codetaylor.mc.pyrotech.modules.core.ModuleCore;
import gregtech.api.capability.impl.PrimitiveRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapPrimitiveMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.MultiblockShapeInfo;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.RecipeMap;
import gregtech.client.renderer.ICubeRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.metatileentity.multiblock.SuSyMultiblockAbilities;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.metatileentities.SuSyMetaTileEntities;

import java.util.Collections;
import java.util.List;

public class MetaTileEntityPrimitiveSmelter extends RecipeMapPrimitiveMultiblockController {
    public MetaTileEntityPrimitiveSmelter(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.PRIMITIVE_SMELTER);
        this.recipeMapWorkable = new PrimitiveSmelterRecipeLogic(this);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("OOO", "III", "SIS")
                .aisle("OOO", "I I", "III")
                .aisle("OOO", "ICI", "SIS")
                .where('I', casingPredicate().or(abilities(SuSyMultiblockAbilities.PRIMITIVE_IMPORT_ITEMS).setMaxGlobalLimited(3)))
                .where('C', selfPredicate())
                .where('O', casingPredicate().or(abilities(SuSyMultiblockAbilities.PRIMITIVE_EXPORT_ITEMS).setMaxGlobalLimited(1)))
                .where('S', states(ModuleCore.Blocks.MASONRY_BRICK_SLAB.getDefaultState()))
                .build();
    }

    public TraceabilityPredicate casingPredicate() {
        return states(ModuleCore.Blocks.MASONRY_BRICK.getDefaultState());
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return SusyTextures.MASONRY_BRICK;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityPrimitiveSmelter(this.metaTileEntityId);
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        return Collections.singletonList(MultiblockShapeInfo.builder()
                .aisle("BBB", "BBB", "SBS")
                .aisle("BBB", "B B", "BBB")
                .aisle("BOB", "ICI", "SBS")
                .where('B', ModuleCore.Blocks.MASONRY_BRICK.getDefaultState())
                .where('C', SuSyMetaTileEntities.PRIMITIVE_SMELTER, EnumFacing.SOUTH)
                .where('I', SuSyMetaTileEntities.PRIMITIVE_ITEM_IMPORT, EnumFacing.SOUTH)
                .where('O', SuSyMetaTileEntities.PRIMITIVE_ITEM_EXPORT, EnumFacing.SOUTH)
                .build());
    }

    public static class PrimitiveSmelterRecipeLogic extends PrimitiveRecipeLogic {
        public PrimitiveSmelterRecipeLogic(RecipeMapPrimitiveMultiblockController tileEntity) {
            super(tileEntity, SuSyRecipeMaps.PRIMITIVE_SMELTER);
            setParallelLimit(8);
        }
    }
}
