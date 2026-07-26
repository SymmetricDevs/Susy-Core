package supersymmetry.common.metatileentities.multi.electric;

import static supersymmetry.api.blocks.VariantHorizontalRotatableBlock.FACING;
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
import supersymmetry.api.metatileentity.multiblock.SuSyPredicates;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.*;

public class MetaTileEntityLunarBucketWheelExcavator extends RecipeMapMultiblockController implements IAnimatableMTE {

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

    public MetaTileEntityLunarBucketWheelExcavator(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.LUNAR_BUCKET_WHEEL_EXCAVATOR);
    }

    protected static IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(MetalCasingType.STAINLESS_CLEAN);
    }

    private static IBlockState getWheelCasingState() {
        return SuSyBlocks.MULTIBLOCK_CASING.getState(BlockSuSyMultiblockCasing.CasingType.HARDENED_TITANIUM_CASING);
    }

    protected IBlockState trackState() {
        return SuSyBlocks.VEHICLE_TRACK.getState(BlockVehicleTrack.VehicleTrackType.DEFAULT);
    }

    protected IBlockState conveyorState() {
        return SuSyBlocks.BWE_CONVEYOR_BELT.getState(BlockBWEConveyorBelt.BWEConveyorType.DEFAULT);
    }

    protected TraceabilityPredicate trackOrientation() {
        return SuSyPredicates.horizontalOrientation(this, trackState(), RelativeDirection.FRONT, FACING);
    }

    protected TraceabilityPredicate trackOrientation2() {
        return SuSyPredicates.horizontalOrientation(this, trackState(), RelativeDirection.BACK, FACING);
    }

    protected TraceabilityPredicate conveyorOrientation() {
        return SuSyPredicates.horizontalOrientation(this, conveyorState(), RelativeDirection.FRONT, FACING);
    }

    protected TraceabilityPredicate conveyorOrientation2() {
        return SuSyPredicates.horizontalOrientation(this, conveyorState(), RelativeDirection.BACK, FACING);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityLunarBucketWheelExcavator(this.metaTileEntityId);
    }

    @NotNull
    @Override
    protected BlockPattern createStructurePattern() {
        // Different characters use common constraints. Copied from GCyM
        TraceabilityPredicate casingPredicate = states(getCasingState()).setMinGlobalLimited(120);

        return FactoryBlockPattern.start()
                .aisle("          TTTTTTTT           ", "                             ",
                        "         HHHHHHHH            ", "         HHHHHHHH            ",
                        "                             ", "                             ",
                        "                             ", "                             ",
                        "                             ", "                             ",
                        "                             ")
                .aisle("          TTTTTTTT           ", "             AA              ",
                        "        HHHHHHHHHH           ", "        HHHHHHHHHH       A   ",
                        "               AA       AEA  ", "              AAA     AACA   ",
                        "           CAAAAA    AC      ", "           C   AA  AAC       ",
                        "           C   AAAAC         ", "            C AACCC          ",
                        "             CAC             ")
                .aisle("           AAAAAA            ", "            AAAA             ",
                        "        HEEEEEEEEH           ", "        HHHHHHEEEAAAAAAAAA   ",
                        "              EAEA    EEPEP  ", "OPPPPPPPPPPPPPPAEA   EPP     ",
                        "AC             AEA EEP       ", "  CCCCCCCCCC   AEEEPP        ",
                        "               PPPP          ", "                             ",
                        "                             ")
                .aisle("           AAAAAA            ", "            AAAA         B   ",
                        "        HEEEEEEEEH     BBBBB ", "        HHHHHHEEEH     BBBBB ",
                        "              EAEA    BBBBBBB", "OPPPPPPPPPPPPPPAEA   E BBBBB ",
                        "AC             AEA EEP BBBBB ", "  CCCCCCCCCC   AEEEPP    B   ",
                        "               PPPP          ", "                             ",
                        "                             ")
                .aisle("          TTTTTTTT           ", "             AA              ",
                        "        HHHHHHHHHH           ", "        HHHHHHHHHH       C   ",
                        "               AA       CEC  ", "              ASA      CCC   ",
                        "           CAAAMA    CCC     ", "           C   AA  CCC       ",
                        "           C   AACCC         ", "            C AACCC          ",
                        "             CAC             ")
                .aisle("          TTTTTTTT           ", "                             ",
                        "         HHHHHHHH            ", "         HHHHHHHH            ",
                        "                             ", "                             ",
                        "                             ", "                             ",
                        "                             ", "                             ",
                        "                             ")
                .where('T', trackOrientation().or(trackOrientation2()))
                .where('A', casingPredicate)
                .where('B', hiddenStates(getWheelCasingState()))
                .where('C', frames(Materials.StainlessSteel))
                .where('E', states(MetaBlocks.TURBINE_CASING.getState(
                        BlockTurbineCasing.TurbineCasingType.STAINLESS_STEEL_GEARBOX)))
                .where('F', states(MetaBlocks.TURBINE_CASING.getState(
                        BlockTurbineCasing.TurbineCasingType.TITANIUM_GEARBOX)))
                .where('O', casingPredicate
                        .or(autoAbilities(false, false, false, true, false, false, false)))
                .where('H', casingPredicate
                        .or(autoAbilities(true, false, true, false, false, false, false)))
                .where('M', casingPredicate
                        .or(autoAbilities(false, true, false, false, false, false, false)))
                .where('S', selfPredicate())
                .where('P', conveyorOrientation().or(conveyorOrientation2()))
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
        return Textures.CLEAN_STAINLESS_STEEL_CASING;
    }

    @Nonnull
    @Override
    @SideOnly(Side.CLIENT)
    protected ICubeRenderer getFrontOverlay() {
        return SusyTextures.MINING_DRILL_OVERLAY;
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

            var v1 = pos.offset(left.getOpposite(), 3).offset(up.getOpposite(), 3);
            var v2 = pos.offset(left, 7).offset(up, 4).offset(front.getOpposite(), 4);// I have no idea what these
                                                                                      // numbers do
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
                .addAnimation("default_loop", ILoopType.EDefaultLoopTypes.LOOP));
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
