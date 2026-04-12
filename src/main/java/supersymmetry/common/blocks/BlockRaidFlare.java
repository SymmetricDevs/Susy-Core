package supersymmetry.common.blocks;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import supersymmetry.common.tileentities.TileEntityFlare;

import java.util.Random;

import static supersymmetry.common.faction.FactionHateManager.addHate;
import static supersymmetry.common.faction.FactionHateManager.getHate;

public class BlockRaidFlare extends VariantBlock<BlockRaidFlare.BlockRaidFlareType> {
    public BlockRaidFlare() {
        super(Material.IRON);
        this.setTranslationKey("raid_flare_block");
        this.setHardness(0.5f);
        this.setSoundType(SoundType.METAL);
        this.setHarvestLevel("pickaxe", 1);
        this.setLightLevel(1.0f);
    }

    public int quantityDropped(Random random) {
        return 0;
    }

    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.AIR;
    }

    @Override
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        return false;
    }

    public static enum BlockRaidFlareType implements IStringSerializable, IStateHarvestLevel {

        BANDITFLARE("bandit_flare", 2, 1.0f, 0.0f, 0.0f,"Bandits");
        //add feds later

        private final String name;
        private final int harvestLevel;
        private final float red;
        private final float green;
        private final float blue;
        private final String faction;

        private BlockRaidFlareType(String name, int harvestLevel, float red, float green, float blue, String faction) {
            this.name = name;
            this.harvestLevel = harvestLevel;
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.faction = faction;
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

        public String getFaction(){
            return this.faction;
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

        flare.setFlareFaction(type.getFaction());
        flare.setFlareType(type.getName());

        return flare;
    }

    //HATE
    @Override
    public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(pos);

            if (tile instanceof TileEntityFlare) {
                TileEntityFlare flare = (TileEntityFlare) tile;
                String faction = flare.getFlareFaction();
                if (faction != null && !faction.isEmpty()) {
                    int currentHate = getHate(player, faction);
                    addHate(player, faction, -currentHate);
                }
            }
        }

        super.onBlockHarvested(world, pos, state, player);
    }
}
