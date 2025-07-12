package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.RelativeDirection;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import supersymmetry.api.metatileentity.IAnimatableMTE;
import supersymmetry.client.renderer.textures.SusyTextures;

public class MetaTileEntityBallMill extends RecipeMapMultiblockController implements IAnimatableMTE {

    @SideOnly(Side.CLIENT)
    private AnimationFactory factory;

    public MetaTileEntityBallMill(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap) {
        super(metaTileEntityId, recipeMap);
    }

    private static IBlockState getGearBoxState() {
        return MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_GEARBOX);
    }

    private static IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    // Placeholder
    private static IBlockState getGearState() {
        return MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.POLYTETRAFLUOROETHYLENE_PIPE);
    }

    // Placeholder
    private static IBlockState getShellCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    // Placeholder
    // How should I call this???
    private static IBlockState getShellLineState() {
        return MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.STEEL_PIPE);
    }

    // Placeholder
    // How should I call this???
    private static IBlockState getShellEndState() {
        return MetaBlocks.BOILER_FIREBOX_CASING.getState(BlockFireboxCasing.FireboxCasingType.STEEL_FIREBOX);
    }

    // Placeholder
    private static IBlockState getEngineCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.PTFE_INERT_CASING);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityBallMill(metaTileEntityId, recipeMap);
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected @NotNull ICubeRenderer getFrontOverlay() {
        return SusyTextures.BALL_MILL_OVERLAY;
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXXXXXXXXXXX", "            ", "            ", " G          ", " G          ", " G          ", "            ", "            ")
                .aisle("X          X", "            ", " G          ", " LHHLHHLHHL ", " LHHLHHLHHL ", " LHHLHHLHHL ", " G          ", "            ")
                .aisle("X          X", "PG         X", "PLHHLHHLHHLX", "PH########HX", "PH########HX", "PH########H ", " LHHLHHLHHL ", " G          ")
                .aisle("X          X", " G          ", " LHHLHHLHHL ", "PH########H ", "I##########I", "PH########H ", " LHHLHHLHHL ", " G          ")
                .aisle("X          X", "PG         X", "PLHHLHHLHHLX", "PH########HX", "PH########HX", "PH########H ", " LHHLHHLHHL ", " G          ")
                .aisle("X          X", "            ", " G          ", " LHHLHHLHHL ", " LHHLHHLHHL ", " LHHLHHLHHL ", " G          ", "            ")
                .aisle("XMMMXXXXXXXX", " NSM        ", "            ", " G          ", " G          ", " G          ", "            ", "            ")
                .where('M', states(getCasingState()).or(autoAbilities(
                        true, true, false,
                        false, false, false, false
                )))
                .where('I', states(getCasingState()).or(autoAbilities(
                        false, false, true,
                        true, false, false, false
                )))
                .where('H', states(getShellCasingState()))
                .where('L', states(getShellLineState()))
                .where('P', states(getEngineCasingState()))
                .where('N', states(getGearBoxState()))
                .where('G', states(getGearState()))
                .where('X', frames(Materials.Steel))
                .where('S', selfPredicate())
                .where('#', air())
                .where(' ', any())
                .build();
    }

    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        EnumFacing front = getFrontFacing();
        EnumFacing up = getUpwardsFacing();
        EnumFacing left = RelativeDirection.LEFT.getRelativeFacing(front, up, isFlipped());

        BlockPos pos = getPos();

        var v1 = pos.offset(left.getOpposite(), 4).offset(EnumFacing.DOWN, 4).offset(front, 5);
        var v2 = pos.offset(left, 4).offset(EnumFacing.UP, 4).offset(front.getOpposite(), 6);

        return new AxisAlignedBB(v1, v2);
    }

    @Override
    public Vec3i getTransformation() {
        EnumFacing front = getFrontFacing();
        EnumFacing back = front.getOpposite();
        EnumFacing up = getUpwardsFacing();
        EnumFacing left = RelativeDirection.LEFT.getRelativeFacing(front, up, isFlipped());

        int xOff = back.getXOffset() * 3 + left.getXOffset() * 4;
        int zOff = back.getZOffset() * 3 + left.getZOffset() * 4;

        return new Vec3i(xOff, 3, zOff);
    }

    @SideOnly(Side.CLIENT)
    private <T extends MetaTileEntity & IAnimatableMTE> PlayState predicate(AnimationEvent<T> event) {
        //        event.getController().transitionLengthTicks = 0.0;
        event.getController()
                .setAnimation((new AnimationBuilder()).addAnimation("default_loop", ILoopType.EDefaultLoopTypes.LOOP));
        return isActive() ? PlayState.CONTINUE : PlayState.STOP;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "controller", 0.0F, this::predicate));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public AnimationFactory getFactory() {
        if (this.factory == null) {
            this.factory = new AnimationFactory(this);
        }
        return this.factory;
    }

    @Override
    public void renderMetaTileEntity(double x, double y, double z, float partialTicks) {
        if (isStructureFormed()) {
            IAnimatableMTE.super.renderMetaTileEntity(x, y, z, partialTicks);
        }
    }
}
