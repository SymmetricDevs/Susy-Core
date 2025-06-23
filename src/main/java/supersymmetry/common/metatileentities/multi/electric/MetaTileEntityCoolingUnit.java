package supersymmetry.common.metatileentities.multi.electric;

import gregtech.common.blocks.StoneVariantBlock;
import net.minecraft.init.Blocks;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockBoilerCasing.BoilerCasingType;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.metatileentity.multiblock.CachedPatternRecipeMapMultiblock;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.BlockSuSyMultiblockCasing;
import supersymmetry.common.blocks.SuSyBlocks;

public class MetaTileEntityCoolingUnit extends CachedPatternRecipeMapMultiblock {

    public MetaTileEntityCoolingUnit(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.COOLING_UNIT_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityCoolingUnit(metaTileEntityId);
    }

    protected static IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    @NotNull
    @Override
    protected BlockPattern createStructurePattern() {
        // Different characters use common constraints. Copied from GCyM

        return FactoryBlockPattern.start()
                .aisle("     CCCCC     ", "     CCCCC     ", "     CCCCC     ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                .aisle("   CCCCCCCCC   ", "   CCWWWWWCC   ", "   CCCCCCCCC   ", "     FFFFF     ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                .aisle("  CCCCCCCCCCC  ", "  CWWWWWWWWWC  ", "  CC#######CC  ", "   FF#####FF   ", "     CCCCC     ", "     CCICC     ", "      CCC      ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                .aisle(" CCCCCCCCCCCCC ", " CWWWWWWWWWWWC ", " CC#########CC ", "  F#########F  ", "   CCAAAAACC   ", "    C#P#P#C    ", "     C###C     ", "     CCCCC     ", "      CCC      ", "               ", "               ", "               ", "               ", "      CCC      ", "     CCCCC     ", "     CCCCC     ")
                .aisle(" CCCCCCCCCCCCC ", " CWWWWWWWWWWWC ", " CC#########CC ", "  F#########F  ", "   CAAAAAAAC   ", "   CPPPPPPPC   ", "    C#####C    ", "    CCCCCCC    ", "     CCCCC     ", "      CCC      ", "      CCC      ", "      CCC      ", "     CCCCC     ", "     CCCCC     ", "    CC###CC    ", "    CC###CC    ")
                .aisle("CCCCCCCCCCCCCCC", "CWWWWWWWWWWWWWC", "CC###########CC", " F###########F ", "  CAAAAAAAAAC  ", "  C#P#P#P#P#C  ", "   C#######C   ", "   CCFFFFFCC   ", "    CC###CC    ", "     C###C     ", "     C###C     ", "     C###C     ", "    CC###CC    ", "    CC###CC    ", "   CC#####CC   ", "   CC#####CC   ")
                .aisle("CCCCCCCCCCCCCCC", "CWWWWWWWWWWWWWC", "CC###########CC", " F###########F ", "  CAAAAAAAAAC  ", "  CPPPPPPPPPC  ", "  C#########C  ", "   CCFFFFFCC   ", "   CC#####CC   ", "    C#####C    ", "    C#####C    ", "    C#####C    ", "    C#####C    ", "   CC#####CC   ", "   C#######C   ", "   C#######C   ")
                .aisle("CCCCCCCCCCCCCCC", "CWWWWWWWWWWWWWC", "CC###########CC", " F###########F ", "  CAAAAAAAAAC  ", "  I#P#P#P#P#I  ", "  C#########C  ", "   CCFFFFFCC   ", "   CC#####CC   ", "    C#####C    ", "    C#####C    ", "    C#####C    ", "    C#####C    ", "    C#####C    ", "   C#######C   ", "   C#######C   ")
                .aisle("CCCCCCCCCCCCCCC", "CWWWWWWWWWWWWWC", "CC###########CC", " F###########F ", "  CAAAAAAAAAC  ", "  CPPPPPPPPPC  ", "  C#########C  ", "   CCFFFFFCC   ", "   CC#####CC   ", "    C#####C    ", "    C#####C    ", "    C#####C    ", "    C#####C    ", "   CC#####CC   ", "   C#######C   ", "   C#######C   ")
                .aisle("CCCCCCCCCCCCCCC", "CWWWWWWWWWWWWWC", "CC###########CC", " F###########F ", "  CAAAAAAAAAC  ", "  C#P#P#P#P#C  ", "   C#######C   ", "   CCFFFFFCC   ", "    CC###CC    ", "     C###C     ", "     C###C     ", "     C###C     ", "    CC###CC    ", "    CC###CC    ", "   CC#####CC   ", "   CC#####CC   ")
                .aisle(" CCCCCCCCCCCCC ", " CWWWWWWWWWWWC ", " CC#########CC ", "  F#########F  ", "   CAAAAAAAC   ", "   CPPPPPPPC   ", "    C#####C    ", "    CCCCCCC    ", "     CCCCC     ", "      CCC      ", "      CCC      ", "      CCC      ", "     CCCCC     ", "     CCCCC     ", "    CC###CC    ", "    CC###CC    ")
                .aisle(" CCCCCCCCCCCCC ", " CWWWWWWWWWWWC ", " CC#########CC ", "  F#########F  ", "   CCAAAAACC   ", "    C#P#P#C    ", "     C###C     ", "     CCCCC     ", "      CCC      ", "               ", "               ", "               ", "               ", "      CCC      ", "      CCC      ", "      CCC      ")
                .aisle("  CCCCCCCCCCP  ", "  CWWWWWWWWWC  ", "  CC#######CC  ", "   FF#####FF   ", "     CCCCC     ", "     CCICC     ", "      CCC      ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                .aisle("   CCCCCCCCCP  ", "   CCWWWWWCC   ", "   CCCCCCCCC   ", "     FFFFF     ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                .aisle("     CCCCC OOO ", "     CCCCC OSO ", "     CCCCC     ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                .where('S', selfPredicate())
                .where('O', states(getCasingState()).or(autoAbilities(true, true, false, false, false, true, false)))
                .where('I', states(getCasingState()).or(autoAbilities(false, false, false, false, true, false, false)))
                .where('C', states(MetaBlocks.STONE_BLOCKS.get(StoneVariantBlock.StoneVariant.SMOOTH).getState(StoneVariantBlock.StoneType.CONCRETE_LIGHT)))
                .where('W', blocks(Blocks.WATER))
                .where('F', frames(Materials.Steel))
                .where('A', states(SuSyBlocks.MULTIBLOCK_CASING.getState(BlockSuSyMultiblockCasing.CasingType.STRUCTURAL_PACKING)))
                .where('P', states(MetaBlocks.BOILER_CASING.getState(BoilerCasingType.STEEL_PIPE)))
                .where(' ', any())
                .where('#', air())
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return SusyTextures.COOLING_UNIT_OVERLAY;
    }


    @Override
    protected String[][] getPattern() {
        //TODO
        return new String[0][];
    }

    @Override
    protected Vec3i getPatternOffset() {
        //TODO
        return new Vec3i(0, 0, 0);
    }
}
