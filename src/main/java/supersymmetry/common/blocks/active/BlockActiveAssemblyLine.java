package supersymmetry.common.blocks.active;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class BlockActiveAssemblyLine extends RedstoneActiveBlock<BlockActiveAssemblyLine.AssemblyLineType> {

    public BlockActiveAssemblyLine() {
        this(false);
    }

    protected BlockActiveAssemblyLine(boolean inverted) {
        super(Material.IRON, inverted);
        setTranslationKey("assembly_line_active");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(AssemblyLineType.ASSEMBLY_LINE));
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    protected boolean isBloomEnabled(AssemblyLineType value) {
        return true;
    }

    public enum AssemblyLineType implements IStringSerializable {

        ASSEMBLY_LINE("assembly_line");

        public final String name;

        AssemblyLineType(String name) {
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
