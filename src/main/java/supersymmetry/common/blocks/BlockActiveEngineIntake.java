package supersymmetry.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class BlockActiveEngineIntake extends RedstoneActiveBlock<BlockActiveEngineIntake.EngineIntakeType> {

    public BlockActiveEngineIntake() {
        this(false);
    }

    protected BlockActiveEngineIntake(boolean inverted) {
        super(Material.IRON, inverted);
        setTranslationKey("engine_intake_active");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(EngineIntakeType.ENGINE_INTAKE));
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    protected boolean isBloomEnabled(EngineIntakeType value) {
        return true;
    }

    public enum EngineIntakeType implements IStringSerializable {

        ENGINE_INTAKE("engine_intake"),
        EXTREME_ENGINE_INTAKE("extreme_engine_intake");

        public final String name;

        EngineIntakeType(String name) {
            this.name = name;
        }

        @NonNull @Override
        public String getName() {
            return this.name;
        }

        public String toString() {
            return this.getName();
        }
    }
}
