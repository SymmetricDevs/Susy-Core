package supersymmetry.common.blocks;

import org.jspecify.annotations.NonNull;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;

public class BlockLunarConcrete extends VariantBlock<BlockLunarConcrete.LunarConcreteType> {

    public BlockLunarConcrete() {
        super(net.minecraft.block.material.Material.ROCK);
        setTranslationKey("lunar_concrete");
        setHardness(3.0f);
        setResistance(5.0f);
        setSoundType(SoundType.STONE);
        setHarvestLevel("pickaxe", 1);
        setDefaultState(getState(LunarConcreteType.LUNAR_CONCRETE_SMOOTH));
    }

    @Override
    public int damageDropped(@NotNull IBlockState state) {
        if (this.getState(state) == LunarConcreteType.LUNAR_CONCRETE_SMOOTH) {
            return LunarConcreteType.LUNAR_CONCRETE_COBBLE.ordinal();
        }
        return super.damageDropped(state);
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos,
                                  EntityPlayer player) {
        return new ItemStack(Item.getItemFromBlock(this), 1, this.getMetaFromState(state));
    }

    public enum LunarConcreteType implements IStringSerializable, IStateHarvestLevel {

        LUNAR_CONCRETE_SMOOTH("lunar_concrete_smooth", 1),
        LUNAR_CONCRETE_BRICKS("lunar_concrete_bricks", 1),
        LUNAR_CONCRETE_BRICKS_CRACKED("lunar_concrete_bricks_cracked", 1),
        LUNAR_CONCRETE_BRICKS_SMALL("lunar_concrete_bricks_small", 1),
        LUNAR_CONCRETE_BRICKS_SQUARE("lunar_concrete_bricks_square", 1),
        LUNAR_CONCRETE_CHISELED("lunar_concrete_chiseled", 1),
        LUNAR_CONCRETE_COBBLE("lunar_concrete_cobble", 1),
        LUNAR_CONCRETE_POLISHED("lunar_concrete_polished", 1),
        LUNAR_CONCRETE_TILED("lunar_concrete_tiled", 1),
        LUNAR_CONCRETE_TILED_SMALL("lunar_concrete_tiled_small", 1),
        LUNAR_CONCRETE_WINDMILL_A("lunar_concrete_windmill_a", 1),
        LUNAR_CONCRETE_WINDMILL_B("lunar_concrete_windmill_b", 1);

        private final String name;
        private final int harvestLevel;

        LunarConcreteType(String name, int harvestLevel) {
            this.name = name;
            this.harvestLevel = harvestLevel;
        }

        @NonNull
        public String getName() {
            return this.name;
        }

        public int getHarvestLevel(IBlockState state) {
            return this.harvestLevel;
        }

        public String getHarvestTool(IBlockState state) {
            return "pickaxe";
        }
    }
}
