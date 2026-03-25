package supersymmetry.common.blocks;

import javax.annotation.Nonnull;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;

public class BlockResource extends VariantBlock<BlockResource.ResourceBlockType> {

    public BlockResource() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("resource_block");
        setHardness(3.0f);
        setResistance(3.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("pickaxe", 1);
        setDefaultState(getState(ResourceBlockType.BAUXITE));
    }

    public enum ResourceBlockType implements IStringSerializable, IStateHarvestLevel {

        BAUXITE("bauxite", 1),
        CALICHE("caliche", 1),
        NON_MARINE_EVAPORITE("non_marine_evaporite", 1),
        HALIDE_EVAPORITE("halide_evaporite", 1),
        SULFATE_EVAPORITE("sulfate_evaporite", 1),
        CARBONATE_EVAPORITE("carbonate_evaporite", 1),
        MONAZITE_ALLUVIAL("monazite_alluvial", 1),
        BASTNASITE_ALLUVIAL("bastnasite_alluvial", 1),
        EUXENITE_ALLUVIAL("euxenite_alluvial", 1),
        XENOTIME_ALLUVIAL("xenotime_alluvial", 1),
        PLATINUM_PLACER("platinum_placer", 1),
        GOLD_ALLUVIAL("gold_alluvial", 1),
        PHOSPHORITE("phosphorite", 1),
        POTASH("potash", 1),
        SULFUR("sulfur", 1),
        COAL("coal", 0);

        private final String name;
        private final int harvestLevel;

        ResourceBlockType(String name, int harvestLevel) {
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
            return "pickaxe";
        }
    }
}
