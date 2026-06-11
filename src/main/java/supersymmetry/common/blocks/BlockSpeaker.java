package supersymmetry.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.World;

import gregtech.api.block.VariantBlock;
import supersymmetry.common.tileentities.TileEntitySpeaker;

public class BlockSpeaker extends VariantBlock<BlockSpeaker.BlockSpeakerType> {

    public static enum BlockSpeakerType implements IStringSerializable {

        SINGLE("single", 16),
        NETWORKED("networked", 64);

        private final String name;
        private final int volume;

        private BlockSpeakerType(String name, int volume) {
            this.name = name;
            this.volume = volume;
        }

        public int getVolume() {
            return volume;
        }

        @Override
        public String getName() {
            return this.name;
        }
    }

    public BlockSpeaker() {
        super(Material.IRON);
        this.setTranslationKey("speaker");
        this.setHardness(4.5f);
        this.setSoundType(SoundType.METAL);
        this.setHarvestLevel("wrench", 1);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        BlockSpeakerType type = this.getState(state);

        var te = new TileEntitySpeaker(type);
        return te;
    }
}
