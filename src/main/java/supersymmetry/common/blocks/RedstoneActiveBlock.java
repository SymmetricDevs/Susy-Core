package supersymmetry.common.blocks;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import org.jetbrains.annotations.NotNull;

import gregtech.api.block.VariantActiveBlock;
import gregtech.api.util.Mods;
import gregtech.client.model.ActiveVariantBlockBakedModel;

import team.chisel.ctm.client.state.CTMExtendedState;

/**
 * A base class for decorative active blocks that respond to redstone, following the
 * same pattern as GregTech's {@code BlockLamp}.
 * <p>
 * The block has a listed {@code powered} blockstate property toggled by redstone.
 * The visual "active" state (which drives bloom rendering) is derived as
 * {@code powered == inverted}:
 * <ul>
 *   <li>Normal (inverted=false): active when NOT powered (bloom on by default, off when powered)</li>
 *   <li>Inverted (inverted=true): active when powered (bloom off by default, on when powered)</li>
 * </ul>
 * <p>
 * The unlisted {@link VariantActiveBlock#ACTIVE} property is set from the derived state
 * in {@link #getExtendedState}, so the {@link gregtech.client.model.ActiveVariantBlockBakedModel}
 * picks the correct model to render.
 */
public abstract class RedstoneActiveBlock<T extends Enum<T> & IStringSerializable>
        extends VariantActiveBlock<T> {

    public static final PropertyBool POWERED = PropertyBool.create("powered");

    protected final boolean inverted;

    protected RedstoneActiveBlock(Material material, boolean inverted) {
        super(material);
        this.inverted = inverted;
    }

    /**
     * Returns whether the visual effect (bloom) should be active for the given state.
     * Logic: {@code powered == inverted}
     */
    public boolean isEffectActive(IBlockState state) {
        return state.getValue(POWERED) == inverted;
    }

    // --- Blockstate ---

    @SuppressWarnings("unchecked")
    private Class<? extends Enum<?>> resolveEnumClass() {
        Class<?> clazz = getClass();
        while (clazz != null) {
            Type genericSuper = clazz.getGenericSuperclass();
            if (genericSuper instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) genericSuper;
                Type[] args = pt.getActualTypeArguments();
                if (args.length > 0 && args[0] instanceof Class
                        && VariantActiveBlock.class.isAssignableFrom((Class<?>) pt.getRawType())) {
                    return (Class<? extends Enum<?>>) args[0];
                }
            }
            clazz = clazz.getSuperclass();
        }
        throw new IllegalStateException("Could not resolve enum class for " + getClass());
    }

    @NotNull
    @Override
    protected BlockStateContainer createBlockState() {
        Class<? extends Enum<?>> enumClass = resolveEnumClass();
        createVariantProperty(enumClass);
        return new ExtendedBlockState(this,
                new IProperty[]{ VARIANT, ACTIVE_DEPRECATED, POWERED },
                new IUnlistedProperty[]{ ACTIVE });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void createVariantProperty(Class<? extends Enum<?>> enumClass) {
        PropertyEnum prop = PropertyEnum.create("variant", (Class) enumClass);
        this.VARIANT = prop;
        this.VALUES = (T[]) prop.getAllowedValues().toArray(new Enum[0]);
    }

    @Override
    public IBlockState getState(T variant) {
        return super.getState(variant).withProperty(POWERED, false);
    }

    @NotNull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        // bit 3 = powered, bits 0-2 = variant
        return super.getStateFromMeta(meta & 0x7)
                .withProperty(POWERED, (meta & 0x8) != 0);
    }

    @Override
    public int getMetaFromState(@NotNull IBlockState state) {
        int meta = ((Enum<?>) state.getValue(VARIANT)).ordinal();
        if (state.getValue(POWERED)) meta |= 0x8;
        return meta;
    }

    @Override
    public int damageDropped(@NotNull IBlockState state) {
        return ((Enum<?>) state.getValue(VARIANT)).ordinal();
    }

    // --- Redstone (same pattern as GT BlockLamp) ---

    @Override
    public void onBlockAdded(World world, @NotNull BlockPos pos, @NotNull IBlockState state) {
        if (!world.isRemote) {
            boolean powered = state.getValue(POWERED);
            if (powered != world.isBlockPowered(pos)) {
                world.setBlockState(pos, state.withProperty(POWERED, !powered), 2);
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(@NotNull IBlockState state, @NotNull World world, @NotNull BlockPos pos,
                                @NotNull Block block, @NotNull BlockPos fromPos) {
        if (!world.isRemote) {
            if (state.getValue(POWERED)) {
                if (!world.isBlockPowered(pos)) {
                    // 4-tick delay before turning off (vanilla redstone lamp behavior)
                    world.scheduleUpdate(pos, this, 4);
                }
            } else if (world.isBlockPowered(pos)) {
                world.setBlockState(pos, state.withProperty(POWERED, true), 2);
            }
        }
    }

    @Override
    public void updateTick(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state,
                           @NotNull Random rand) {
        if (!world.isRemote && state.getValue(POWERED) && !world.isBlockPowered(pos)) {
            world.setBlockState(pos, state.withProperty(POWERED, false), 2);
        }
    }

    // --- Rendering ---

    @NotNull
    @Override
    public IExtendedBlockState getExtendedState(@NotNull IBlockState state, @NotNull IBlockAccess world,
                                                @NotNull BlockPos pos) {
        IExtendedBlockState ext = ((IExtendedBlockState) state)
                .withProperty(ACTIVE, isEffectActive(state));

        if (Mods.CTM.isModLoaded()) {
            return new CTMExtendedState(ext, world, pos);
        }
        return ext;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onModelRegister() {
        Int2ObjectMap<ModelResourceLocation> models = new Int2ObjectArrayMap<>();
        for (T value : VALUES) {
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
}
