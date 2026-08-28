package supersymmetry.common.blocks;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

import gregtech.client.model.ActiveVariantBlockBakedModel;
import gregtech.common.blocks.BlockFusionCasing.CasingType;

public class BlockActiveFusionCasing extends RedstoneActiveBlock<CasingType> {

    private static final List<CasingType> BLOOM_VARIANTS = Arrays.asList(
            CasingType.FUSION_CASING,
            CasingType.FUSION_CASING_MK2,
            CasingType.FUSION_CASING_MK3);

    public BlockActiveFusionCasing() {
        this(false);
    }

    protected BlockActiveFusionCasing(boolean inverted) {
        super(Material.IRON, inverted);
        setTranslationKey("fusion_casing_active");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(CasingType.FUSION_CASING));
    }

    @Override
    protected boolean isBloomEnabled(CasingType value) {
        return true;
    }

    @NotNull @Override
    protected BlockStateContainer createBlockState() {
        this.VARIANT = PropertyEnum.create("variant", CasingType.class, BLOOM_VARIANTS);
        this.VALUES = VARIANT.getAllowedValues().toArray(new CasingType[0]);
        return new ExtendedBlockState(this,
                new IProperty[] { VARIANT, ACTIVE_DEPRECATED, POWERED },
                new IUnlistedProperty[] { ACTIVE });
    }

    @Override
    public IBlockState getState(CasingType variant) {
        return super.getState(BLOOM_VARIANTS.contains(variant) ? variant : CasingType.FUSION_CASING);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = BLOOM_VARIANTS.indexOf(state.getValue(VARIANT));
        if (state.getValue(POWERED)) meta |= 0x8;
        return meta;
    }

    @Override
    public int damageDropped(@NotNull IBlockState state) {
        return BLOOM_VARIANTS.indexOf(state.getValue(VARIANT));
    }

    @Override
    public ItemStack getItemVariant(CasingType variant, int amount) {
        return new ItemStack(this, amount, BLOOM_VARIANTS.indexOf(variant));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void onModelRegister() {
        Map<CasingType, ActiveVariantBlockBakedModel> models = new EnumMap<>(CasingType.class);
        for (CasingType value : VALUES) {
            ModelResourceLocation inactiveModel = model(false, value);
            ModelResourceLocation activeModel = model(true, value);

            ActiveVariantBlockBakedModel model = new ActiveVariantBlockBakedModel(inactiveModel, activeModel,
                    () -> isBloomEnabled(value));
            models.put(value, model);

            Item item = Item.getItemFromBlock(this);
            if (!inverted) {
                ModelLoader.setCustomModelResourceLocation(item, BLOOM_VARIANTS.indexOf(value), activeModel);
                ModelLoader.registerItemVariants(item, inactiveModel);
            } else {
                ModelLoader.setCustomModelResourceLocation(item, BLOOM_VARIANTS.indexOf(value), inactiveModel);
                ModelLoader.registerItemVariants(item, activeModel);
            }
        }
        ModelLoader.setCustomStateMapper(this, block -> {
            Map<IBlockState, ModelResourceLocation> map = new java.util.HashMap<>();
            for (IBlockState state : block.getBlockState().getValidStates()) {
                map.put(state, models.get(state.getValue(VARIANT)).getModelLocation());
            }
            return map;
        });
    }

    private ModelResourceLocation model(boolean active, CasingType variant) {
        return new ModelResourceLocation(getRegistryName(),
                "active=" + active + ",variant=" + variant.getName());
    }
}
