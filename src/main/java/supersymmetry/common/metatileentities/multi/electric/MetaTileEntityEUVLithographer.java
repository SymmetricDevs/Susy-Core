package supersymmetry.common.metatileentities.multi.electric;


import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.*;

import javax.annotation.Nonnull;


import static gregtech.api.util.RelativeDirection.*;

public class MetaTileEntityEUVLithographer extends RecipeMapMultiblockController {

    public MetaTileEntityEUVLithographer(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.EUV_LITHOGRAPHY);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityEUVLithographer(metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        TraceabilityPredicate casingPredicate = states(getCasingState()).setMinGlobalLimited(210);
        return FactoryBlockPattern.start(RIGHT, FRONT, DOWN)
                .aisle("CCCCCCCCCCXXXX", "CCCCCCCCCCXXXX", "CCCCCCCCCCXXXX", "CCCCCCCCCCXXXX")
                .aisle("CCCCCCCCCCXXXX", "C     MMRMXLLX", "C         XLLX", "CCCCCCCCCCXXXX")
                .aisle("CCSCCCCCCCXXXX", "C         XLLX", "CATTTA    XLLX", "CCCCCCCCCCXXXX")
                .aisle("CCCCCCCCCCXXXX", "CTATATMMMLLLLX", "CXXXXXXXXXXLLX", "CCCCCCCCCCXXXX")
                .aisle("CCCCCCCCCCXXXX", "CCCCCCCCCCXXXX", "CCCCCCCCCCXXXX", "CCCCCCCCCCXXXX")
                .where('S', selfPredicate())
                .where('C', casingPredicate.or(autoAbilities()))
                .where('X', casingPredicate)
                .where('G', states(getGearBoxState()))
                .where('L', states(getLaserEmitterState()))
                .where('T', states(getTrayState()))
                .where('A', states(getRobotArmState()))
                .where('M', states(getMirrorState()))
                .where('R', states(getReticleState()))
                .where(' ', air())
                .build();
    }

    protected IBlockState getCasingState() {
        return SuSyBlocks.MULTIBLOCK_CASING.getState(BlockSuSyMultiblockCasing.CasingType.EUV_SAFE_CASING);
    }

    protected IBlockState getGearBoxState() {
        return MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.TUNGSTENSTEEL_GEARBOX);
    }

    protected IBlockState getTrayState() {
        return SuSyBlocks.EUV_LITHOGRAPHER_COMPONENT.getState(BlockEUVLithographerComponent.EUVComponentType.TRAY);
    }

    protected IBlockState getRobotArmState() {
        return SuSyBlocks.EUV_LITHOGRAPHER_COMPONENT.getState(BlockEUVLithographerComponent.EUVComponentType.HANDLER);
    }

    protected IBlockState getLaserEmitterState() {
        return SuSyBlocks.EUV_LITHOGRAPHER_COMPONENT.getState(BlockEUVLithographerComponent.EUVComponentType.LASER);
    }

    protected IBlockState getMirrorState() {
        return SuSyBlocks.EUV_LITHOGRAPHER_COMPONENT.getState(BlockEUVLithographerComponent.EUVComponentType.MIRROR);
    }

    protected IBlockState getReticleState() {
        return SuSyBlocks.EUV_LITHOGRAPHER_COMPONENT.getState(BlockEUVLithographerComponent.EUVComponentType.RETICLE);
    }



    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return SusyTextures.EUV_SAFE_CASING;
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.BLAST_FURNACE_OVERLAY;
    }

    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }

    public boolean allowsFlip() {
        return true;
    }
}
