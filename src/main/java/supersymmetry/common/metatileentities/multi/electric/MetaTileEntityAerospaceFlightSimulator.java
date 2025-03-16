package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class MetaTileEntityAerospaceFlightSimulator extends MultiblockWithDisplayBase {
    public MetaTileEntityAerospaceFlightSimulator(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    protected void updateFormedValid() {

    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("CCCCC", "CCCCC", "AAAAA", "AAAAA", "AAAAA", "CCCCC")
                .aisle("CCCCC", "CCCCC", "PPPPP", "PPPPP", "PPPPP", "CCCCC")
                .aisle("CCCCC", "CCCCC", "PPPPP", "PPPPP", "PPPPP", "CCCCC")
                .aisle("CCCCC", "CCCCC", "PPPPP", "PPPPP", "PPPPP", "CCCCC")
                .aisle("CCSCC", "CCCCC", "AAAAA", "AAAAA", "AAAAA", "CCCCC")
                .where('S', selfPredicate())
                .where('A', air())
                .where('C', states(getCasingState()))
                .where('P', states(getComputerState()))
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    public IBlockState getCasingState() {
        return null;
    }

    public IBlockState getComputerState() {
        return null;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityAerospaceFlightSimulator(metaTileEntityId);
    }
}
