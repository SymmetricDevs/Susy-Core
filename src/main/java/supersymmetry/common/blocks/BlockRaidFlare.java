package supersymmetry.common.blocks;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.World;
import supersymmetry.common.tileentities.TileEntityFlare;

public class BlockRaidFlare extends VariantBlock<BlockRaidFlare.BlockRaidFlareType> {
    public BlockRaidFlare() {
        super(Material.IRON);
        this.setTranslationKey("bandit_flare_block");
        this.setHardness(0.5f);
        this.setSoundType(SoundType.METAL);
        this.setHarvestLevel("pickaxe", 1);
    }

    public static enum BlockRaidFlareType implements IStringSerializable, IStateHarvestLevel {

        BANDITFLARE("bandit_flare", 2, 1.0f, 0.0f, 0.0f);
        //add feds later

        private final String name;
        private final int harvestLevel;
        private final float red;
        private final float green;
        private final float blue;

        private BlockRaidFlareType(String name, int harvestLevel, float red, float green, float blue) {
            this.name = name;
            this.harvestLevel = harvestLevel;
            this.red = red;
            this.green = green;
            this.blue = blue;
        }

        public float getRed()   { return red; }
        public float getGreen() { return green; }
        public float getBlue()  { return blue; }

        @Override
        public int getHarvestLevel(IBlockState iBlockState) {
            return this.harvestLevel;
        }

        @Override
        public String getName() {
            return this.name;
        }
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        BlockRaidFlareType type = this.getState(state);
        TileEntityFlare flare = new TileEntityFlare();
        flare.setColor(type.getRed(), type.getGreen(), type.getBlue());
        return flare;
    }
}
