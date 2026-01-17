package supersymmetry.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import org.jetbrains.annotations.NotNull;

import supersymmetry.api.blocks.VariantAxialRotatableBlock;

public class BlockGirthGearTooth extends VariantAxialRotatableBlock<BlockGirthGearTooth.Type> {

    public BlockGirthGearTooth() {
        super(Material.IRON);
        setTranslationKey("girth_gear_tooth");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(Type.STEEL));
    }

    @Override
    public boolean canCreatureSpawn(
                                    @NotNull IBlockState state,
                                    @NotNull IBlockAccess world,
                                    @NotNull BlockPos pos,
                                    @NotNull SpawnPlacementType type) {
        return false;
    }

    public enum Type implements IStringSerializable {

        STEEL("steel"),
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
