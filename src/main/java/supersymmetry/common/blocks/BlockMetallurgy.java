package supersymmetry.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.IStringSerializable;
import supersymmetry.api.blocks.VariantHorizontalRotatableBlock;

public class BlockMetallurgy extends VariantHorizontalRotatableBlock<BlockMetallurgy.BlockMetallurgyType> {
    public BlockMetallurgy() {
        super(Material.IRON);
        setTranslationKey("metallurgy");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(BlockMetallurgyType.ROLL));
    }

    public enum BlockMetallurgyType implements IStringSerializable {

        ROLL("roll"),
        HYDRAULIC_PRESS("hydraulic_press"),
        HYDRAULIC_PLUNGER("hydraulic_plunger"),
        STEEL_GAS_JET("steel_gas_jet");
        private String name;

        BlockMetallurgyType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
