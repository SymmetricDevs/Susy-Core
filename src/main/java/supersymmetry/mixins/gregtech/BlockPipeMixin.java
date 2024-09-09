package supersymmetry.mixins.gregtech;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import gregtech.api.block.BuiltInRenderBlock;
import gregtech.api.cover.Cover;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.pipenet.IBlockAppearance;
import gregtech.api.pipenet.PipeNet;
import gregtech.api.pipenet.WorldPipeNet;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.unification.material.Material;
import gregtech.integration.ctm.IFacadeWrapper;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import supersymmetry.api.blocks.IForcedStates;
import supersymmetry.common.blocks.BlockSheetedFrame;

import java.util.List;

import static supersymmetry.common.blocks.SuSyMetaBlocks.SHEETED_FRAMES;

@Mixin(value = BlockPipe.class, remap = false)
public abstract class BlockPipeMixin<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>, NodeDataType,
        WorldPipeNetType extends WorldPipeNet<NodeDataType, ? extends PipeNet<NodeDataType>>>
        extends BuiltInRenderBlock implements ITileEntityProvider, IFacadeWrapper, IBlockAppearance {

    @Shadow @Final
    protected ThreadLocal<IPipeTile<PipeType, NodeDataType>> tileEntities;

    @Shadow
    public abstract IPipeTile<PipeType, NodeDataType> getPipeTileEntity(IBlockAccess world, BlockPos selfPos);

    @Shadow
    public abstract ItemStack getDropItem(IPipeTile<PipeType, NodeDataType> var1);

    @Shadow
    public abstract boolean canPipesConnect(IPipeTile<PipeType, NodeDataType> var1, EnumFacing var2, IPipeTile<PipeType, NodeDataType> var3);

    @Shadow
    public abstract boolean canPipeConnectToBlock(IPipeTile<PipeType, NodeDataType> var1, EnumFacing var2, @Nullable TileEntity var3);

    // shouldn't ever be used
    public BlockPipeMixin(net.minecraft.block.material.Material materialIn) {
        super(materialIn);
    }

    //@Inject(method = "onPipeActivated", at = @At(value = "INVOKE_ASSIGN", ordinal = 0))
    // mixin methods always return void, with any potential "return" calls done through callBackInfo
    // removing descriptor causes unable to locate obfuscation mapping
    @Inject(method = "onPipeActivated(Lnet/minecraft/world/World;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/util/EnumHand;Lnet/minecraft/util/EnumFacing;Lcodechicken/lib/raytracer/CuboidRayTraceResult;Lgregtech/api/pipenet/tile/IPipeTile;)Z",
            at = @At("HEAD"), cancellable = true)
    protected void onOnPipeActivated(World world, IBlockState state, BlockPos pos, EntityPlayer entityPlayer, EnumHand hand,
                                   EnumFacing side, CuboidRayTraceResult hit, IPipeTile<PipeType, NodeDataType> pipeTile, CallbackInfoReturnable<Boolean> callBackInfoR) {
        if (pipeTile == null || pipeTile.getFrameMaterial() == null || ((IForcedStates) pipeTile).getForcedState() == 0) return;

        ItemStack handStack = entityPlayer.getHeldItem(hand);

        // try to change its saved orientation
        BlockSheetedFrame.ToolReactions itemToolCheck = BlockSheetedFrame.ToolReactions.getReaction(handStack);
        if (itemToolCheck != BlockSheetedFrame.ToolReactions.NONE) {
            int resultOrdinal = BlockSheetedFrame.ToolReactions.reactionResult(itemToolCheck, ((IForcedStates) pipeTile).getForcedState() - 1);
            ((IForcedStates) pipeTile).setForcedState(resultOrdinal + 1);

            // clear "blocked" connections [setConnection is the connection facing relative to the one calling, the connection state, and if the neighbor is the one making the call/ "updating" caller
            if (resultOrdinal != BlockSheetedFrame.FrameEnumAxis.NONE.ordinal()) {
                for (EnumFacing.Axis currAxis : EnumFacing.Axis.values()) {
                    if (currAxis.ordinal() == resultOrdinal) continue; // don't prune connections on axis
                    pipeTile.setConnection(EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.POSITIVE, currAxis), false, false);
                    pipeTile.setConnection(EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.NEGATIVE, currAxis), false, false);
                }
            }

            callBackInfoR.setReturnValue(true);
        }

        if (handStack.getItem().getToolClasses(handStack).contains(ToolClasses.CROWBAR)) {
            final Material prevMat = pipeTile.getFrameMaterial();
            ((IForcedStates) pipeTile).setForcedState(0);
            ((TileEntityPipeBase<?, ?>) pipeTile).setFrameMaterial(null);
            spawnAsEntity(world, pos, SHEETED_FRAMES.get(prevMat).getItem(prevMat));
            callBackInfoR.setReturnValue(true);
        }
    }

    @Inject(method = "activateFrame(Lnet/minecraft/world/World;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/util/EnumHand;Lcodechicken/lib/raytracer/CuboidRayTraceResult;Lgregtech/api/pipenet/tile/IPipeTile;)Z",
            at = @At("HEAD"), cancellable = true)
    private void onOnActivateFrame(World world, IBlockState state, BlockPos pos, EntityPlayer entityPlayer, EnumHand hand, CuboidRayTraceResult hit, IPipeTile<PipeType, NodeDataType> pipeTile, CallbackInfoReturnable<Boolean> callBackInfoR) {
        if (pipeTile.getFrameMaterial() == null || ((IForcedStates) pipeTile).getForcedState() == 0) return; // cancel custom logic early if normal block or no frame exists
        callBackInfoR.setReturnValue(SHEETED_FRAMES.get(pipeTile.getFrameMaterial()).onBlockActivated(true, world, pos, state, entityPlayer, hand, hit.sideHit, (float) hit.hitVec.x, (float) hit.hitVec.y, (float) hit.hitVec.z));
    }

    //@Inject(method = "onEntityCollision", at = @At(value = "INVOKE_ASSIGN", ordinal = 1), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    @Inject(method = "onEntityCollision(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/entity/Entity;)V",
            at = @At("HEAD"), cancellable = true)
    public void onOnEntityCollision(World worldIn, BlockPos pos, IBlockState state, Entity entityIn, CallbackInfo callbackInfo) {
        // only called when shorter side of frame is collided with for some reason
        IPipeTile<PipeType, NodeDataType> pipeTile = getPipeTileEntity(worldIn, pos);
        if (pipeTile == null || pipeTile.getFrameMaterial() == null || ((IForcedStates) pipeTile).getForcedState() == 0) return;
        SHEETED_FRAMES.get(pipeTile.getFrameMaterial()).onEntityCollision(worldIn, pos, state, entityIn);
        callbackInfo.cancel(); // don't do frame logic
    }

    // <Lnet/minecraft/item/ItemStack;> - https://fabricmc.net/wiki/tutorial:mixin_injects : because generics dont exist at run time, they arent needed
    @Inject(method = "getDrops(Lnet/minecraft/util/NonNullList;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)V",
            at = @At("HEAD"), cancellable = true)
    public void onGetDrops(@NotNull NonNullList<ItemStack> drops, @NotNull IBlockAccess world, @NotNull BlockPos pos, @NotNull IBlockState state, int fortune, CallbackInfo callbackInfo) {
        IPipeTile<PipeType, NodeDataType> pipeTile = this.tileEntities.get() == null ? this.getPipeTileEntity(world, pos) : this.tileEntities.get();
        if (pipeTile.getFrameMaterial() == null || ((IForcedStates) pipeTile).getForcedState() == 0) return;
        BlockSheetedFrame sheetedFrame = SHEETED_FRAMES.get(pipeTile.getFrameMaterial());
        drops.add(sheetedFrame.getItem(pipeTile.getFrameMaterial()));
        drops.add(getDropItem(pipeTile));

        callbackInfo.cancel();
    }

    @Inject(method = "canConnect(Lgregtech/api/pipenet/tile/IPipeTile;Lnet/minecraft/util/EnumFacing;)Z", at = @At("HEAD"), cancellable = true)
    public void onCanConnect(IPipeTile<PipeType, NodeDataType> selfTile, EnumFacing facing, CallbackInfoReturnable<Boolean> callbackInfoR) {
        if (((IForcedStates) selfTile).getForcedState() == 0) return;
        int rotationOrdinal = ((IForcedStates) selfTile).getForcedState() - 1;

        boolean result = rotationOrdinal == BlockSheetedFrame.FrameEnumAxis.NONE.ordinal() || rotationOrdinal == facing.getAxis().ordinal();
        if (!result) callbackInfoR.setReturnValue(false);

        // simplified version of blockPipe check which only attempts to set the result to true
        result = false; // reset result to default as false, now that frame check has been done
        if (selfTile.getPipeWorld().getBlockState(selfTile.getPipePos().offset(facing)).getBlock() != Blocks.AIR) {
            Cover cover = selfTile.getCoverableImplementation().getCoverAtSide(facing);
            if (cover == null || cover.canPipePassThrough()) {
                TileEntity other = selfTile.getNeighbor(facing);
                if (other instanceof IPipeTile) {
                    cover = ((IPipeTile<?, ?>)other).getCoverableImplementation().getCoverAtSide(facing.getOpposite());
                    result = (cover == null || cover.canPipePassThrough()) && this.canPipesConnect(selfTile, facing, (IPipeTile<PipeType, NodeDataType>) other);
                } else {
                    result = this.canPipeConnectToBlock(selfTile, facing, other);
                }
            }
        }

        callbackInfoR.setReturnValue(result);
    }

    // <Lnet/minecraft/util/math/AxisAlignedBB;> - generics are not real
    @Inject(method = "addCollisionBoxToList(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/entity/Entity;Z)V",
            at = @At("HEAD"), cancellable = true)
    public void onAddCollisionBoxToList(@NotNull IBlockState state, @NotNull World worldIn, @NotNull BlockPos pos,
                                        @NotNull AxisAlignedBB entityBox, @NotNull List<AxisAlignedBB> collidingBoxes,
                                        @Nullable Entity entityIn, boolean isActualState, CallbackInfo callBackInfo) {
        IPipeTile<PipeType, NodeDataType> pipeTile = getPipeTileEntity(worldIn, pos);
        if (pipeTile == null || pipeTile.getFrameMaterial() == null || ((IForcedStates) pipeTile).getForcedState() == 0) return;

        // get equivalent state for calculation of bounding box
        IBlockState equivalentSheetedState = BlockSheetedFrame.determineSheetedState(worldIn, pos);
        if (equivalentSheetedState == null) return; // if something went wrong in getting state, let typical logic run

        // do custom bounding box based on sheeted frame
        AxisAlignedBB box = SHEETED_FRAMES.get(pipeTile.getFrameMaterial()).getCollisionBoundingBox(equivalentSheetedState, worldIn, pos).offset(pos);
        if (box.intersects(entityBox)) collidingBoxes.add(box);

        // only hitbox is frame
        callBackInfo.cancel();
    }
}
