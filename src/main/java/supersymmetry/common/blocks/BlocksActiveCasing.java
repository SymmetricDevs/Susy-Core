package supersymmetry.common.blocks;

import java.util.Objects;
import java.util.stream.Collectors;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import gregtech.api.block.VariantActiveBlock;
import gregtech.client.model.ActiveVariantBlockBakedModel;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

public class BlocksActiveCasing extends VariantActiveBlock<BlocksActiveCasing.ActiveBlockType> {

    protected boolean inverted = false;

    public BlocksActiveCasing() {
        super(Material.IRON);
        setTranslationKey("active_casing");
        setHardness(0.5f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(ActiveBlockType.BASIC_INTAKE_CASING));
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        super.onBlockAdded(world, pos, state);
        RedstoneActiveBlockHelper.onBlockAdded(world, pos, inverted);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos,
                                net.minecraft.block.Block block, BlockPos fromPos) {
        super.neighborChanged(state, world, pos, block, fromPos);
        RedstoneActiveBlockHelper.neighborChanged(state, world, pos, block, fromPos, inverted);
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        RedstoneActiveBlockHelper.onBlockRemoved(world, pos);
        super.breakBlock(world, pos, state);
    }

    @NotNull @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.SOLID;
    }

    @Override
    protected boolean isBloomEnabled(ActiveBlockType value) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onModelRegister() {
        Int2ObjectMap<ModelResourceLocation> models = new Int2ObjectArrayMap<>();
        for (ActiveBlockType value : VALUES) {
            int index = ((Enum<?>) value).ordinal();
            ModelResourceLocation inactiveModel = new ModelResourceLocation(
                    Objects.requireNonNull(getRegistryName()),
                    "active=false,variant=" + value.getName());
            ModelResourceLocation activeModel = new ModelResourceLocation(
                    Objects.requireNonNull(getRegistryName()),
                    "active=true,variant=" + value.getName());

            ActiveVariantBlockBakedModel model = new ActiveVariantBlockBakedModel(inactiveModel, activeModel,
                    () -> isBloomEnabled(value));
            models.put(index, model.getModelLocation());

            Item item = Item.getItemFromBlock(this);
            if (!inverted) {
                ModelLoader.setCustomModelResourceLocation(item, index, activeModel);
                ModelLoader.registerItemVariants(item, inactiveModel);
            } else {
                ModelLoader.setCustomModelResourceLocation(item, index, inactiveModel);
                ModelLoader.registerItemVariants(item, activeModel);
            }
        }
        ModelLoader.setCustomStateMapper(this,
                b -> b.getBlockState().getValidStates().stream().collect(Collectors.toMap(
                        s -> s, s -> models.get(((Enum<?>) s.getValue(VARIANT)).ordinal()))));
    }

    public enum ActiveBlockType implements IStringSerializable {

        BASIC_INTAKE_CASING("basic_intake_casing");

        public final String name;

        ActiveBlockType(String name) {
            this.name = name;
        }

        @NonNull @Override
        public String getName() {
            return this.name;
        }

        public String toString() {
            return this.getName();
        }
    }
}
