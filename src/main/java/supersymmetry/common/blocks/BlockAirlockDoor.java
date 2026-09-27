package supersymmetry.common.blocks;

import java.util.function.Supplier;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

import gregtech.api.block.IStateHarvestLevel;
import supersymmetry.api.blocks.VariantFullDoorBlock;

public class BlockAirlockDoor extends VariantFullDoorBlock<BlockAirlockDoor.AirlockType> {

    private final Supplier<ItemStack> itemSupplier;

    public BlockAirlockDoor(Supplier<ItemStack> itemSupplier) {
        this.itemSupplier = itemSupplier;

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
