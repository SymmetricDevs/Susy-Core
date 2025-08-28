package supersymmetry.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.blocks.VariantAxialRotatableBlock;

public class BlockMetallurgyRoll extends VariantAxialRotatableBlock<BlockMetallurgyRoll.BlockMetallurgyRollType> {
    public BlockMetallurgyRoll() {
        super(Material.IRON);
        setTranslationKey("metallurgy_roll");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(BlockMetallurgyRollType.ROLL));
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    public enum BlockMetallurgyRollType implements IStringSerializable {

        ROLL("roll");
        private String name;

        BlockMetallurgyRollType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
