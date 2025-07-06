package supersymmetry.api.blocks;

import gregtech.api.GregTechAPI;
import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.util.LocalizationUtils;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public class VariantBlockFalling<T extends Enum<T> & IStringSerializable> extends BlockFalling {
    protected PropertyEnum<T> VARIANT;
    protected T[] VALUES;

    public VariantBlockFalling(Material materialIn) {
        super(materialIn);
        if (this.VALUES.length > 0 && this.VALUES[0] instanceof IStateHarvestLevel) {
            for(T t : this.VALUES) {
                IStateHarvestLevel stateHarvestLevel = (IStateHarvestLevel)t;
                IBlockState state = this.getState(t);
                this.setHarvestLevel(stateHarvestLevel.getHarvestTool(state), stateHarvestLevel.getHarvestLevel(state), state);
            }
        }

        this.setCreativeTab(GregTechAPI.TAB_GREGTECH_DECORATIONS);
        this.setDefaultState(this.blockState.getBaseState().withProperty(this.VARIANT, this.VALUES[0]));
    }

    public void getSubBlocks(@NotNull CreativeTabs tab, @NotNull NonNullList<ItemStack> list) {
        for(T variant : this.VALUES) {
            list.add(this.getItemVariant(variant));
        }

    }

    public IBlockState getState(T variant) {
        return this.getDefaultState().withProperty(this.VARIANT, variant);
    }

    public T getState(IBlockState blockState) {
        return (T)(blockState.getValue(this.VARIANT));
    }

    public T getState(ItemStack stack) {
        return (T)this.getState(this.getStateFromMeta(stack.getItemDamage()));
    }

    public ItemStack getItemVariant(T variant) {
        return this.getItemVariant(variant, 1);
    }

    public ItemStack getItemVariant(T variant, int amount) {
        return new ItemStack(this, amount, variant.ordinal());
    }

    protected @NotNull BlockStateContainer createBlockState() {
        Class<T> enumClass = getActualTypeParameter(this.getClass(), VariantBlockFalling.class);
        this.VARIANT = PropertyEnum.create("variant", enumClass);
        this.VALUES = (T[])(enumClass.getEnumConstants());
        return new BlockStateContainer(this, new IProperty[]{this.VARIANT});
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(@NotNull ItemStack stack, @Nullable World player, @NotNull List<String> tooltip, @NotNull ITooltipFlag advanced) {
        String unlocalizedVariantTooltip = this.getTranslationKey() + ".tooltip";
        if (I18n.hasKey(unlocalizedVariantTooltip)) {
            Collections.addAll(tooltip, LocalizationUtils.formatLines(unlocalizedVariantTooltip, new Object[0]));
        }

        String unlocalizedTooltip = stack.getTranslationKey() + ".tooltip";
        if (I18n.hasKey(unlocalizedTooltip)) {
            Collections.addAll(tooltip, LocalizationUtils.formatLines(unlocalizedTooltip, new Object[0]));
        }

    }

    public int damageDropped(@NotNull IBlockState state) {
        return this.getMetaFromState(state);
    }

    public @NotNull IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(this.VARIANT, this.VALUES[meta % this.VALUES.length]);
    }

    public int getMetaFromState(IBlockState state) {
        return ((Enum)state.getValue(this.VARIANT)).ordinal();
    }

    protected static <T, R> Class<T> getActualTypeParameter(Class<? extends R> thisClass, Class<R> declaringClass) {
        Type type = thisClass.getGenericSuperclass();

        while(!(type instanceof ParameterizedType) || ((ParameterizedType)type).getRawType() != declaringClass) {
            if (type instanceof ParameterizedType) {
                type = ((Class)((ParameterizedType)type).getRawType()).getGenericSuperclass();
            } else {
                type = ((Class)type).getGenericSuperclass();
            }
        }

        return (Class)((ParameterizedType)type).getActualTypeArguments()[0];
    }
}
