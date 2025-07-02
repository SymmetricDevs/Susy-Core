package supersymmetry.common.blocks;

import gregtech.api.block.VariantBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.NotNull;

public class BlockGrinderCasing extends VariantBlock<BlockGrinderCasing.Type> {

    public BlockGrinderCasing() {
        super(Material.IRON);
        setTranslationKey("grinder_casing");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(Type.ABRASION_RESISTANT_CASING));
    }

    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos, @NotNull EntityLiving.@NotNull SpawnPlacementType type) {
        return false;
    }

    public enum Type implements IStringSerializable {

        ABRASION_RESISTANT_CASING("abrasion_resistant_casing"),
        HYDRAULIC_MECHANICAL_GEARBOX("hydraulic_mechanical_gearbox"),
        ;

        private final String name;

        Type(String name) {
            this.name = name;
        }

        @NotNull
        public String getName() {
            return name;
        }
    }
}
