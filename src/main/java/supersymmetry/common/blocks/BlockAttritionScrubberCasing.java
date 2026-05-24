package supersymmetry.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import org.jetbrains.annotations.NotNull;

import gregtech.api.block.VariantBlock;

public class BlockAttritionScrubberCasing extends VariantBlock<BlockAttritionScrubberCasing.Type> {

    public BlockAttritionScrubberCasing() {
        super(Material.IRON);
        setTranslationKey("attrition_scrubber_casing");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 1);
        setDefaultState(getState(Type.HYDROSTATIC_CASING));
    }

    @Override
    public int getHarvestLevel(IBlockState state) {
        return state.getValue(VARIANT) == Type.HYDROSTATIC_CASING ? 2 : 1;
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

        HYDROSTATIC_CASING("hydrostatic_casing"),
        ALUMINIUM_GEARBOX("aluminium_gearbox"),
        ALUMINIUM_SHAFT("aluminium_shaft"),
        ALUMINIUM_BLADE("aluminium_blade"),
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
