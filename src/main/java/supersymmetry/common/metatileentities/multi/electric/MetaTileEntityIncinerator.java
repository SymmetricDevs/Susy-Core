package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.*;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import supersymmetry.common.metatileentities.multi.ItemVoidingMultiblockBase;

public class MetaTileEntityIncinerator extends ItemVoidingMultiblockBase {
    public MetaTileEntityIncinerator(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                //S: steel casing
                //G: grate casing
                //C: coil
                //B: alu frame box
                //F: steel firebox casing
                //P: steel pipe casing
                //M: muffler hatch
                //I: self
                .aisle("SSSSGS", "SSSCCC", " SSFFF", "  SCCC", "   BFB", "   BFB", "    B ", "    B ","    B ")
                .aisle("SSSSGG", "S##G#C", "S##G#F", " SSC#C", "   F#F", "   F#F", "   BPB", "   BPB", "   BMB")
                .aisle("SSSSGS", "SSICCC", " SSFFF", "  SCCC", "   BFB", "   BFB", "    B ", "    B ","    B ")
                .where('S', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID)).setMinGlobalLimited(28, 26).or(abilities(MultiblockAbility.IMPORT_ITEMS).setExactLimit(1)))
                .where('G', states(MetaBlocks.MULTIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.GRATE_CASING)))
                .where('C', heatingCoils())
                .where('B', states(MetaBlocks.FRAMES.get(Materials.Aluminium).getBlock(Materials.Aluminium)))
                .where('F', states(MetaBlocks.BOILER_FIREBOX_CASING.getState(BlockFireboxCasing.FireboxCasingType.STEEL_FIREBOX)))
                .where('P', states(MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.STEEL_PIPE)))
                .where('M', abilities(MultiblockAbility.MUFFLER_HATCH).setExactLimit(1))
                .where('I', selfPredicate())
                .where(' ', any())
                .where('#', air())
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityIncinerator(this.metaTileEntityId);
    }
}
