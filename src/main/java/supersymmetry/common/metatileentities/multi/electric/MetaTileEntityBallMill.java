package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.RelativeDirection;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
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
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import supersymmetry.api.capability.SuSyDataCodes;
import supersymmetry.api.metatileentity.IAnimatableMTE;
import supersymmetry.api.util.BlockRenderManager;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.BlockGrinderCasing;
import supersymmetry.common.blocks.SuSyBlocks;

import java.util.ArrayList;
import java.util.List;

import static supersymmetry.api.metatileentity.multiblock.SuSyPredicates.hiddenGearTooth;
import static supersymmetry.api.metatileentity.multiblock.SuSyPredicates.hiddenStates;

public class MetaTileEntityBallMill extends RecipeMapMultiblockController implements IAnimatableMTE {

    @SideOnly(Side.CLIENT)
    private AnimationFactory factory;

    private List<BlockPos> hiddenBlocks;

    public MetaTileEntityBallMill(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap) {
        super(metaTileEntityId, recipeMap);
    }

    private static IBlockState getGearBoxState() {
        return MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_GEARBOX);
    }

    private static IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    private static IBlockState getShellCasingState() {
        return SuSyBlocks.GRINDER_CASING.getState(BlockGrinderCasing.Type.WEAR_RESISTANT_LINED_MILL_SHELL);
    }

    private static IBlockState getShellHeadState() {
        return SuSyBlocks.GRINDER_CASING.getState(BlockGrinderCasing.Type.WEAR_RESISTANT_LINED_SHELL_HEAD);
    }

    private static IBlockState getDiaphragmState() {
        return SuSyBlocks.GRINDER_CASING.getState(BlockGrinderCasing.Type.INTERMEDIATE_DIAPHRAGM);
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
    @SideOnly(Side.CLIENT)
    public ICubeRenderer getBaseTexture(IMultiblockPart part) {
        if (part instanceof IMultiblockAbilityPart<?> abilityPart) {
            var ability = abilityPart.getAbility();
            if (ability != MultiblockAbility.MAINTENANCE_HATCH && ability != MultiblockAbility.INPUT_ENERGY) {
                return SusyTextures.BALL_MILL_SHELL;
            }
        }
        return Textures.SOLID_STEEL_CASING;
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        var shell = states(getShellCasingState());

        return FactoryBlockPattern.start()
                .aisle(" XMMMXXXXXXXX", "  NMM        ", "             ", "  G          ", "  G          ", "  G          ", "             ", "             ")
                .aisle(" X          X", "             ", "  G          ", "  HCCCCCCCCH ", "  HCCCCCCCCH ", "  HCCCCCCCCH ", "  G          ", "             ")
                .aisle(" X          X", " XG         X", " XHCCCCCCCCHX", " XH#####D##HX", " XH#####D##HX", " XH#####D##H ", "  HCCCCCCCCH ", "  G          ")
                .aisle(" X          X", "  G          ", "  HCCCCCCCCH ", "OXH#####D##H ", "AA######D###Y", "ZXH#####D##HI", "  HCCCCCCCCH ", "  G          ")
                .aisle(" X          X", " XG         X", " XHCCCCCCCCHX", " XH#####D##HX", " XH#####D##HX", " XH#####D##H ", "  HCCCCCCCCH ", "  G          ")
                .aisle(" X          X", "             ", "  G          ", "  HCCCCCCCCH ", "  HCCCCCCCCH ", "  HCCCCCCCCH ", "  G          ", "             ")
                .aisle(" XMMMXXXXXXXX", "  NSM        ", "             ", "  G          ", "  G          ", "  G          ", "             ", "             ")
                .where('M', states(getCasingState()).or(autoAbilities(
                        true, true, false,
                        false, false, false, false
                )))
                .where('Y', abilities(MultiblockAbility.IMPORT_ITEMS).or(shell))
                .where('Z', abilities(MultiblockAbility.EXPORT_FLUIDS).or(shell))
                .where('I', abilities(MultiblockAbility.IMPORT_ITEMS).or(shell))
                .where('O', abilities(MultiblockAbility.EXPORT_ITEMS).or(shell))
                .where('A', states(getShellCasingState()))
                .where('C', hiddenStates(getShellCasingState()))
                .where('H', hiddenStates(getShellHeadState()))
                .where('D', hiddenStates(getDiaphragmState()))
                .where('G', hiddenGearTooth(getFrontFacing().rotateYCCW().getAxis()))
                .where('N', states(getGearBoxState()))
                .where('X', frames(Materials.Steel))
                .where('S', selfPredicate())
                .where('#', air())
                .where(' ', any())
                .build();
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.hiddenBlocks = context.getOrDefault("Hidden", new ArrayList<>());
        World world = getWorld();
        if (world != null && !world.isRemote) {
            disableBlockRendering(true);
        }
    }

    protected void disableBlockRendering(boolean disable) {
        if (hiddenBlocks != null && !hiddenBlocks.isEmpty()) {
            int id = getWorld().provider.getDimension();

            writeCustomData(SuSyDataCodes.UPDATE_BLOCK_RENDERING, buf -> {
                buf.writeInt(id);
                buf.writeBoolean(disable);
                // Only write blockPoses into the packet when we're disabling the rendering
                if (disable) {
                    buf.writeInt(hiddenBlocks.size());
                    hiddenBlocks.forEach(buf::writeBlockPos);
                }
            });
        }
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        World world = getWorld();
        if (world != null && !world.isRemote) {
            disableBlockRendering(false);
        }
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        if (this.getWorld() != null && !this.getWorld().isRemote) {
            BlockRenderManager.removeDisableModel(getPos(), true);
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        disableBlockRendering(isStructureFormed()); // This is a bit ugly tho...
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        if (dataId == SuSyDataCodes.UPDATE_BLOCK_RENDERING) {
            boolean updateRendering = getWorld().provider.getDimension() == buf.readInt();
            boolean disable = buf.readBoolean();
            if (disable) {
                List<BlockPos> hiddenBlocks = new ArrayList<>();
                int size = buf.readInt();
                for (int i = 0; i < size; i++) {
                    hiddenBlocks.add(buf.readBlockPos());
                }
                BlockRenderManager.addDisableModel(getPos(), hiddenBlocks, updateRendering);
            } else {
                BlockRenderManager.removeDisableModel(getPos(), updateRendering);
            }
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
        EnumFacing front = getFrontFacing();
        EnumFacing up = getUpwardsFacing();
        EnumFacing left = RelativeDirection.LEFT.getRelativeFacing(front, up, isFlipped());

        BlockPos pos = getPos();

        // Oddly, the left appears to be switched.
        var v1 = pos.offset(left.getOpposite(), 3).offset(EnumFacing.DOWN, 1).offset(front, 0);
        var v2 = pos.offset(left, 10).offset(EnumFacing.UP, 8).offset(front.getOpposite(), 6);

        return new AxisAlignedBB(v1, v2);
    }

    @SideOnly(Side.CLIENT)
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
    @Override
    public BlockPos getLightPos() {
        EnumFacing front = getFrontFacing();
        EnumFacing back = front.getOpposite();
        EnumFacing up = getUpwardsFacing();
        EnumFacing left = RelativeDirection.LEFT.getRelativeFacing(front, up, isFlipped());

        return getPos().offset(up, 6).offset(back, 3).offset(left, 4); // TODO
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

    @SideOnly(Side.CLIENT)
    @Override
    public void renderMetaTileEntity(double x, double y, double z, float partialTicks) {
        if (isStructureFormed()) {
            IAnimatableMTE.super.renderMetaTileEntity(x, y, z, partialTicks);
        }
    }
}
