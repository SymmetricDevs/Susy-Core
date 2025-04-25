package supersymmetry.common.metatileentities.multi;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import supersymmetry.common.blocks.BlockSuSyMultiblockCasing;
import supersymmetry.common.blocks.SuSyBlocks;

import javax.annotation.Nonnull;

public class MetaTileEntityLandingPad extends MultiblockWithDisplayBase {

    public MetaTileEntityLandingPad(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityLandingPad(metaTileEntityId);
    }

    @Override
    protected void updateFormedValid() {
        if (this.getWorld().isRemote) return;
    }

    protected static IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    protected static IBlockState getPadState() {
        return SuSyBlocks.MULTIBLOCK_CASING.getState(BlockSuSyMultiblockCasing.CasingType.HEAVY_DUTY_PAD);
    }

    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    public TraceabilityPredicate getAbilityPredicate() {
        TraceabilityPredicate predicate = super.autoAbilities(true, false);
        predicate.or(abilities(new MultiblockAbility[]{MultiblockAbility.INPUT_ENERGY}).setMinGlobalLimited(1).setMaxGlobalLimited(2).setPreviewCount(1));
        predicate.or(abilities(new MultiblockAbility[]{MultiblockAbility.EXPORT_ITEMS}).setPreviewCount(1));
        predicate.or(abilities(new MultiblockAbility[]{MultiblockAbility.EXPORT_FLUIDS}).setPreviewCount(1));
        return predicate;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(),
                this.isActive(), true);
    }

    @NotNull
    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("     CCCCC     ", "      CCC      ")
                .aisle("   CCPPPPPCC   ", "     AAAAA     ")
                .aisle("  CPPPPPPPPPC  ", "   AAAAAAAAA   ")
                .aisle(" CPPPPPPPPPPPC ", "  AAAAAAAAAAA  ")
                .aisle(" CPPPPPPPPPPPC ", "  AAAAAAAAAAA  ")
                .aisle("CPPPPPPPPPPPPPC", " AAAAAAAAAAAAA ")
                .aisle("CPPPPPPPPPPPPPC", "CAAAAAAAAAAAAAC")
                .aisle("CPPPPPPPPPPPPPC", "CAAAAAAAAAAAAAC")
                .aisle("CPPPPPPPPPPPPPC", "CAAAAAAAAAAAAAC")
                .aisle("CPPPPPPPPPPPPPC", " AAAAAAAAAAAAA ")
                .aisle(" CPPPPPPPPPPPC ", "  AAAAAAAAAAA  ")
                .aisle(" CPPPPPPPPPPPC ", "  AAAAAAAAAAA  ")
                .aisle("  CPPPPPPPPPC  ", "   AAAAAAAAA   ")
                .aisle("   CCPPPPPCC   ", "     AAAAA     ")
                .aisle("     CCSCC     ", "      CCC      ")
                .where(' ', any())
                .where('A', air())
                .where('S', selfPredicate())
                .where('C', states(getCasingState()).setMinGlobalLimited(6).or(getAbilityPredicate()))
                .where('P', states(getPadState()))
                .build();
    }

    @Override
    public boolean isMultiblockPartWeatherResistant(@Nonnull IMultiblockPart part) {
        return true;
    }

    @Override
    public boolean getIsWeatherOrTerrainResistant() {
        return true;
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return true;
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.ASSEMBLER_OVERLAY;
    }

}
