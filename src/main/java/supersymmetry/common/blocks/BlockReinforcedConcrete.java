package supersymmetry.common.blocks;

import gregtech.api.GregTechAPI;
import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class BlockReinforcedConcrete extends VariantBlock<BlockReinforcedConcrete.Type> {

    public BlockReinforcedConcrete(){
        super(Material.ROCK);
        setTranslationKey("concrete_reinforced");
        setHardness(6.0f);
        setResistance(15.0f);
        setSoundType(SoundType.STONE);
        setHarvestLevel("pickaxe", 2);
        setDefaultState(getState(Type.FRAMED));
        setCreativeTab(GregTechAPI.TAB_GREGTECH_DECORATIONS);
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

    public enum Type implements IStringSerializable, IStateHarvestLevel {
        FRAMED("framed", 2);

        private final String name;
        private final int harvestLevel;

        Type(String name, int harvestLevel) {
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
            return "pickaxe";
        }
    }
}
