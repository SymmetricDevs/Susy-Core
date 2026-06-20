package supersymmetry.common.metatileentities.multi.electric;

import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
import supersymmetry.api.recipes.SuSyRecipeMaps;

public class MetaTileEntityLayupMachine extends RecipeMapMultiblockController {

    public MetaTileEntityLayupMachine(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.LAYUP);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("PPPPPPPPP", "   GGG   ", "   SSS   ", "   SSS   ", "   SSS   ", "   SSS   ", "   SSS   ",
                        "   SSS   ", "    G    ")
                .aisle("S   S   S", "         ", "         ", "         ", "         ", "         ", "         ",
                        "         ", "   SSS   ")
                .aisle("S   S   S", "         ", "         ", "         ", "         ", "         ", "         ",
                        "         ", "   SSS   ")
                .aisle("sssssssss", "s       s", "         ", "  sssss  ", "  sssss  ", "  sssss  ", "         ",
                        "    S    ", "    G    ")
                .aisle("sssssssss", "s       s", "  sssss  ", "  sssss  ", " S     S ", "  sssss  ", "  sssss  ",
                        "         ", "         ")
                .aisle("sssssssss", "s       s", "S sssss S", "SS     SS", "GS     SG", " S     S ", "  sssss  ",
                        "         ", "         ")
                .aisle("sssssssss", "s       s", "  sssss  ", "  sssss  ", " S     S ", "  sssss  ", "  sssss  ",
                        "         ", "         ")
                .aisle("sssssssss", "s       s", "         ", "  sssss  ", "  sssss  ", "  sssss  ", "         ",
                        "         ", "         ")
                .aisle("   HHH   ", "         ", "         ", "         ", "         ", "         ", "         ",
                        "         ", "         ")
                .aisle("   HHH   ", "   HCH   ", "         ", "         ", "         ", "         ", "         ",
                        "         ", "         ")
                .where('s', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STAINLESS_CLEAN)))
                .where('S', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID)))
                .where('H', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID))
                        .or(autoAbilities()))
                .where('G',
                        states(MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_GEARBOX)))
                .where('P', states(MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.STEEL_PIPE)))
                .where('C', selfPredicate())
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityLayupMachine(this.metaTileEntityId);
    }
}
