package supersymmetry.common.blocks;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class BlocksHardened1 extends VariantBlock<BlocksHardened1.HardenedBlockType> {

    public BlocksHardened1() {
        super(Material.ROCK);
        this.setHardness(50.0F);
        this.setResistance(25.0F);
        this.setSoundType(SoundType.STONE);
        this.setTranslationKey("hardened_blocks1");
    }

    public int quantityDropped(Random random)
    {
        return 1;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        SusyStoneVariantBlock bricksBlock = SuSyBlocks.SUSY_STONE_BLOCKS.get(SusyStoneVariantBlock.StoneVariant.BRICKS);
        return Item.getItemFromBlock(bricksBlock);
    }

    @Override
    public int damageDropped(IBlockState state) {
        return 9;
    }

    public static enum HardenedBlockType implements IStringSerializable, IStateHarvestLevel {
        INDUSTRIAL_CONCRETE_HARDENED("industrial_concrete_hardened", 5);

        private final String name;
        private final int harvestLevel;

        private HardenedBlockType(String name, int harvestLevel) {
            this.name = name;
            this.harvestLevel = harvestLevel;
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

