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

public class BlockVacuumPump extends VariantBlock<BlockVacuumPump.VacuumPumpType> {

    public BlockVacuumPump() {
        super(Material.IRON);
        setTranslationKey("vacuum_pump");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(VacuumPumpType.STAINLESS_STEEL));
    }

    @Override
    public boolean canCreatureSpawn(@NonNull IBlockState state, @NonNull IBlockAccess world, @NonNull BlockPos pos,
                                    EntityLiving.@NonNull SpawnPlacementType type) {
        return false;
    }

    public enum VacuumPumpType implements IStringSerializable {

        STAINLESS_STEEL("stainless_steel");

        private final String name;

        VacuumPumpType(String name) {
            this.name = name;
        }

        @NonNull @Override
        public String getName() {
            return this.name;
        }
    }
}
