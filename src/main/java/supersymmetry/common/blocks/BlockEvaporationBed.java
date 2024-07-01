package supersymmetry.common.blocks;

import gregicality.multiblocks.common.block.blocks.BlockUniqueCasing;
import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantActiveBlock;
import gregtech.api.block.VariantBlock;
import gregtech.client.utils.BloomEffectUtil;
import gregtech.common.ConfigHolder;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.IStringSerializable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class BlockEvaporationBed extends VariantActiveBlock<BlockEvaporationBed.EvaporationBedType> {

    public BlockEvaporationBed() {
        super(net.minecraft.block.material.Material.CLAY);
        setTranslationKey("evaporation_bed");
        setHardness(0.5f);
        setResistance(0.5f);
        setSoundType(SoundType.GROUND);
        setHarvestLevel("shovel", 0);
        setDefaultState(getState(EvaporationBedType.DIRT));
    }

    @NotNull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.SOLID;
    }

    public static enum EvaporationBedType implements IStringSerializable, IStateHarvestLevel {
        DIRT("dirt", 0);

        private final String name;
        private final int harvestLevel;

        private EvaporationBedType(String name, int harvestLevel) {
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
            return "shovel";
        }
    }
}
