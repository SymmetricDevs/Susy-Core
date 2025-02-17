package supersymmetry.common.metatileentities.multi.electric.strand;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.unification.FluidUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.util.RelativeDirection;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.blocks.VariantAxialRotatableBlock;
import supersymmetry.api.capability.Strand;
import supersymmetry.api.metatileentity.multiblock.SuSyMultiblockAbilities;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.*;

public class MetaTileEntityTurningZone extends MetaTileEntityStrandShaper {
    public MetaTileEntityTurningZone(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    protected long getVoltage() {
        return energyContainer.getInputVoltage();
    }

    @Override
    protected void consumeInputsAndSetupRecipe() {
        FluidStack stack = getFirstMaterialFluid().copy();
        stack.amount = 2304;
        this.inputFluidInventory.drain(stack, true);
        this.maxProgress = 20;
    }

    @Override
    protected Strand resultingStrand() {
        FluidStack stack = getFirstMaterialFluid();
        if (stack == null || stack.amount < 2304) {
            return null;
        }
        Material mat = FluidUnifier.getMaterialFromFluid(stack.getFluid());
        if (mat == null || !mat.hasProperty(PropertyKey.INGOT)) {
            return null;
        }

        return new Strand(1, false, mat, stack.getFluid().getTemperature());
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(RelativeDirection.RIGHT, RelativeDirection.FRONT, RelativeDirection.UP)
                .aisle("ABBBA",
                        "ABBBA",
                        "ABBBA",
                        "ABBBA",
                        "     ",
                        "     ",
                        "     ",
                        "     ",
                        "F F F")
                .aisle("  O  ",
                        "     ",
                        "     ",
                        "     ",
                        "ABBBA",
                        "ABBBA",
                        "     ",
                        "     ",
                        "F S F")
                .aisle("ABBBA",
                        "ABBBA",
                        "ABBBA",
                        "     ",
                        "     ",
                        "     ",
                        "ABBBA",
                        "     ",
                        "F F F")
                .aisle("     ",
                        "     ",
                        "     ",
                        "ABBBA",
                        "ABBBA",
                        "     ",
                        "     ",
                        "ABBBA",
                        "F F F")
                .aisle("     ",
                        "     ",
                        "     ",
                        "     ",
                        "     ",
                        "ABBBA",
                        "     ",
                        "ABBBA",
                        "F F F")
                .aisle("     ",
                        "     ",
                        "     ",
                        "     ",
                        "     ",
                        "ABBBA",
                        "     ",
                        "     ",
                        "ABBBA")
                .aisle("     ",
                        "     ",
                        "     ",
                        "     ",
                        "     ",
                        "     ",
                        "ABBBA",
                        "     ",
                        "ABBBA")
                .aisle("     ",
                        "     ",
                        "     ",
                        "     ",
                        "     ",
                        "     ",
                        "ABBBA",
                        "  I  ",
                        "ABBBA")
                .where('B', states(SuSyBlocks.METALLURGY_ROLL.getState(BlockMetallurgyRoll.BlockMetallurgyRollType.ROLL)
                        .withProperty(VariantAxialRotatableBlock.AXIS,
                                getRelativeFacing(RelativeDirection.RIGHT).getAxis())))
                .where('A', states(MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_GEARBOX)))
                .where('I', abilities(MultiblockAbility.IMPORT_FLUIDS))
                .where('O', abilities(SuSyMultiblockAbilities.STRAND_EXPORT))
                .where('S', selfPredicate())
                .where('F', states(SuSyMetaBlocks.SHEETED_FRAMES.get(Materials.Steel).getBlock(Materials.Steel)
                        .withProperty(BlockSheetedFrame.SHEETED_FRAME_AXIS, BlockSheetedFrame.FrameEnumAxis
                                .fromFacingAxis(getRelativeFacing(RelativeDirection.UP).getAxis())))
                        .or(abilities(MultiblockAbility.MAINTENANCE_HATCH, MultiblockAbility.INPUT_ENERGY)))
                .where(' ', any())
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Override
    protected @NotNull ICubeRenderer getFrontOverlay() {
        return SusyTextures.TURNING_ZONE_OVERLAY;
    }

    protected EnumFacing getRelativeFacing(RelativeDirection dir) {
        return dir.getRelativeFacing(getFrontFacing(), getUpwardsFacing(), isFlipped());
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(),
                isActive, true);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityTurningZone(this.metaTileEntityId);
    }
}
