package supersymmetry.common.blocks.rocketry;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.IStringSerializable;
import supersymmetry.api.blocks.VariantDirectionalCoverableBlock;

public class BlockRoomPadding extends VariantDirectionalCoverableBlock<BlockRoomPadding.CoveringType> {
    public BlockRoomPadding() {
        super(Material.IRON);
        setTranslationKey("spacecraft_room_padding");
        setHardness(5f);
        setResistance(15f);
        setSoundType(SoundType.CLOTH);
        setDefaultState(getState(CoveringType.PADDING));
    }

    public enum CoveringType implements IStringSerializable {
        PADDING("padding");
        public String name;
        CoveringType(String name) {
            this.name = name;
        }
        @Override
        public String getName() {
            return name;
        }
    }
}
