package supersymmetry.common.metatileentities.multi.electric.strand;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.metatileentity.multiblock.SuSyMultiblockAbilities;
import supersymmetry.common.blocks.BlockSuSyMultiblockCasing;
import supersymmetry.common.blocks.SuSyBlocks;

public class MetaTileEntityBilletMold extends MetaTileEntityStrandMold {
    public MetaTileEntityBilletMold(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    protected int getRequiredMetal() {
        return 2592;
    }

    @Override
    protected double getOutputThickness() {
        return 1 / 3.;
    }

    @Override
    protected double getOutputWidth() {
        return 1 / 3.;
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC", "CCCCCCC")
                .aisle("COOOOOC", "CPPPPPC", "CPPPPPC", "CPPPPPC", "CIIIIIC")
                .aisle("CO M OC", "CP   PC", "CP   PC", "CP   PC", "CIIIIIC")
                .aisle("COOOOOC", "CPPPPPC", "CPPPPPC", "CPPPPPC", "CIIIIIC")
                .aisle("CCCCCCC", "CCCCCCC", "CCCSCCC", "CCCCCCC", "CCCCCCC")
                .where('C', states(getCasingState()).or(autoAbilities()))
                .where('P', states(getPipeCasingState()))
                .where('M', abilities(SuSyMultiblockAbilities.STRAND_EXPORT))
                .where('I', abilities(MultiblockAbility.IMPORT_FLUIDS).or(states(getPipeCasingState())))
                .where('O', abilities(MultiblockAbility.EXPORT_FLUIDS).or(states(getPipeCasingState())))
                .where('S', selfPredicate())
                .where(' ', air())
                .build();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityBilletMold(metaTileEntityId);
    }
}
