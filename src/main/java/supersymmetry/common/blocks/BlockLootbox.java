package supersymmetry.common.blocks;

import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nonnull;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import gregtech.api.block.VariantBlock;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class BlockLootbox extends VariantBlock<BlockLootbox.LootboxType> {

    public BlockLootbox() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("lootbox");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setDefaultState(getState(LootboxType.BASIC_FOOD_I));
    }

    public enum LootboxType implements IStringSerializable {

        BASIC_FOOD_I("basic_food_i"),
        PREMIUM_FOOD_I("premium_food_i"),
        GOLDEN_FOOD_I("golden_food_i"),
        BASIC_FOOD_II("basic_food_ii"),
        SUPREME_FOOD_II("supreme_food_ii"),
        SAVINGS_FOOD_III("savings_food_iii"),
        BASIC_WEAPONS_I("basic_weapons_i"),
        GOLDEN_WEAPONS_I("golden_weapons_i"),
        ELITE_WEAPONS_IV("elite_weapons_iv"),
        VALUE_MACHINES_III("value_machines_iii"),
        BASIC_STYLE_II("basic_style_ii"),
        TRENDY_STYLE_II("trendy_style_ii"),
        SAVINGS_STYLE_III("savings_style_iii");

        private String name;
        public List<LootEntry> loot = new ObjectArrayList<>();

        LootboxType(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        public void addLoot(ItemStack stack, int weight) {
            this.loot.add(new LootEntry(stack, weight));
        }
    }

    static class LootEntry extends WeightedRandom.Item {

        @Nonnull
        public final ItemStack loot;

        public LootEntry(@Nonnull ItemStack loot, int weight) {
            super(weight);
            this.loot = loot;
        }

        @Nonnull
        public ItemStack getStack(Random rand, int fortune) {
            return loot.copy();
        }
    }

    @Override
    public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn) {
        ItemStack held = playerIn.getHeldItemMainhand();
        Set<String> toolClasses = held.getItem().getToolClasses(held);
        if (!toolClasses.contains("hammer")) {
            return;
        }

        worldIn.playSound(null, pos, SoundEvents.BLOCK_ANVIL_DESTROY, SoundCategory.BLOCKS, 1f, 1f);
        // Choose loot
        IBlockState state = worldIn.getBlockState(pos);
        LootboxType type = this.getState(state);
        ItemStack stack = WeightedRandom.getRandomItem(worldIn.rand, type.loot).loot;
        // Spawn item
        if (!worldIn.isRemote) {
            worldIn.spawnEntity(new EntityItem(worldIn, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack));
        }
        // Remove block
        worldIn.setBlockToAir(pos);
        // Explosion particle effect
        worldIn.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, pos.getX() + 0.5, pos.getY() + 0.5,
                pos.getZ() + 0.5, 0, 0, 0);
    }
}
