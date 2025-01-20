package supersymmetry.common.blocks;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;

public class BlockRebarMesh extends VariantBlock<BlockRebarMesh.RebarMeshType> {

    public BlockRebarMesh() {
        super(Material.IRON);
        setTranslationKey("rebar_mesh");
        setHardness(3.0F);
        setResistance(5.0F);
        setSoundType(SoundType.METAL);
        setDefaultState(getState(RebarMeshType.REBAR_MESH));
    }

    public enum RebarMeshType implements IStringSerializable, IStateHarvestLevel {

        REBAR_MESH("rebar_mesh", 1);

        private final String name;
        private final int harvestLevel;

        RebarMeshType(String name, int harvestLevel) {
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
