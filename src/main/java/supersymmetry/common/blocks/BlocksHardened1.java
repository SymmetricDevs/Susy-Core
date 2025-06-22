package supersymmetry.common.blocks;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;
import java.util.function.Supplier;

public class BlocksHardened1 extends VariantBlock<BlocksHardened1.HardenedBlockType> {

    public BlocksHardened1() {
        super(Material.ROCK);
        this.setHardness(75.0F);
        this.setResistance(25.0F);
        this.setSoundType(SoundType.STONE);
        this.setTranslationKey("hardened_blocks1");
    }

    public int quantityDropped(Random random)
    {
        return 1;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        Random rand = world instanceof World ? ((World)world).rand : RANDOM;

        HardenedBlockType type = getState(state);
        int count = quantityDropped(state, fortune, rand);
        for (int i = 0; i < count; i++)
        {
            drops.add(type.droppedItem.get()); // Damage dropped is hardcoded here since `damageDropped` is used elsewhere
        }
    }

    public enum HardenedBlockType implements IStringSerializable, IStateHarvestLevel {
        INDUSTRIAL_CONCRETE_HARDENED("industrial_concrete_hardened", 4,
                () -> new ItemStack(Item.getItemFromBlock(SuSyBlocks.SUSY_STONE_BLOCKS.get(SusyStoneVariantBlock.StoneVariant.BRICKS)), 1, 9)),
        MILITARY_CONCRETE_COBBLESTONE_HARDENED("military_concrete_cobblestone_hardened", 4,
                () -> new ItemStack(Item.getItemFromBlock(SuSyBlocks.SUSY_STONE_BLOCKS.get(SusyStoneVariantBlock.StoneVariant.COBBLE)), 1, 10)),
        MILITARY_CONCRETE_HARDENED("military_concrete_hardened", 4,
                                             () -> new ItemStack(Item.getItemFromBlock(SuSyBlocks.SUSY_STONE_BLOCKS.get(SusyStoneVariantBlock.StoneVariant.SMOOTH)), 1, 10));


        private final String name;
        private final int harvestLevel;
        private final Supplier<ItemStack> droppedItem;

        HardenedBlockType(String name, int harvestLevel, Supplier<ItemStack> droppedItem) {
            this.name = name;
            this.harvestLevel = harvestLevel;
            this.droppedItem = droppedItem;
        }

        @Override
        public int getHarvestLevel(IBlockState iBlockState) {
            return this.harvestLevel;
        }

        @Override
        public String getName() {
            return this.name;
        }
    }
}

