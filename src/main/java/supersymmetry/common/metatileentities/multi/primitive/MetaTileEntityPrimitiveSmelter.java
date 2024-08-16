package supersymmetry.common.metatileentities.multi.primitive;

import com.codetaylor.mc.pyrotech.modules.core.ModuleCore;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapPrimitiveMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.client.renderer.ICubeRenderer;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.metatileentity.multiblock.SuSyMultiblockAbilities;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.textures.SusyTextures;

public class MetaTileEntityPrimitiveSmelter extends RecipeMapPrimitiveMultiblockController {
    public MetaTileEntityPrimitiveSmelter(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.PRIMITIVE_SMELTER);
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


}
