package supersymmetry.common.blocks;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.blocks.VariantDirectionalRotatableBlock;
import supersymmetry.api.blocks.VariantHorizontalRotatableBlock;

import javax.annotation.Nonnull;

public class BlockEngineCasing2 extends VariantDirectionalRotatableBlock<BlockEngineCasing2.EngineCasingType2> {

    public BlockEngineCasing2() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("engine_casing_2");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(EngineCasingType2.CRANKSHAFT));
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @NotNull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.SOLID;
    }

    public enum EngineCasingType2 implements IStringSerializable, IStateHarvestLevel {

        CRANKSHAFT("crankshaft", 3);
        private final String name;
        private final int harvestLevel;

        EngineCasingType2(String name, int harvestLevel) {
            this.name = name;
            this.harvestLevel = harvestLevel;
        }

        @Nonnull
        public String getName() {
            return this.name;
        }

        public int getHarvestLevel(IBlockState state) {
            return this.harvestLevel;
        }

        public String getHarvestTool(IBlockState state) {
            return "wrench";
        }
    }
}
