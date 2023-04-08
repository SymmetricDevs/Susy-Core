package supersymmetry.common.blocks;

import gregtech.api.GregTechAPI;
import gregtech.api.block.VariantBlock;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import java.util.Random;
import javax.annotation.Nonnull;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.Item;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import supersymmetry.common.materials.SusyMaterials;

public class SusyStoneVariantBlock extends VariantBlock<SusyStoneVariantBlock.StoneType> {
    private static final PropertyEnum<StoneType> PROPERTY = PropertyEnum.create("variant", StoneType.class);
    private final StoneVariant stoneVariant;

    public SusyStoneVariantBlock(@Nonnull StoneVariant stoneVariant) {
        super(Material.ROCK);
        this.stoneVariant = stoneVariant;
        this.setRegistryName(stoneVariant.id);
        this.setTranslationKey(stoneVariant.translationKey);
        this.setHardness(stoneVariant.hardness);
        this.setResistance(stoneVariant.resistance);
        this.setSoundType(SoundType.STONE);
        this.setHarvestLevel("pickaxe", 0);
        this.setDefaultState(this.getState(StoneType.GABBRO));
        this.setCreativeTab(GregTechAPI.TAB_GREGTECH_DECORATIONS);
    }

    @Nonnull
    protected BlockStateContainer createBlockState() {
        this.VARIANT = PROPERTY;
        this.VALUES = SusyStoneVariantBlock.StoneType.values();
        return new BlockStateContainer(this, new IProperty[]{this.VARIANT});
    }

    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    public double getWalkingSpeedBonus() {
        return 1.6;
    }

    public boolean checkApplicableBlocks(@Nonnull IBlockState state) {
        return false;
    }

    protected boolean canSilkHarvest() {
        return this.stoneVariant == SusyStoneVariantBlock.StoneVariant.SMOOTH;
    }

    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock((Block)(this.stoneVariant == SusyStoneVariantBlock.StoneVariant.SMOOTH ? (Block)SuSyBlocks.SUSY_STONE_BLOCKS.get(SusyStoneVariantBlock.StoneVariant.COBBLE) : this));
    }

    public static enum StoneVariant {
        SMOOTH("susy_stone_smooth"),
        COBBLE("susy_stone_cobble", 2.0F, 10.0F);
//      TODO
//        COBBLE_MOSSY("stone_cobble_mossy", 2.0F, 10.0F),
//        POLISHED("stone_polished"),
//        BRICKS("stone_bricks"),
//        BRICKS_CRACKED("stone_bricks_cracked"),
//        BRICKS_MOSSY("stone_bricks_mossy"),
//        CHISELED("stone_chiseled"),
//        TILED("stone_tiled"),
//        TILED_SMALL("stone_tiled_small"),
//        BRICKS_SMALL("stone_bricks_small"),
//        WINDMILL_A("stone_windmill_a", "stone_bricks_windmill_a"),
//        WINDMILL_B("stone_windmill_b", "stone_bricks_windmill_b"),
//        BRICKS_SQUARE("stone_bricks_square");

        public final String id;
        public final String translationKey;
        public final float hardness;
        public final float resistance;

        private StoneVariant(@Nonnull String id) {
            this(id, id);
        }

        private StoneVariant(@Nonnull String id, @Nonnull String translationKey) {
            this(id, translationKey, 1.5F, 10.0F);
        }

        private StoneVariant(@Nonnull String id, float hardness, float resistance) {
            this(id, id, hardness, resistance);
        }

        private StoneVariant(@Nonnull String id, @Nonnull String translationKey, float hardness, float resistance) {
            this.id = id;
            this.translationKey = translationKey;
            this.hardness = hardness;
            this.resistance = resistance;
        }
    }

    public static enum StoneType implements IStringSerializable {
        GABBRO("gabbro", MapColor.GRAY),
        GNEISS("gneiss", MapColor.RED_STAINED_HARDENED_CLAY),
        GRAPHITE("graphite", MapColor.BLACK),
        LIMESTONE("limestone", MapColor.GRAY_STAINED_HARDENED_CLAY),
        MICA("mica", MapColor.WHITE_STAINED_HARDENED_CLAY),
        PHYLLITE("phyllite", MapColor.GRAY),
        QUARTZITE("quartzite", MapColor.QUARTZ),
        SHALE("shale", MapColor.RED_STAINED_HARDENED_CLAY),
        SLATE("slate", MapColor.RED_STAINED_HARDENED_CLAY),
        SOAPSTONE("soapstone", MapColor.GRAY_STAINED_HARDENED_CLAY),
        KIMBERLITE("kimberlite", MapColor.GRAY);

        private final String name;
        public final MapColor mapColor;

        private StoneType(@Nonnull String name, @Nonnull MapColor mapColor) {
            this.name = name;
            this.mapColor = mapColor;
        }

        @Nonnull
        public String getName() {
            return this.name;
        }

        public OrePrefix getOrePrefix() {
            switch (this) {
                case GABBRO:
                case GNEISS:
                case GRAPHITE:
                case LIMESTONE:
                case MICA:
                case PHYLLITE:
                case QUARTZITE:
                case SHALE:
                case SLATE:
                case SOAPSTONE:
                case KIMBERLITE:
                    return OrePrefix.stone;
                default:
                    throw new IllegalStateException("Unreachable");
            }
        }

        public gregtech.api.unification.material.Material getMaterial() {
            switch (this) {
                case GABBRO:
                    return SusyMaterials.Gabbro;
                case GNEISS:
                    return SusyMaterials.Gneiss;
                case GRAPHITE:
                    return Materials.Graphite;
                case LIMESTONE:
                    return SusyMaterials.Limestone;
                case MICA:
                    return Materials.Mica;
                case PHYLLITE:
                    return SusyMaterials.Phyllite;
                case QUARTZITE:
                    return Materials.Quartzite;
                case SHALE:
                    return SusyMaterials.Shale;
                case SLATE:
                    return SusyMaterials.Slate;
                case SOAPSTONE:
                    return Materials.Soapstone;
                case KIMBERLITE:
                    return SusyMaterials.Kimberlite;
                default:
                    throw new IllegalStateException("Unreachable");
            }
        }
    }
}
