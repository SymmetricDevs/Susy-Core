package supersymmetry.common.blocks;

import gregtech.api.block.IStateHarvestLevel;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;
import supersymmetry.api.blocks.VariantFullDoorBlock;

public class BlockAirlockDoor extends VariantFullDoorBlock<BlockAirlockDoor.AirlockType> {
    public BlockAirlockDoor() {
        setTranslationKey("airlock_door");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        disableStats();
    }

    public enum AirlockType implements IStringSerializable, IStateHarvestLevel {
        AIRLOCK("basic", 2);


        private final String name;
        private final int harvestLevel;

        private AirlockType(String name, int harvestLevel) {
            this.name = name;
            this.harvestLevel = harvestLevel;
        }

        @Override
        public int getHarvestLevel(IBlockState state) {
            return harvestLevel;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
