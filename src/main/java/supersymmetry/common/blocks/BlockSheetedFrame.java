package supersymmetry.common.blocks;

import gregtech.api.GregTechAPI;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTUtility;
import gregtech.common.blocks.properties.PropertyMaterial;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
import supersymmetry.api.unification.material.info.SuSyMaterialIconType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class BlockSheetedFrame extends Block {

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
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
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
    public IBlockState withRotation(IBlockState state, Rotation rot)
    {
        switch (rot)
        {
            case COUNTERCLOCKWISE_90:
            case CLOCKWISE_90:

                switch (state.getValue(SHEETED_FRAME_AXIS))
                {
                    case X:
                        return state.withProperty(SHEETED_FRAME_AXIS, BlockSheetedFrame.FrameEnumAxis.Z);
                    case Z:
                        return state.withProperty(SHEETED_FRAME_AXIS, BlockSheetedFrame.FrameEnumAxis.X);
                    default:
                        return state;
                }

            default:
                return state;
        }
    }

    public static enum FrameEnumAxis implements IStringSerializable
    {
        X("x"),
        Y("y"),
        Z("z"),
        NONE("none");

        private final String name;

        private FrameEnumAxis(String name)
        {
            this.name = name;
        }

        public String toString()
        {
            return this.name;
        }

        public static FrameEnumAxis fromFacingAxis(EnumFacing.Axis axis)
        {
            switch (axis)
            {
                case X:
                    return X;
                case Y:
                    return Y;
                case Z:
                    return Z;
                default:
                    return NONE;
            }
        }

        public String getName()
        {
            return this.name;
        }
    }

    @Override @Deprecated
    public boolean isOpaqueCube(IBlockState state) { return false; }


    @Override @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer()
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

    public static ItemStack getItem(IBlockState blockState) {
        return GTUtility.toItem(blockState);
    }

    public ItemStack getItem(Material material) {
        return getItem(this.getDefaultState().withProperty(this.variantProperty, material).withProperty(SHEETED_FRAME_AXIS, FrameEnumAxis.Y));
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

    /*
    public boolean replaceWithFramedPipe(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, ItemStack stackInHand, EnumFacing facing) {
        BlockPipe<?, ?, ?> blockPipe = (BlockPipe)((ItemBlockPipe)stackInHand.getItem()).getBlock();
        if (((IPipeType)blockPipe.getItemPipeType(stackInHand)).getThickness() < 1.0F) {
            ItemBlock itemBlock = (ItemBlock)stackInHand.getItem();
            IBlockState pipeState = blockPipe.getDefaultState();
            itemBlock.placeBlockAt(stackInHand, playerIn, worldIn, pos, facing, 0.0F, 0.0F, 0.0F, pipeState);
            IPipeTile<?, ?> pipeTile = blockPipe.getPipeTileEntity(worldIn, pos);
            if (pipeTile instanceof TileEntityPipeBase) {
                ((TileEntityPipeBase)pipeTile).setFrameMaterial(this.getGtMaterial(this.getMetaFromState(state)));
                SoundType type = blockPipe.getSoundType(state, worldIn, pos, playerIn);
                worldIn.playSound(playerIn, pos, type.getPlaceSound(), SoundCategory.BLOCKS, (type.getVolume() + 1.0F) / 2.0F, type.getPitch() * 0.8F);
                if (!playerIn.capabilities.isCreativeMode) {
                    stackInHand.shrink(1);
                }

                return true;
            } else {
                GTLog.logger.error("Pipe was not placed!");
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean removeFrame(World world, BlockPos pos, EntityPlayer player, ItemStack stack) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityPipeBase && ((IPipeTile)te).getFrameMaterial() != null) {
            TileEntityPipeBase<?, ?> pipeTile = (TileEntityPipeBase)te;
            Material frameMaterial = pipeTile.getFrameMaterial();
            pipeTile.setFrameMaterial((Material)null);
            Block.spawnAsEntity(world, pos, this.getItem(frameMaterial));
            ToolHelper.damageItem(stack, player);
            ToolHelper.playToolSound(stack, player);
            return true;
        } else {
            return false;
        }
    }

    public boolean onBlockActivated(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, EntityPlayer playerIn, @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stackInHand = playerIn.getHeldItem(hand);
        if (stackInHand.isEmpty()) {
            return false;
        } else if (stackInHand.getItem() instanceof ItemBlockPipe) {
            return this.replaceWithFramedPipe(worldIn, pos, state, playerIn, stackInHand, facing);
        } else if (stackInHand.getItem().getToolClasses(stackInHand).contains("crowbar")) {
            return this.removeFrame(worldIn, pos, playerIn, stackInHand);
        } else if (!(stackInHand.getItem() instanceof FrameItemBlock)) {
            return false;
        } else {
            BlockPos.PooledMutableBlockPos blockPos = BlockPos.PooledMutableBlockPos.retain();
            blockPos.setPos(pos);

            for(int i = 0; i < 32; ++i) {
                if (worldIn.getBlockState(blockPos).getBlock() instanceof BlockFrame) {
                    blockPos.move(EnumFacing.UP);
                } else {
                    TileEntity te = worldIn.getTileEntity(blockPos);
                    if (!(te instanceof IPipeTile) || ((IPipeTile)te).getFrameMaterial() == null) {
                        if (this.canPlaceBlockAt(worldIn, blockPos)) {
                            worldIn.setBlockState(blockPos, ((FrameItemBlock)stackInHand.getItem()).getBlockState(stackInHand));
                            SoundType type = this.getSoundType(stackInHand);
                            worldIn.playSound((EntityPlayer)null, pos, type.getPlaceSound(), SoundCategory.BLOCKS, (type.getVolume() + 1.0F) / 2.0F, type.getPitch() * 0.8F);
                            if (!playerIn.capabilities.isCreativeMode) {
                                stackInHand.shrink(1);
                            }

                            blockPos.release();
                            return true;
                        } else if (te instanceof TileEntityPipeBase && ((TileEntityPipeBase)te).getFrameMaterial() == null) {
                            Material material = ((BlockFrame)((FrameItemBlock)stackInHand.getItem()).getBlock()).getGtMaterial(stackInHand.getMetadata());
                            ((TileEntityPipeBase)te).setFrameMaterial(material);
                            SoundType type = this.getSoundType(stackInHand);
                            worldIn.playSound((EntityPlayer)null, pos, type.getPlaceSound(), SoundCategory.BLOCKS, (type.getVolume() + 1.0F) / 2.0F, type.getPitch() * 0.8F);
                            if (!playerIn.capabilities.isCreativeMode) {
                                stackInHand.shrink(1);
                            }

                            blockPos.release();
                            return true;
                        } else {
                            blockPos.release();
                            return false;
                        }
                    }

                    blockPos.move(EnumFacing.UP);
                }
            }

            blockPos.release();
            return false;
        }
    }
     */

    public void onEntityCollision(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, Entity entityIn) {
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

    public AxisAlignedBB getCollisionBoundingBox(@Nonnull IBlockState blockState, @Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos) {
        AxisAlignedBB boundingBox = switch (this.getMetaFromState(blockState) >>> 2) {
            //x
            case (0) -> new AxisAlignedBB(0.05, 0.0, 0.00, 0.95, 1.0, 1.00);
            //z
            case (2) -> new AxisAlignedBB(0.00, 0.0, 0.05, 1.0, 1.0, 0.95);
            //NONE (all sided) or y [1] as the climbable axis would be on the top of the block
            default -> new AxisAlignedBB(0.00, 0.0, 0.00, 1.0, 1.0, 1.0);
        };
        return boundingBox;
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

    //function adapted by me from tictem's original implementation
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
