package supersymmetry.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import org.jspecify.annotations.NonNull;

import gregtech.api.block.VariantBlock;

public class BlockWiring extends VariantBlock<BlockWiring.WiringType> {

    public BlockWiring() {
        super(Material.IRON);
        setTranslationKey("wiring");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(WiringType.COPPER));
    }

    @Override
    public boolean canCreatureSpawn(@NonNull IBlockState state, @NonNull IBlockAccess world, @NonNull BlockPos pos,
                                    EntityLiving.@NonNull SpawnPlacementType type) {
        return false;
    }

    public enum WiringType implements IStringSerializable {

        COPPER("copper");

        private final String name;

        WiringType(String name) {
            this.name = name;
        }

        @NonNull @Override
        public String getName() {
            return this.name;
        }
    }
}
