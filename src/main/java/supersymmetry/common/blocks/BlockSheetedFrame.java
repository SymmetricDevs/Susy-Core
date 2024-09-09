package supersymmetry.common.blocks;

import gregtech.api.GregTechAPI;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.block.ItemBlockPipe;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTUtility;
import gregtech.common.blocks.BlockFrame;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.properties.PropertyMaterial;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import gregtech.api.unification.material.Material;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.SusyLog;
import supersymmetry.api.blocks.IForcedStates;
import supersymmetry.api.unification.material.info.SuSyMaterialIconType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

import static supersymmetry.common.blocks.SuSyMetaBlocks.SHEETED_FRAMES;

public class BlockSheetedFrame extends Block {

    public static final int UPDATE_ROTATION_STATE = GregtechDataCodes.assignId();

    public static final PropertyEnum<BlockSheetedFrame.FrameEnumAxis> SHEETED_FRAME_AXIS = PropertyEnum.<BlockSheetedFrame.FrameEnumAxis>create("axis", BlockSheetedFrame.FrameEnumAxis.class);

    public final PropertyMaterial variantProperty;

    public BlockSheetedFrame(Material[] materials)
    {
        super(net.minecraft.block.material.Material.IRON);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        this.setHardness(3.0F);
        this.setResistance(5.0F);
        this.setTranslationKey("sheeted_frame");
        this.setCreativeTab(GregTechAPI.TAB_GREGTECH_MATERIALS);
        this.variantProperty = PropertyMaterial.create("variant", materials);
        BlockStateContainer stateContainer = this.createStateContainer();
        //I have literally no clue what this does, but shit breaks if you remove it apparently
        ObfuscationReflectionHelper.setPrivateValue(Block.class, this, stateContainer, 21); //this.stateContainer
        //setDefaultState(stateContainer.getBaseState());
        this.setDefaultState(stateContainer.getBaseState().withProperty(SHEETED_FRAME_AXIS, FrameEnumAxis.Y));
    }

    /**
     * Called by ItemBlocks just before a block is actually set in the world, to allow for adjustments to the
     * IBlockstate
     */
    @Override @NotNull
    public IBlockState getStateForPlacement(@NotNull World worldIn, @NotNull BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, @NotNull EntityLivingBase placer)
    {
        return this.getStateFromMeta(meta).withProperty(SHEETED_FRAME_AXIS, BlockSheetedFrame.FrameEnumAxis.fromFacingAxis(facing.getAxis()));
    }

    protected BlockStateContainer createStateContainer() {
        return new BlockStateContainer(this, SHEETED_FRAME_AXIS, this.variantProperty);
    }

