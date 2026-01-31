package supersymmetry.common.metatileentities.multi.primitive;

import net.minecraft.util.*;

import org.jetbrains.annotations.NotNull;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapPrimitiveMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import supersymmetry.api.recipes.SuSyRecipeMaps;

public class MetaTileEntityCupolaFurnace extends RecipeMapPrimitiveMultiblockController {

    public MetaTileEntityCupolaFurnace(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.PRIMITIVE_SMELTER);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityCupolaFurnace(this.metaTileEntityId);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("CCC", "CCC", "CCC", "CCC")
                .aisle("CCC", "CAC", "CAC", "CAC")
                .aisle("CCC", "CSC", "CCC", "CCC")
                .where('C',
                        states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.PRIMITIVE_BRICKS))
                                .setMinGlobalLimited(28)
                                .or(abilities(MultiblockAbility.EXPORT_ITEMS, MultiblockAbility.IMPORT_ITEMS)))
                .where('A', air())
                .where('S', selfPredicate())
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.PRIMITIVE_BRICKS;
    }
}
