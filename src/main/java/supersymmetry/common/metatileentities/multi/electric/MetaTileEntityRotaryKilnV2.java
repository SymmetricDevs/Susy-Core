package supersymmetry.common.metatileentities.multi.electric;

import static supersymmetry.api.metatileentity.multiblock.SuSyPredicates.hiddenGearTooth;
import static supersymmetry.api.metatileentity.multiblock.SuSyPredicates.hiddenStates;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nonnull;

import net.minecraft.block.state.IBlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.RelativeDirection;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import supersymmetry.api.capability.SuSyDataCodes;
import supersymmetry.api.metatileentity.IAnimatableMTE;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.BlockGrinderCasing;
import supersymmetry.common.blocks.SuSyBlocks;

public class MetaTileEntityRotaryKilnV2 extends RecipeMapMultiblockController implements IAnimatableMTE {

    @SideOnly(Side.CLIENT)
    private BlockPos lightPos;
    @SideOnly(Side.CLIENT)
    private Vec3i transformation;
    @SideOnly(Side.CLIENT)
    private AxisAlignedBB renderBounding;

    @SideOnly(Side.CLIENT)
    private AnimationFactory factory;

    @Nullable
    private Collection<BlockPos> hiddenBlocks;

    public MetaTileEntityRotaryKilnV2(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.ROTARY_KILN);
    }

    protected static IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(MetalCasingType.STEEL_SOLID);
    }

    private static IBlockState getShellCasingState() {
        return SuSyBlocks.GRINDER_CASING.getState(BlockGrinderCasing.Type.WEAR_RESISTANT_LINED_MILL_SHELL);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityRotaryKilnV2(this.metaTileEntityId);
    }

    @NotNull
    @Override
    protected BlockPattern createStructurePattern() {
        // Different characters use common constraints. Copied from GCyM
        TraceabilityPredicate casingPredicate = states(getCasingState()).setMinGlobalLimited(6);
        TraceabilityPredicate maintenance = abilities(MultiblockAbility.MAINTENANCE_HATCH).setMinGlobalLimited(1)
                .setMaxGlobalLimited(1);

        return FactoryBlockPattern.start()
                .aisle("F         F", "LD   B   DR", "LDCCCBCCCDR", "GD   B   DG")
                .aisle("     F     ", "LDCCCBCCCDR", "L#########R", "LDCCCBCCCDR")
                .aisle("F         F", "LD   B   DR", "LDCCCSCCCDR", "GD   B   DG")
                .where('S', selfPredicate())
                .where('B', hiddenStates(getCasingState()))
                .where('C', hiddenStates(getShellCasingState()))
                .where('D', hiddenGearTooth(
                        RelativeDirection.LEFT.getRelativeFacing(getFrontFacing(), getUpwardsFacing(), false)
                                .getAxis()))
                .where('F', frames(Materials.Steel))
                .where('G',
                        states(MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_GEARBOX)))
                .where('L', casingPredicate
                        .or(autoAbilities(false, false, true, false, false, true, false))
                        .or(autoAbilities(true, false, false, false, false, false, false)).setMinGlobalLimited(0)
                        .or(maintenance))
                .where('R', casingPredicate
                        .or(autoAbilities(false, false, false, true, true, false, false))
                        .or(autoAbilities(true, false, false, false, false, false, false)).setMinGlobalLimited(0)
                        .or(maintenance))
                .where(' ', any())
                .where('#', air())
                .build();
    }

    @Override
    @Nullable
    public Collection<BlockPos> getHiddenBlocks() {
        return hiddenBlocks;
    }

    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Nonnull
    @Override
    @SideOnly(Side.CLIENT)
    protected ICubeRenderer getFrontOverlay() {
        return SusyTextures.ROTARY_KILN_OVERLAY;
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.hiddenBlocks = context.getOrDefault("Hidden", new ArrayList<>());
        World world = getWorld();

        // This will only be called on a server side world
        // so actually no need to check !world.isRemote
        if (world != null && !world.isRemote) {
            disableBlockRendering(true);
        }
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        World world = getWorld();
        if (world != null && !world.isRemote) {
            writeCustomData(SuSyDataCodes.RESET_RENDER_FIELDS, buf -> {
                /* Do nothing */
            });
            disableBlockRendering(false);
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        World world = getWorld();
        if (world != null && !world.isRemote) {
            disableBlockRendering(isStructureFormed()); // This is a bit ugly tho...
        }
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        if (dataId == SuSyDataCodes.RESET_RENDER_FIELDS) {
            this.lightPos = null;
            this.renderBounding = null;
            this.transformation = null;
        } else {
            super.receiveCustomData(dataId, buf);
        }
    }

    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if (this.renderBounding == null) {
            EnumFacing front = getFrontFacing();
            EnumFacing upwards = getUpwardsFacing();
            boolean flipped = isFlipped();
            EnumFacing left = RelativeDirection.LEFT.getRelativeFacing(front, upwards, flipped);
            EnumFacing up = RelativeDirection.UP.getRelativeFacing(front, upwards, flipped);
            BlockPos pos = getPos();

            var v1 = pos.offset(left.getOpposite(), 7).offset(up.getOpposite(), 3);
            var v2 = pos.offset(left, 7).offset(up, 4).offset(front.getOpposite(), 4);
            this.renderBounding = new AxisAlignedBB(v1, v2);
        }
        return renderBounding;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Vec3i getTransformation() {
        if (this.transformation == null) {
            EnumFacing front = getFrontFacing();
            EnumFacing upwards = getUpwardsFacing();
            boolean flipped = isFlipped();
            EnumFacing back = front.getOpposite();
            EnumFacing up = RelativeDirection.UP.getRelativeFacing(front, upwards, flipped);

            int xOff = back.getXOffset() - up.getXOffset();
            int yOff = back.getYOffset() - up.getYOffset();
            int zOff = back.getZOffset() - up.getZOffset();

            this.transformation = new Vec3i(xOff, yOff, zOff);
        }
        return transformation;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BlockPos getLightPos() {
        if (this.lightPos == null) {
            EnumFacing front = getFrontFacing();
            EnumFacing upwards = getUpwardsFacing();
            boolean flipped = isFlipped();
            EnumFacing back = front.getOpposite();
            EnumFacing up = RelativeDirection.UP.getRelativeFacing(front, upwards, flipped);

            this.lightPos = getPos().offset(up, 2).offset(back, 1);
        }
        return lightPos;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public AnimationFactory getFactory() {
        if (this.factory == null) {
            this.factory = new AnimationFactory(this);
        }
        return this.factory;
    }

    @SideOnly(Side.CLIENT)
    private <T extends MetaTileEntity & IAnimatableMTE> PlayState predicate(AnimationEvent<T> event) {
        event.getController().setAnimation(new AnimationBuilder()
                .addAnimation("rotary_kiln.animation", ILoopType.EDefaultLoopTypes.LOOP));
        return isActive() ? PlayState.CONTINUE : PlayState.STOP;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "controller", 0.0F, this::predicate));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderMetaTileEntity(double x, double y, double z, float partialTicks) {
        if (isStructureFormed()) {
            IAnimatableMTE.super.renderMetaTileEntity(x, y, z, partialTicks);
        }
    }
}