    @Override @Nonnull
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this);
    }

    /**
     * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
     * blockstate.
     * @deprecated call via {@link IBlockState#withRotation(Rotation)} whenever possible. Implementing/overriding is
     * fine.
     */
    @Override
    public @NotNull IBlockState withRotation(@NotNull IBlockState state, Rotation rot)
    {
        switch (rot) {
            case COUNTERCLOCKWISE_90, CLOCKWISE_90 -> {
                return switch (state.getValue(SHEETED_FRAME_AXIS)) {
                    case X -> state.withProperty(SHEETED_FRAME_AXIS, FrameEnumAxis.Z);
                    case Z -> state.withProperty(SHEETED_FRAME_AXIS, FrameEnumAxis.X);
                    default -> state;
                };
            }

            default -> {
                return state;
            }
        }
    }

    public enum FrameEnumAxis implements IStringSerializable
    {
        X("x", EnumFacing.Axis.X),
        Y("y", EnumFacing.Axis.Y),
        Z("z", EnumFacing.Axis.Z),
        NONE("none", null);

        private final String name;

        private final EnumFacing.Axis axis;

        FrameEnumAxis(String name, EnumFacing.Axis axis) {
            this.name = name;
            this.axis = axis;
        }

        public String toString()
        {
            return this.name;
        }

        public static FrameEnumAxis fromFacingAxis(EnumFacing.Axis axis)
        {
            if (axis == null) return NONE;
            return switch (axis) {
                case X -> X;
                case Y -> Y;
                case Z -> Z;
            };
        }

        public @NotNull String getName()
        {
            return this.name;
        }

        public @Nullable EnumFacing.Axis getAxis() { return this.axis; }

        // never returns none
        public static FrameEnumAxis fromFacing(EnumFacing facing) {
            return FrameEnumAxis.values()[facing.getAxis().ordinal()];
        }
    }

    @Override @Deprecated
    public boolean isOpaqueCube(@NotNull IBlockState state) { return false; }


    @Override @SideOnly(Side.CLIENT)
    public @NotNull BlockRenderLayer getRenderLayer()
    {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    public int damageDropped(@Nonnull IBlockState state) {
        return this.getMetaFromState(state);
    }

    @Nonnull
    public IBlockState getStateFromMeta(int meta) {
        if (meta > 15) meta = 0;

        //axis of block is related two 2 most significant bits in first four bits; indexing with (meta % 16) /4
        return this.getDefaultState().withProperty(this.variantProperty, this.variantProperty.getAllowedValues().get(meta & 3))
                .withProperty(SHEETED_FRAME_AXIS, FrameEnumAxis.values()[(meta & 15) >>> 2]);
    }

    public int getMetaFromState(IBlockState state) {
        // place axis value in top two bits of first four bits of meta (X: 00, Y: 01, Z: 10, NONE: 11)
        int meta = (state.getValue(SHEETED_FRAME_AXIS).ordinal() << 2);

        //place result in lowest two bits
        meta |= this.variantProperty.getAllowedValues().indexOf(state.getValue(this.variantProperty));

        return meta;
    }

    public String getHarvestTool(IBlockState state) {
        Material material = state.getValue(this.variantProperty);
        return ModHandler.isMaterialWood(material) ? "axe" : "wrench";
    }

    @Nonnull
    public SoundType getSoundType(IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nullable Entity entity) {
        Material material = state.getValue(this.variantProperty);
        return ModHandler.isMaterialWood(material) ? SoundType.WOOD : SoundType.METAL;
    }

    public int getHarvestLevel(@Nonnull IBlockState state) {
        return 1;
    }

    @Nonnull
    public net.minecraft.block.material.Material getMaterial(IBlockState state) {
        Material material = state.getValue(this.variantProperty);
        return ModHandler.isMaterialWood(material) ? net.minecraft.block.material.Material.WOOD : super.getMaterial(state);
    }

    public void getSubBlocks(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> list) {
        //bit shift down returned 4 bits from meta to look at axis, only display y-aligned
        blockState.getValidStates().stream()
                .filter(blockState -> blockState.getValue(variantProperty) != Materials.NULL && getMetaFromState(blockState) >>> 2 == 1)
                .forEach(blockState -> list.add(getItem(blockState)));
    }

    // returns null to indicate an invalid/ non-existent sheeted frame state equivalent, or returns the equivalent sheeted state
    public static IBlockState determineSheetedState(IBlockAccess world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);

        if (state.getBlock() instanceof BlockSheetedFrame) {
            return state;
        }

        if (state.getBlock() instanceof BlockPipe) {
            IPipeTile<?, ?> pipetile = ((BlockPipe<?, ?, ?>) state.getBlock()).getPipeTileEntity(world, pos);
            if (pipetile == null) return null;

            int rotationOrdinal = ((IForcedStates) pipetile).getForcedState() - 1;
            Material mat = pipetile.getFrameMaterial();
            if (rotationOrdinal < 0 || mat == null) return null;

            return SHEETED_FRAMES.get(mat).getDefaultState().withProperty(SHEETED_FRAME_AXIS, FrameEnumAxis.values()[rotationOrdinal]);
        }

        return null;
    }

    public ItemStack getItem(Material material) {
        return getItem(this.getDefaultState().withProperty(this.variantProperty, material).withProperty(SHEETED_FRAME_AXIS, FrameEnumAxis.Y));
    }

    // doesn't really make sense to drop based on orientation, but the method is here just in case
    public ItemStack getItem(Material material, int orientationOrdinal) {
        return getItem(this.getDefaultState().withProperty(this.variantProperty, material).withProperty(SHEETED_FRAME_AXIS, FrameEnumAxis.values()[orientationOrdinal]));
    }

    public static ItemStack getItem(IBlockState blockState) {
        return GTUtility.toItem(blockState);
    }

    public IBlockState getBlock(Material material) {
        return getDefaultState().withProperty(this.variantProperty, material).withProperty(SHEETED_FRAME_AXIS, FrameEnumAxis.Y);
    }

    public Material getGtMaterial(int meta) {
        //only bottom two bits are relevant for getting material
        return this.variantProperty.getAllowedValues().get((meta & 3));
    }

    public Material getGtMaterial(IBlockState state) {
        return this.getGtMaterial(this.getMetaFromState(state));
    }

    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    public enum ToolReactions {
        ROTATE,
        MUTATE,
        NONE;

        public static ToolReactions getReaction(ItemStack stack) {
            if (stack == null) return NONE;
            if (stack.getItem().getToolClasses(stack).contains(ToolClasses.SCREWDRIVER)) return ROTATE;
            if (stack.getItem().getToolClasses(stack).contains(ToolClasses.HARD_HAMMER)) return MUTATE;
            return NONE;
        }

        public static int reactionResult(ToolReactions reaction, int state) {
            if (reaction == ROTATE) {
                return (state + 1) % 3;
            } else if (reaction == MUTATE) {
                return state == FrameEnumAxis.NONE.ordinal() ? FrameEnumAxis.Y.ordinal() : FrameEnumAxis.NONE.ordinal();
            }

            return state;
        }
    }

    @Override
    public boolean onBlockActivated(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state,
                                    @NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull EnumFacing facing,
                                    float hitX, float hitY, float hitZ) {
        return onBlockActivated(false, world, pos, state, player, hand, facing, hitX, hitY, hitZ);
    }

    public boolean onBlockActivated(boolean isPipe, @NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state,
                                    @NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull EnumFacing facing,
                                    float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack.isEmpty()) {
            return false;
        }

        // pipes surrounded by frames have their own implementation within the mixin for these behaviors
        if (!isPipe) {
            // special action on screwdrivers and wrenches, though changes are done pipe-side if blockstate "contains" pipe
            if ((state.getBlock() instanceof BlockSheetedFrame)) {
                if (ToolReactions.getReaction(stack) == ToolReactions.MUTATE) {
                    world.setBlockState(pos, state.withProperty(SHEETED_FRAME_AXIS, state.getValue(SHEETED_FRAME_AXIS) == FrameEnumAxis.NONE ? FrameEnumAxis.Y : FrameEnumAxis.NONE));
                    return true;
                } else if (ToolReactions.getReaction(stack) == ToolReactions.ROTATE) {
                    world.setBlockState(pos, state.withProperty(SHEETED_FRAME_AXIS, FrameEnumAxis.values()[(state.getValue(SHEETED_FRAME_AXIS).ordinal() + 1) % 3]));
                    return true;
                }
            }

            // replace frame with pipe and set the frame material to this frame
            if (stack.getItem() instanceof ItemBlockPipe) {
                return replaceWithFramedPipe(world, pos, state, player, stack, facing);
            }
        }

        // check if frame block, return if not
        BlockSheetedFrame sheetedFrameBlock = getFrameBlockFromItem(stack);
        if (sheetedFrameBlock == null) return false;

        BlockPos.PooledMutableBlockPos blockPos = BlockPos.PooledMutableBlockPos.retain();
        blockPos.setPos(pos);

        // determine ordinal for orientation
        int rotationOrdinal;
        if (state.getBlock() instanceof BlockSheetedFrame) {
            rotationOrdinal = state.getValue(SHEETED_FRAME_AXIS).ordinal();
        } else {
            // in theory te should always be pipe, otherwise this would never be called, but other methods double check, so I will too
            TileEntity te = world.getTileEntity(blockPos);

            // get rotationOrdinal from stack in hand if state at pos is not valid
            if (!(te instanceof IPipeTile) || ((IPipeTile<?, ?>) te).getFrameMaterial() == null || ((IForcedStates) te).getForcedState() == 0) { // stored state of 0 implies no value, so 0 - 1 -> no value/ default 0) {
                rotationOrdinal = getStateFromMeta(stack.getMetadata()).getValue(SHEETED_FRAME_AXIS).ordinal(); // always going to be y
            } else {
                rotationOrdinal = ((IForcedStates) te).getForcedState() - 1; // stored state of 0 implies no value, so 0 - 1 -> no value/ default
            }
        }

        EnumFacing currBaseDir = facing; // default to side clicked if orientation is NONE
        if (rotationOrdinal != FrameEnumAxis.NONE.ordinal()) {
            try {
                // default to positive, or do neg if target has NONE in pos dir and only sheeted frame ahead (this is a monster)
                currBaseDir = EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.POSITIVE, FrameEnumAxis.values()[rotationOrdinal].axis);
                IBlockState currDirState = world.getBlockState((pos.offset(currBaseDir)));
                Block oppDirBlock = world.getBlockState((pos.offset(currBaseDir.getOpposite()))).getBlock();
                if (!(currDirState.getBlock() instanceof BlockAir) && oppDirBlock instanceof BlockAir ||
                        oppDirBlock instanceof BlockSheetedFrame &&
                        currDirState.getBlock() instanceof BlockSheetedFrame &&
                        currDirState.getValue(SHEETED_FRAME_AXIS) == FrameEnumAxis.NONE) {

                    currBaseDir = currBaseDir.getOpposite();
                }
            } catch (Exception e) {
                // if an error occurred, don't try to place
                return false;
            }
        }

        // attempts to place more frames vertically, up to 32 block tall tower
        for (int i = 0; i < 32; i++) {
            IBlockState targetState = world.getBlockState(blockPos);
            if (targetState.getBlock() instanceof BlockFrame || targetState.getBlock() instanceof BlockSheetedFrame) {
                blockPos.move(currBaseDir);
                continue;
            }

            // skips over pipes with non-null frame materials (has frame around it)
            TileEntity te = world.getTileEntity(blockPos);
            if (te instanceof IPipeTile && ((IPipeTile<?, ?>) te).getFrameMaterial() != null) {
                blockPos.move(currBaseDir);
                continue;
            }

            // try to place frame block if allowed, and if not check if the obstruction is a pipe base which can be framed
            if (canPlaceBlockAt(world, blockPos)) {
                // ensure placed block orientation matches base
                world.setBlockState(blockPos,
                        sheetedFrameBlock.getStateFromMeta(stack.getItem().getMetadata(stack.getItemDamage())).withProperty(SHEETED_FRAME_AXIS, FrameEnumAxis.fromFacing(currBaseDir)));

                SoundType type = ModHandler.isMaterialWood(sheetedFrameBlock.getGtMaterial(stack)) ? SoundType.WOOD : SoundType.METAL;
                world.playSound(null, pos, type.getPlaceSound(), SoundCategory.BLOCKS, (type.getVolume() + 1.0F) / 2.0F,
                        type.getPitch() * 0.8F);
                if (!player.capabilities.isCreativeMode) {
                    stack.shrink(1);
                }
                blockPos.release();
                return true;
            } else if (te instanceof TileEntityPipeBase<?, ?> pipeTile && pipeTile.getFrameMaterial() == null) {
                // "sheet" pipe if it is blocking further frame scaffolding
                pipeTile.setFrameMaterial(sheetedFrameBlock.getGtMaterial(stack));
                ((IForcedStates) pipeTile).setForcedState(rotationOrdinal + 1); // should work with mixin to store orientation

                // clear "blocked" connections [setConnection is the connection facing relative to the one calling, the connection state, and if the neighbor is the one making the call/ "updating" caller
                if (rotationOrdinal != BlockSheetedFrame.FrameEnumAxis.NONE.ordinal()) {
                    for (EnumFacing.Axis currAxis : EnumFacing.Axis.values()) {
                        if (currAxis.ordinal() == rotationOrdinal) continue; // don't prune connections on axis
                        pipeTile.setConnection(EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.POSITIVE, currAxis), false, false);
                        pipeTile.setConnection(EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.NEGATIVE, currAxis), false, false);
                    }
                }

                SoundType type = ModHandler.isMaterialWood(getGtMaterial(stack)) ? SoundType.WOOD : SoundType.METAL;
                world.playSound(null, pos, type.getPlaceSound(), SoundCategory.BLOCKS, (type.getVolume() + 1.0F) / 2.0F,
                        type.getPitch() * 0.8F);
                if (!player.capabilities.isCreativeMode) {
                    stack.shrink(1);
                }

                blockPos.release();
                return true;
            } else { // stops at obstructions, rather than continuing
                blockPos.release();
                return false;
            }
        }

        blockPos.release();
        return false;
    }

    public boolean replaceWithFramedPipe(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
                                         ItemStack stackInHand, EnumFacing facing) {
        BlockPipe<?, ?, ?> blockPipe = (BlockPipe<?, ?, ?>) ((ItemBlockPipe<?, ?>) stackInHand.getItem()).getBlock();
        if (blockPipe.getItemPipeType(stackInHand).getThickness() < 1) {
            ItemBlock itemBlock = (ItemBlock) stackInHand.getItem();
            IBlockState pipeState = blockPipe.getDefaultState();

            // these 0 values are not actually used by forge
            itemBlock.placeBlockAt(stackInHand, playerIn, worldIn, pos, facing, 0, 0, 0, pipeState);

            IPipeTile<?, ?> pipeTile = blockPipe.getPipeTileEntity(worldIn, pos);
            if (pipeTile instanceof TileEntityPipeBase) {
                ((TileEntityPipeBase<?, ?>) pipeTile).setFrameMaterial(getGtMaterial(state));
                ((IForcedStates) pipeTile).setForcedState(state.getValue(SHEETED_FRAME_AXIS).ordinal() + 1);
            } else {
                SusyLog.logger.atError().log("Pipe was not placed!");
                return false;
            }

            SoundType type = blockPipe.getSoundType(state, worldIn, pos, playerIn);
            worldIn.playSound(playerIn, pos, type.getPlaceSound(), SoundCategory.BLOCKS,
                    (type.getVolume() + 1.0F) / 2.0F, type.getPitch() * 0.8F);
            if (!playerIn.capabilities.isCreativeMode) {
                stackInHand.shrink(1);
            }
            return true;
        }

        return false;
    }

    public SoundType getSoundType(ItemStack stack) {
        return ModHandler.isMaterialWood(getGtMaterial(stack)) ? SoundType.WOOD : SoundType.METAL;
    }

    public Material getGtMaterial(ItemStack stack) {
        return variantProperty.getAllowedValues().get(stack.getMetadata() & 3);
    }

    public static BlockFrame getFrameFromSheeted(ItemStack stack) {
        BlockSheetedFrame itemBlock = getFrameBlockFromItem(stack);
        if (itemBlock == null) return null;

        return MetaBlocks.FRAMES.get(itemBlock.getGtMaterial(stack));
    }

    public static @Nullable BlockSheetedFrame getFrameBlockFromItem(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ItemBlock) {
            Block block = ((ItemBlock)item).getBlock();
            if (block instanceof BlockSheetedFrame) {
                return (BlockSheetedFrame)block;
            }
        }

        return null;
    }

    public void onEntityCollision(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, Entity entityIn) {
        // this is only called when the "shorter" side is collided with for some reason. Colliding with a solid block does nothing
        entityIn.motionX = MathHelper.clamp(entityIn.motionX, -0.15, 0.15);
        entityIn.motionZ = MathHelper.clamp(entityIn.motionZ, -0.15, 0.15);

        entityIn.fallDistance = 0.0F;
        if (entityIn.motionY < -0.15) {
            entityIn.motionY = -0.15;
        }

        if (entityIn.isSneaking() && entityIn.motionY < 0.0) {
            entityIn.motionY = 0.0;
        }

        if (entityIn.collidedHorizontally) {
            entityIn.motionY = 0.3;
        }

    }

    @Nonnull
    public EnumPushReaction getPushReaction(@Nonnull IBlockState state) {
        return EnumPushReaction.NORMAL;
    }

    @Override @NotNull
    public AxisAlignedBB getCollisionBoundingBox(@Nonnull IBlockState blockState, @Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos) {
        return switch (this.getMetaFromState(blockState) >>> 2) {
            //x
            case (0) -> new AxisAlignedBB(0.05, 0.0, 0.0, 0.95, 1.0, 1.0);
            //z
            case (2) -> new AxisAlignedBB(0.0, 0.0, 0.05, 1.0, 1.0, 0.95);
            //NONE (all sided) or y [1] as the climbable axis would be on the top of the block
            default -> new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
        };
    }

    @Nonnull
    public BlockFaceShape getBlockFaceShape(@Nonnull IBlockAccess worldIn, @Nonnull IBlockState state, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    /*
    @SideOnly(Side.CLIENT)
    public void onModelRegister() {
        ModelLoader.setCustomStateMapper(this, new MaterialStateMapper(
                SuSyMaterialIconType.sheetedFrame, s -> s.getValue(this.variantProperty).getMaterialIconSet()));

        for (IBlockState state : this.getBlockState().getValidStates()) {
            //hopefully stop null materials from getting to register
            if (state.getValue(variantProperty) == Materials.NULL) continue;
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), this.getMetaFromState(state),
                    MaterialBlockModelLoader.registerItemModel(
                            SuSyMaterialIconType.sheetedFrame,
                            state.getValue(this.variantProperty).getMaterialIconSet()));
        }
    }
     */

    // function adapted by me [Eight/EightXOR8] from tictem's original implementation
    @SideOnly(Side.CLIENT)
    public void onModelRegister() {
        Map<IBlockState, ModelResourceLocation> map = new Object2ObjectOpenHashMap<>();
        for (IBlockState state : this.getBlockState().getValidStates()) {
            Material material = getGtMaterial(state);
            map.put(state, supersymmetry.api.util.MaterialBlockModelLoader.loadBlockModel(SuSyMaterialIconType.sheetedFrame, material.getMaterialIconSet(),
                    "axis=" + state.getValue(SHEETED_FRAME_AXIS).getName()));

            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this),
                    this.getMetaFromState(state),
                    supersymmetry.api.util.MaterialBlockModelLoader.loadItemModel(SuSyMaterialIconType.sheetedFrame, material.getMaterialIconSet()));
        }
        ModelLoader.setCustomStateMapper(this, b -> map);
    }

}
