package supersymmetry.common.blocks;

import gregicality.multiblocks.common.block.blocks.BlockUniqueCasing;
import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantActiveBlock;
import gregtech.client.utils.BloomEffectUtil;
import gregtech.common.ConfigHolder;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.IStringSerializable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class BlockElectrodeAssembly extends VariantActiveBlock<BlockElectrodeAssembly.ElectrodeAssemblyType> {

    public BlockElectrodeAssembly() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("electrode_assembly");
        setHardness(3.0f);
        setResistance(3.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 1);
        setDefaultState(getState(ElectrodeAssemblyType.CARBON));
    }

    @NotNull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.SOLID;
    }

    @Override
    public boolean canRenderInLayer(@Nonnull IBlockState state, @Nonnull BlockRenderLayer layer) {
        ElectrodeAssemblyType type = getState(state);
        if (type == ElectrodeAssemblyType.CARBON) {
            if (layer == BlockRenderLayer.SOLID) return true;
        } else if (layer == BlockRenderLayer.CUTOUT) return true;

        if (isBloomEnabled(type)) return layer == BloomEffectUtil.getRealBloomLayer();
        return layer == BlockRenderLayer.CUTOUT;
    }

    protected boolean isBloomEnabled(ElectrodeAssemblyType value) {
        if (ConfigHolder.client.coilsActiveEmissiveTextures && value == ElectrodeAssemblyType.CARBON) {
            return true;
        }
        return false;
    }

    public static enum ElectrodeAssemblyType implements IStringSerializable, IStateHarvestLevel {
        CARBON("carbon", 1);

        private final String name;
        private final int harvestLevel;

        private ElectrodeAssemblyType(String name, int harvestLevel) {
            this.name = name;
            this.harvestLevel = harvestLevel;
        }

        @Nonnull
        public String getName() {
            return this.name;
        }

        public int getHarvestLevel(IBlockState state) {
            return this.harvestLevel;
        }

        public String getHarvestTool(IBlockState state) {
            return "wrench";
        }
    }
}
