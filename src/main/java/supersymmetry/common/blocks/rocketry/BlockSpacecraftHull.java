package supersymmetry.common.blocks.rocketry;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.IStringSerializable;
import supersymmetry.api.blocks.VariantDirectionalCoverableBlock;

public class BlockSpacecraftHull extends VariantDirectionalCoverableBlock<BlockSpacecraftHull.HullType> {
    public BlockSpacecraftHull() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("spacecraft_hull");
        setHardness(5f);
        setResistance(15f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench",2);
        setDefaultState(getState(HullType.HULL_TYPE));
    }

    public enum HullType implements IStringSerializable {
        HULL_TYPE("hull_type");
        public String name;

        HullType(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
