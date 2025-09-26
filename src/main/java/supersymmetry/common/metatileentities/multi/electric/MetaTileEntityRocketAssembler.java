package supersymmetry.common.metatileentities.multi.electric;

import javax.annotation.Nonnull;

import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import supersymmetry.api.metatileentity.multiblock.SuSyPredicates;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.common.blocks.BlockRocketAssemblerCasing;
import supersymmetry.common.blocks.SuSyBlocks;

public class MetaTileEntityRocketAssembler extends RecipeMapMultiblockController {

    public MetaTileEntityRocketAssembler(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.ROCKET_ASSEMBLER);
        this.recipeMapWorkable = new MultiblockRecipeLogic(this);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
                        "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
                        "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ")
                .aisle("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
                        "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
                        "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
                        " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
                        " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
                        " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
                        " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
                        " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
                        " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
                        " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
                        " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
                        " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
                        " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
                        " PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP ")
                .aisle("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        " P           P   P           P   P           P   P           P ")
                .aisle("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        " P           P   P           P   P           P   P           P ")
                .aisle("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        " P           P   P           P   P           P   P           P ")
                .aisle("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB",
                        " P           P   P           P   P           P   P           P ")
                .aisle("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
                        "RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        " P           P   P           P   P           P   P           P ")
                .aisle("                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        " P           P   P           P   P           P   P           P ")
                .aisle("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
                        "RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        " P           P   P           P   P           P   P           P ")
                .aisle("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB",
                        " P           P   P           P   P           P   P           P ")
                .aisle("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        " P           P   P           P   P           P   P           P ")
                .aisle("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        " P           P   P           P   P           P   P           P ")
                .aisle("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        " P           P   P           P   P           P   P           P ")
                .aisle("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
                        "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
                        "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
                        " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
                        " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
                        " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
                        " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
                        " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
                        " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
                        " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
                        " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
                        " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
                        " P   P   P   P   P   P   P   P   P   P   P   P   P   P   P   P ",
                        " PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP ")
                .aisle("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                        "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
                        "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
                        "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCSCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ",
                        "                                                               ")
                .where(' ', any())
                .where('S', selfPredicate())
                .where('F',
                        states(SuSyBlocks.ROCKET_ASSEMBLER_CASING
                                .getState(BlockRocketAssemblerCasing.RocketAssemblerCasingType.REINFORCED_FOUNDATION)))
                .where('C',
                        states(SuSyBlocks.ROCKET_ASSEMBLER_CASING
                                .getState(BlockRocketAssemblerCasing.RocketAssemblerCasingType.FOUNDATION)))
                .where('R', SuSyPredicates.rails())
                .where('P',
                        states(SuSyBlocks.ROCKET_ASSEMBLER_CASING
                                .getState(BlockRocketAssemblerCasing.RocketAssemblerCasingType.STRUCTURAL_FRAME)))
                .where('B',
                        states(SuSyBlocks.ROCKET_ASSEMBLER_CASING
                                .getState(BlockRocketAssemblerCasing.RocketAssemblerCasingType.RAILS)))
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.ASSEMBLER_OVERLAY;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityRocketAssembler(metaTileEntityId);
    }
}
