package supersymmetry.common.blocks;

import gregtech.api.GregTechAPI;
import gregtech.api.block.VariantBlock;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.IStringSerializable;
import org.jetbrains.annotations.NotNull;
import supersymmetry.common.materials.SusyMaterials;

import javax.annotation.Nonnull;
import java.util.Random;

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
        this.VALUES = StoneType.values();
        return new BlockStateContainer(this, this.VARIANT);
    }

    @Override
    @SuppressWarnings("deprecation")
    protected boolean canSilkHarvest() {
        return this.stoneVariant == StoneVariant.SMOOTH;
    }

    @NotNull
    @Override
    public Item getItemDropped(@NotNull IBlockState state, @NotNull Random rand, int fortune) {
        return Item.getItemFromBlock(this.stoneVariant == StoneVariant.SMOOTH ?
                SuSyBlocks.SUSY_STONE_BLOCKS.get(StoneVariant.COBBLE) : this);
    }

    public double getWalkingSpeed() {
        return this.stoneVariant.walkingSpeed;
    }

    public enum StoneVariant {
        SMOOTH("susy_stone_smooth"),
        COBBLE("susy_stone_cobble", 2.0F, 10.0F),
        BRICKS("susy_stone_bricks", 0.25);
//      TODO
//        COBBLE_MOSSY("stone_cobble_mossy", 2.0F, 10.0F),
//        POLISHED("stone_polished"),
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
        public final double walkingSpeed;

        StoneVariant(@Nonnull String id) {
            this(id, id);
        }

        StoneVariant(@Nonnull String id, double walkingSpeed) {
            this(id, id, 1.5F, 10.0F, walkingSpeed);
        }


        StoneVariant(@Nonnull String id, @Nonnull String translationKey) {
            this(id, translationKey, 1.5F, 10.0F, 0);
        }

        StoneVariant(@Nonnull String id, float hardness, float resistance) {
            this(id, id, hardness, resistance, 0);
        }

        StoneVariant(@Nonnull String id, @Nonnull String translationKey, float hardness, float resistance, double walkingSpeed) {
            this.id = id;
            this.translationKey = translationKey;
            this.hardness = hardness;
            this.resistance = resistance;
            this.walkingSpeed = walkingSpeed;
        }
    }

    public enum StoneType implements IStringSerializable {
        GABBRO("gabbro", MapColor.GRAY),
        GNEISS("gneiss", MapColor.RED_STAINED_HARDENED_CLAY),
        LIMESTONE("limestone", MapColor.GRAY_STAINED_HARDENED_CLAY),
        PHYLLITE("phyllite", MapColor.GRAY),
        QUARTZITE("quartzite", MapColor.QUARTZ),
        SHALE("shale", MapColor.RED_STAINED_HARDENED_CLAY),
        SLATE("slate", MapColor.RED_STAINED_HARDENED_CLAY),
        SOAPSTONE("soapstone", MapColor.GRAY_STAINED_HARDENED_CLAY),
        KIMBERLITE("kimberlite", MapColor.GRAY),
        INDUSTRIAL_CONCRETE("industrial_concrete", MapColor.YELLOW_STAINED_HARDENED_CLAY),
        MILITARY_CONCRETE("minitary_concrete", MapColor.BLACK),
        ANORTHOSITE("anorthosite", MapColor.GRAY);

        private final String name;
        public final MapColor mapColor;

        StoneType(@Nonnull String name, @Nonnull MapColor mapColor) {
            this.name = name;
            this.mapColor = mapColor;
        }

        @Nonnull
        public String getName() {
            return this.name;
        }

        public OrePrefix getOrePrefix() {
            return switch (this) {
                case GABBRO, GNEISS, LIMESTONE, PHYLLITE, QUARTZITE, SHALE, SLATE, SOAPSTONE, KIMBERLITE, ANORTHOSITE ->
                        OrePrefix.stone;
                case INDUSTRIAL_CONCRETE, MILITARY_CONCRETE ->
                        OrePrefix.block;
            };
        }

        public gregtech.api.unification.material.Material getMaterial() {
            return switch (this) {
                case GABBRO -> SusyMaterials.Gabbro;
                case GNEISS -> SusyMaterials.Gneiss;
                case LIMESTONE -> SusyMaterials.Limestone;
                case PHYLLITE -> SusyMaterials.Phyllite;
                case QUARTZITE -> Materials.Quartzite;
                case SHALE -> SusyMaterials.Shale;
                case SLATE -> SusyMaterials.Slate;
                case SOAPSTONE -> Materials.Soapstone;
                case KIMBERLITE -> SusyMaterials.Kimberlite;
                case ANORTHOSITE -> SusyMaterials.Anorthosite;
                case INDUSTRIAL_CONCRETE, MILITARY_CONCRETE -> Materials.Concrete;
            };
        }
    }
}
