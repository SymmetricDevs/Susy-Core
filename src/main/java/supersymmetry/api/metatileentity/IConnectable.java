package supersymmetry.api.metatileentity;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

import static gregtech.api.util.GTUtility.gregtechId;

public interface IConnectable {

    Function<@Nullable IMultiblockPart, IBlockState> DUMMY = part -> null;

    // Should ONLY be used for gregtech multis.
    // Just extend this interface and override what you need for SuSy multis.
    Object2ObjectMap<ResourceLocation, Function<@Nullable IMultiblockPart, IBlockState>> registry = new Object2ObjectOpenHashMap<>() {{

        put(gregtechId("electric_blast_furnace"), part -> MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.INVAR_HEATPROOF));

    }};

    @SuppressWarnings("unused")
    static void register(@NotNull ResourceLocation mteId, @NotNull Function<@Nullable IMultiblockPart, @Nullable IBlockState> handler) {
        registry.put(mteId, handler);
    }

    /// Null for self, same as [MultiblockControllerBase#getBaseTexture(IMultiblockPart)]
    ///
    /// @see supersymmetry.mixins.ctm.BlockMachineMixin
    @Nullable
    default IBlockState getVisualState(@Nullable IMultiblockPart part) {
        ResourceLocation mteId = ((MetaTileEntity) this).metaTileEntityId;

        return registry.getOrDefault(mteId, DUMMY).apply(part);
    }

    /// Supplies the extra [BlockRenderLayer]s the MTE can be rendered in.
    ///
    /// @see supersymmetry.mixins.ctm.MetaTileEntityMixin
    default boolean shouldRenderInLayer(@NotNull BlockRenderLayer layer) {
        IBlockState visualState = getVisualState(null);
        if (visualState == null) return false;
        return visualState.getBlock().canRenderInLayer(visualState, layer);
    }
}
