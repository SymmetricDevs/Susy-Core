package supersymmetry.common.blocks.vehicles;

import gregtech.api.block.VariantBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class BlockKirovInterior extends VariantBlock<BlockKirovInterior.KirovInteriorType> {

    public BlockKirovInterior() {
        this(Material.IRON);
    }

    public BlockKirovInterior(Material materialIn) {
        super(materialIn);
        this.setResistance(1200.0f);
        this.setSoundType(SoundType.METAL);
        this.setBlockUnbreakable();
        setTranslationKey("kirov_block");
        this.setDefaultState(getState(KirovInteriorType.HULL));
    }

    @NotNull
    @Override
    @SuppressWarnings("deprecation")
    public EnumPushReaction getPushReaction(@NotNull IBlockState state) {
        return EnumPushReaction.IGNORE;
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    public boolean canSilkHarvest(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state,
                                        @NotNull EntityPlayer player) {
        return false;
    }


    public enum KirovInteriorType implements IStringSerializable {

        HULL("hull"),
        STRUT("strut");

        private final String name;

        KirovInteriorType(String name) {
            this.name = name;
        }

        @NonNull
        public String getName() {
            return this.name;
        }
    }
}
