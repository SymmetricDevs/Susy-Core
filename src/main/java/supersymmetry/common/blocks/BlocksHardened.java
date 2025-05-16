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

public class BlocksHardened extends VariantBlock<BlocksHardened.HardenedBlockType> {

    public BlocksHardened() {
        super(Material.ROCK);
        this.setHardness(25.0F);
        this.setResistance(25.0F);
        this.setSoundType(SoundType.STONE);
        this.setTranslationKey("hardened_blocks");
    }

    public int quantityDropped(Random random)
    {
        return 0;
    }

    public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
        return Items.AIR;
    }

    @Override
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        return false;
    }

    public static enum HardenedBlockType implements IStringSerializable, IStateHarvestLevel {
        LAIR10("lair10", 3),
        KRYP8("kryp8", 3),
        KRYP7("kryp7", 3),
        LAIR11("lair11", 3),
        LAIR7("lair7", 3);

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

