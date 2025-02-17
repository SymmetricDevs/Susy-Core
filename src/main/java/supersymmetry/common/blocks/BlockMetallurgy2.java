package supersymmetry.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.IStringSerializable;
import supersymmetry.api.blocks.VariantDirectionalRotatableBlock;

public class BlockMetallurgy2 extends VariantDirectionalRotatableBlock<BlockMetallurgy2.BlockMetallurgy2Type> {
    public BlockMetallurgy2() {
        super(Material.IRON);
        setTranslationKey("metallurgy_2");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(BlockMetallurgy2Type.STEEL_GAS_JET));
    }

    public enum BlockMetallurgy2Type implements IStringSerializable {
        STEEL_GAS_JET("steel_gas_jet");
        private String name;

        BlockMetallurgy2Type(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
