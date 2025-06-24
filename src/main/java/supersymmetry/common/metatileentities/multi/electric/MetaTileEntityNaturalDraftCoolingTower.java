package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.TextComponentUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockBoilerCasing.BoilerCasingType;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.StoneVariantBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supersymmetry.api.metatileentity.multiblock.CachedPatternRecipeMapMultiblock;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.BlockSuSyMultiblockCasing;
import supersymmetry.common.blocks.SuSyBlocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static supercritical.api.pattern.SCPredicates.*;

public class MetaTileEntityNaturalDraftCoolingTower extends CachedPatternRecipeMapMultiblock {

    private static final String[][] VAPOR_PATTERN = {{
            "  VVV",
            " VVVVV",
            "VVVVVVV",
            "VVVVVVV",
            "VVVVVVV",
            " VVVVV",
            "  VVV"
    }};

    private static final Vec3i PATTERN_OFFSET = new Vec3i(-8, 14, 4);

    private boolean waterFilled;
    private List<BlockPos> waterPositions;

    public MetaTileEntityNaturalDraftCoolingTower(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.NATURAL_DRAFT_COOLING_TOWER);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityNaturalDraftCoolingTower(metaTileEntityId);
    }

    protected static IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);

        this.waterPositions = context.getOrDefault(FLUID_BLOCKS_KEY, new ArrayList<>());
        this.waterFilled = waterPositions.isEmpty();
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.waterPositions = null; // Clear water fill data when the structure is invalidated
        this.waterFilled = false;
    }

    @Override
    protected void updateFormedValid() {
        super.updateFormedValid();
        if (!waterFilled && getOffsetTimer() % 5 == 0) {
            fillFluid(this, this.waterPositions, FluidRegistry.WATER);
            if (this.waterPositions.isEmpty()) {
                this.waterFilled = true;
            }
        }
    }

    @Override
    public boolean isStructureObstructed() {
        return super.isStructureObstructed() || !waterFilled;
    }

    @NotNull
    @Override
    protected BlockPattern createStructurePattern() {

        return FactoryBlockPattern.start()
                .aisle("     CCCCC     ", "     CCCCC     ", "     CCCCC     ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                .aisle("   CCCCCCCCC   ", "   CCWWWWWCC   ", "   CCCCCCCCC   ", "     FFFFF     ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                .aisle("  CCCCCCCCCCC  ", "  CWWWWWWWWWC  ", "  CC#######CC  ", "   FF#####FF   ", "     CCCCC     ", "     CCICC     ", "      CCC      ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                .aisle(" CCCCCCCCCCCCC ", " CWWWWWWWWWWWC ", " CC#########CC ", "  F#########F  ", "   CCAAAAACC   ", "    C#P#P#C    ", "     C###C     ", "     CCCCC     ", "      CCC      ", "               ", "               ", "               ", "               ", "      CCC      ", "     CCCCC     ", "     CCCCC     ")
                .aisle(" CCCCCCCCCCCCC ", " CWWWWWWWWWWWC ", " CC#########CC ", "  F#########F  ", "   CAAAAAAAC   ", "   CPPPPPPPC   ", "    C#####C    ", "    CCCCCCC    ", "     CCCCC     ", "      CCC      ", "      CCC      ", "      CCC      ", "     CCCCC     ", "     CCCCC     ", "    CC###CC    ", "    CC###CC    ")
                .aisle("CCCCCCCCCCCCCCC", "CWWWWWWWWWWWWWC", "CC###########CC", " F###########F ", "  CAAAAAAAAAC  ", "  C#P#P#P#P#C  ", "   C#######C   ", "   CCFFFFFCC   ", "    CC###CC    ", "     C###C     ", "     C###C     ", "     C###C     ", "    CC###CC    ", "    CC###CC    ", "   CC#####CC   ", "   CC#####CC   ")
                .aisle("CCCCCCCCCCCCCCC", "CWWWWWWWWWWWWWC", "CC###########CC", " F###########F ", "  CAAAAAAAAAC  ", "  CPPPPPPPPPC  ", "  C#########C  ", "   CCFFFFFCC   ", "   CC#####CC   ", "    C#####C    ", "    C#####C    ", "    C#####C    ", "    C#####C    ", "   CC#####CC   ", "   C#######C   ", "   C#######C   ")
                .aisle("CCCCCCCCCCCCCCC", "CWWWWWWWWWWWWWC", "CC###########CC", " F###########F ", "  CAAAAAAAAAC  ", "  I#P#P#P#P#I  ", "  C#########C  ", "   CCFFFFFCC   ", "   CC#####CC   ", "    C#####C    ", "    C#####C    ", "    C#####C    ", "    C#####C    ", "   CC#####CC   ", "   C#######C   ", "   C#######C   ")
                .aisle("CCCCCCCCCCCCCCC", "CWWWWWWWWWWWWWC", "CC###########CC", " F###########F ", "  CAAAAAAAAAC  ", "  CPPPPPPPPPC  ", "  C#########C  ", "   CCFFFFFCC   ", "   CC#####CC   ", "    C#####C    ", "    C#####C    ", "    C#####C    ", "    C#####C    ", "   CC#####CC   ", "   C#######C   ", "   C#######C   ")
                .aisle("CCCCCCCCCCCCCCC", "CWWWWWWWWWWWWWC", "CC###########CC", " F###########F ", "  CAAAAAAAAAC  ", "  C#P#P#P#P#C  ", "   C#######C   ", "   CCFFFFFCC   ", "    CC###CC    ", "     C###C     ", "     C###C     ", "     C###C     ", "    CC###CC    ", "    CC###CC    ", "   CC#####CC   ", "   CC#####CC   ")
                .aisle(" CCCCCCCCCCCCC ", " CWWWWWWWWWWWC ", " CC#########CC ", "  F#########F  ", "   CAAAAAAAC   ", "   CPPPPPPPC   ", "    C#####C    ", "    CCCCCCC    ", "     CCCCC     ", "      CCC      ", "      CCC      ", "      CCC      ", "     CCCCC     ", "     CCCCC     ", "    CC###CC    ", "    CC###CC    ")
                .aisle(" CCCCCCCCCCCCC ", " CWWWWWWWWWWWC ", " CC#########CC ", "  F#########F  ", "   CCAAAAACC   ", "    C#P#P#C    ", "     C###C     ", "     CCCCC     ", "      CCC      ", "               ", "               ", "               ", "               ", "      CCC      ", "     CCCCC     ", "     CCCCC     ")
                .aisle("  CCCCCCCCCCP  ", "  CWWWWWWWWWC  ", "  CC#######CC  ", "   FF#####FF   ", "     CCCCC     ", "     CCICC     ", "      CCC      ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                .aisle("   CCCCCCCCCP  ", "   CCWWWWWCC   ", "   CCCCCCCCC   ", "     FFFFF     ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                .aisle("     CCCCC OOO ", "     CCCCC OSO ", "     CCCCC     ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                .where('S', selfPredicate())
                .where('O', states(getCasingState()).or(autoAbilities(true, true, false, false, false, true, false)))
                .where('I', states(MetaBlocks.STONE_BLOCKS.get(StoneVariantBlock.StoneVariant.SMOOTH).getState(StoneVariantBlock.StoneType.CONCRETE_LIGHT)).or(autoAbilities(false, false, false, false, true, false, false)))
                .where('C', states(MetaBlocks.STONE_BLOCKS.get(StoneVariantBlock.StoneVariant.SMOOTH).getState(StoneVariantBlock.StoneType.CONCRETE_LIGHT)))
                .where('W', fluid(FluidRegistry.WATER))
                .where('F', frames(Materials.Steel))
                .where('A', states(SuSyBlocks.MULTIBLOCK_CASING.getState(BlockSuSyMultiblockCasing.CasingType.STRUCTURAL_PACKING)))
                .where('P', states(MetaBlocks.BOILER_CASING.getState(BoilerCasingType.STEEL_PIPE)))
                .where(' ', any())
                .where('#', air())
                .build();
    }

    @Override
    protected void addErrorText(List<ITextComponent> textList) {
        super.addErrorText(textList);
        if (isStructureFormed() && !waterFilled) {
            textList.add(TextComponentUtil.translationWithColor(TextFormatting.RED,
                    "susy.multiblock.natural_draft_cooling_tower.obstructed"));
            textList.add(TextComponentUtil.translationWithColor(TextFormatting.GRAY,
                    "susy.multiblock.natural_draft_cooling_tower.obstructed.desc"));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("supercritical.machine.fluid_auto_fill.tooltip"));
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return SusyTextures.NATURAL_DRAFT_COOLING_TOWER_OVERLAY;
    }


    @Override
    protected String[][] getPattern() {
        return VAPOR_PATTERN;
    }

    @Override
    protected Vec3i getPatternOffset() {
        return PATTERN_OFFSET;
    }

    @Override
    public void update() {
        super.update();
        if (this.isActive() && getWorld().isRemote)
            createParticles();

    }

    @SideOnly(Side.CLIENT)
    private void createParticles() {
        Random rand = getWorld().rand;
        if (cachedPattern == null || cachedPattern.length == 0) generateCachedPattern(getPattern(), getPatternOffset(), this.frontFacing, isFlipped());
        for (Vec3i offset : cachedPattern) {

            BlockPos pos = this.getPos().add(offset);

            this.getWorld().spawnParticle(EnumParticleTypes.CLOUD,
                    pos.getX() + rand.nextDouble(),
                    pos.getY() + .5F,
                    pos.getZ() + rand.nextDouble(), .1F, .3F, .1F);
        }
    }

    @Override
    public boolean isMultiblockPartWeatherResistant(@NotNull IMultiblockPart part) {
        return true;
    }

    @Override
    public boolean getIsWeatherOrTerrainResistant() {
        return true;
    }
}
