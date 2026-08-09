package supersymmetry.common.metatileentities.multi.electric;

import static gregtech.api.metatileentity.MetaTileEntityHolder.TRACKED_TICKS;
import static supersymmetry.api.blocks.VariantHorizontalRotatableBlock.FACING;
import static supersymmetry.api.metatileentity.multiblock.SuSyPredicates.heliostat;

import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.Recipe;
import gregtech.api.util.TextComponentUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.RelativeDirection;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.SusyLog;
import supersymmetry.api.capability.SuSyDataCodes;
import supersymmetry.api.metatileentity.multiblock.SuSyPredicates;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.api.recipes.properties.SolarFurnaceMinPowerProperty;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.*;
import supersymmetry.common.util.RecipeCheckUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class MetaTileEntitySolarFurnace extends RecipeMapMultiblockController {

    public static final int WATTS_PER_HELIOSTAT = 2000;
    public int numValidHeliostats = 0;
    public int currentPower = 0;
    public boolean hasEnoughPower = false;

    private final int[] recipeSpeedStats = new int[TRACKED_TICKS];
    private int statsIndex = 0;

    public MetaTileEntitySolarFurnace(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.SOLAR_FURNACE_RECIPES);
        this.recipeMapWorkable = new SolarFurnaceRecipeLogic(this);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntitySolarFurnace(metaTileEntityId);
    }

    @NotNull
    @Override
    protected  BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("      FFF      ", "       F       ", "               ", "               ", "               ",
                        "               ", "               ", "               ", "               ", "               ",
                        "               ")
                .aisle("               ", "       F       ", "       F       ", "               ", "               ",
                        "               ", "               ", "               ", "               ", "               ",
                        "               ")
                .aisle("               ", "               ", "       F       ", "       F       ", "               ",
                        "               ", "               ", "               ", "               ", "               ",
                        "               ")
                .aisle("FF           FF", "F             F", "               ", "       F       ", "       F       ",
                        "               ", "               ", "               ", "               ", "               ",
                        "               ")
                .aisle("F             F", "       F       ", " F     F     F ", " F   FFFFF   F ", "       F       ",
                        "       F       ", "               ", "               ", "               ", "               ",
                        "               ")
                .aisle("     FFFFF     ", "      MMM      ", "     MMMMM     ", "  FFFMMMMMFFF  ", "  F  MMMMM  F  ",
                        "  F   MMM   F  ", "       F       ", "       F       ", "               ", "               ",
                        "               ")
                .aisle("    FMMMMMF    ", "    MM###MM    ", "   MM#####MM   ", " FFMM#####MMFF ", "   MM#####MM   ",
                        "    MM###MM    ", "   FMMMMMMMF   ", "   F  MMM  F   ", "       F       ", "       F       ",
                        "               ")
                .aisle("  FFM#####MFF  ", "  MM#######MM  ", " MM#########MM ", "FMM#########MMF", " MM#########MM ",
                        "  MM#######MM  ", "   M#######M   ", "   MMM###MMM   ", "    F MMM F    ", "    F  M  F    ",
                        "      FFF      ")
                .aisle(" FMM#######MMF ", " M###########M ", "M#############M", "M#############M", "M#############M",
                        " M###########M ", " MM#########MM ", "  M#########M  ", "  MMMM###MMMM  ", "    MMM#MMM    ",
                        "     FMMMF     ")
                .aisle(" M###########M ", "M#############M", "###############", "###############", "###############",
                        "M#############M", "M#############M", " M###########M ", " M###########M ", "  MM#######MM  ",
                        "    MM###MM    ")
                .aisle("M#############M", "###############", "###############", "###############", "###############",
                        "###############", "###############", " ############# ", " ############# ", "  ###########  ",
                        "    #######    ")
                .aisle("#######I#######", "###############", "###############", "###############", "###############",
                        "###############", "###############", " ############# ", " ############# ", "  ###########  ",
                        "    #######    ")
                .aisle("####### #######", "#######I#######", "###############", "###############", "###############",
                        "###############", "###############", " ############# ", " ############# ", "  ###########  ",
                        "    #######    ")
                .aisle("#####I   I#####", "######I I######", "#######U#######", "#######X#######", "###############",
                        "###############", "###############", " ############# ", " ############# ", "  ###########  ",
                        "    #######    ")
                .aisle("##### # # #####", "###### I ######", "####### #######", "####### #######", "###############",
                        "###############", "###############", " ############# ", " ############# ", "  ###########  ",
                        "    #######    ")
                .aisle("#####  S  #####", "######   ######", "####### #######", "####### #######", "###############",
                        "###############", "###############", " ############# ", " ############# ", "  ###########  ",
                        "    #######    ")
                .aisle("               ", "######   ######", "####### #######", "####### #######", "###############",
                        "###############", "###############", " ############# ", " ############# ", "  ###########  ",
                        "    #######    ")
                .aisle("               ", "######   ######", "####### #######", "####### #######", "###############",
                        "###############", "###############", " ############# ", " ############# ", "  ###########  ",
                        "    #######    ")
                .aisle("               ", "H#H#H#   #H#H#H", "####### #######", "####### #######", "###############",
                        "###############", "###############", " ############# ", " ############# ", "  ###########  ",
                        "    #######    ")
                .aisle("               ", "               ", "####### #######", "####### #######", "###############",
                        "###############", "###############", " ############# ", " ############# ", "  ###########  ",
                        "    #######    ")
                .aisle("               ", "               ", "H#H#H#H H#H#H#H", "####### #######", "###############",
                        "###############", "###############", " ############# ", " ############# ", "  ###########  ",
                        "    #######    ")
                .aisle("               ", "               ", "               ", "####### #######", "###############",
                        "###############", "###############", " ############# ", " ############# ", "  ###########  ",
                        "    #######    ")
                .aisle("               ", "               ", "               ", "H#H#H#H H#H#H#H", "###############",
                        "###############", "###############", " ############# ", " ############# ", "  ###########  ",
                        "    #######    ")
                .aisle("               ", "               ", "               ", "               ", "###############",
                        "###############", "###############", " ############# ", " ############# ", "  ###########  ",
                        "    #######    ")
                .aisle("               ", "               ", "               ", "               ", "H#H#H#H#H#H#H#H",
                        "###############", "###############", " ############# ", " ############# ", "  ###########  ",
                        "    #######    ")
                .aisle("               ", "               ", "               ", "               ", "               ",
                        "###############", "###############", " ############# ", " ############# ", "  ###########  ",
                        "    #######    ")
                .aisle("               ", "               ", "               ", "               ", "               ",
                        "H#H#H#H#H#H#H#H", "###############", " ############# ", " ############# ", "  ###########  ",
                        "    #######    ")
                .aisle("               ", "               ", "               ", "               ", "               ",
                        "               ", "###############", " ############# ", " ############# ", "  ###########  ",
                        "    #######    ")
                .aisle("               ", "               ", "               ", "               ", "               ",
                        "               ", "H#H#H#H#H#H#H#H", " ############# ", " ############# ", "  ###########  ",
                        "    #######    ")
                .aisle("               ", "               ", "               ", "               ", "               ",
                        "               ", "               ", " ############# ", " ############# ", "  ###########  ",
                        "    #######    ")
                .aisle("               ", "               ", "               ", "               ", "               ",
                        "               ", "               ", " #H#H#H#H#H#H# ", " ############# ", "  ###########  ",
                        "    #######    ")
                .aisle("               ", "               ", "               ", "               ", "               ",
                        "               ", "               ", "               ", " ############# ", "  ###########  ",
                        "    #######    ")
                .aisle("               ", "               ", "               ", "               ", "               ",
                        "               ", "               ", "               ", " #H#H#H#H#H#H# ", "  ###########  ",
                        "    #######    ")
                .aisle("               ", "               ", "               ", "               ", "               ",
                        "               ", "               ", "               ", "               ", "  ###########  ",
                        "    #######    ")
                .aisle("               ", "               ", "               ", "               ", "               ",
                        "               ", "               ", "               ", "               ", "  H#H#H#H#H#H  ",
                        "    #######    ")
                .where('S', selfPredicate())
                .where('F', frames(Materials.Aluminium))
                .where('I', frames(Materials.Aluminium).or(autoAbilities(false, true, true, true, true, true, false)))
                .where('M', epoxyMirrorOrientation().or(steelMirrorOrientation()))
                .where('H', heliostat(this.getFrontFacing().getOpposite()).or(air()))
                .where('#', air())
                .where('X', redirectingMirrorOrientation())
                .where('U', states(SuSyBlocks.SOLAR_FURNACE_CRUCIBLE.getState(BlockSolarFurnaceCrucible.SolarFurnaceCrucibleType.DEFAULT)))
                .where(' ', any())
                .build();
    }

    public static enum EnumMirrorSides implements IStringSerializable {

        TOP_LEFT("top_left"),
        TOP_RIGHT("top_right"),
        BOTTOM_LEFT("bottom_left"),
        BOTTOM_RIGHT("bottom_right");

        public static EnumMirrorSides fromInteger(int number) {
            switch (number) {
                case 1:
                    return TOP_RIGHT;
                case 2:
                    return BOTTOM_LEFT;
                case 3:
                    return BOTTOM_RIGHT;
                default:
                    return TOP_LEFT;
            }
        }
        private final String name;

        private EnumMirrorSides(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }

        public String getName() {
            return this.name;
        }
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.FROST_PROOF_CASING;
    }

    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return SusyTextures.HEAT_EXCHANGER_OVERLAY;
    }

    protected List<BlockPos> heliostats;

    @Override
    protected void formStructure(PatternMatchContext context) {
        this.heliostats = context.getOrDefault("HeliostatPositions", new LinkedList<>());
        super.formStructure(context);
    }
    protected IBlockState epoxyMirrorState() {
        return SuSyBlocks.SOLAR_FURNACE_MIRROR.getState(BlockSolarFurnaceMirror.SolarFurnaceMirrorType.EPOXY);
    }

    protected TraceabilityPredicate epoxyMirrorOrientation() {
        return SuSyPredicates.orientation(this, epoxyMirrorState(), RelativeDirection.FRONT, FACING);
    }

    protected IBlockState steelMirrorState() {
        return SuSyBlocks.SOLAR_FURNACE_MIRROR.getState(BlockSolarFurnaceMirror.SolarFurnaceMirrorType.STEEL);
    }

    protected TraceabilityPredicate steelMirrorOrientation() {
        return SuSyPredicates.orientation(this, steelMirrorState(), RelativeDirection.FRONT, FACING);
    }

    protected IBlockState redirectingMirrorState() {
        return SuSyBlocks.SOLAR_FURNACE_REDIRECTING_MIRROR.getState(BlockSolarFurnaceRedirectingMirror.SolarFurnaceRedirectingMirrorType.DEFAULT);
    }

    protected TraceabilityPredicate redirectingMirrorOrientation() {
        return SuSyPredicates.orientation(this, redirectingMirrorState(), RelativeDirection.BACK, FACING);
    }

    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }

    @Override
    public void update() {
        super.update();
        if (getOffsetTimer() % 100 == 0) {
            numValidHeliostats = getNumOfValidHeliostats();
        }
        currentPower = WATTS_PER_HELIOSTAT * numValidHeliostats;

    }

    public int getNumOfValidHeliostats() {
        World world = this.getWorld();
        int i = 0;
        if (heliostats != null && !heliostats.isEmpty()) {
            for (BlockPos pos : heliostats) {
                if (world.getBlockState(pos).getBlock() == SuSyBlocks.HELIOSTAT && getWorld().canSeeSky(pos)) {
                    i++;
                }
            }
        }
        return i;
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        textList.add(new TextComponentTranslation("HELIOSTAT NUMBER: " + numValidHeliostats));
        textList.add(new TextComponentTranslation("CURRENT POWER: " + currentPower));
        textList.add(new TextComponentTranslation("AVG SPEED: " + getAverageSpeed()));
    }

    @Override
    protected void addWarningText(List<ITextComponent> textList) {
        super.addWarningText(textList);
        if (isStructureFormed() && this.isActive() && !hasEnoughPower) {
                textList.add(TextComponentUtil.translationWithColor(TextFormatting.YELLOW,
                        "susy.multiblock.solar_furnace.low_power"));
        }
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.numValidHeliostats = 0;
        this.currentPower = 0;
        this.hasEnoughPower = false;
    }

    private void updateSpeedStats(int progress) {
        recipeSpeedStats[statsIndex] = progress;
        statsIndex = (statsIndex + 1) % TRACKED_TICKS;
    }

    public float getAverageSpeed() {
        return ((float) Arrays.stream(recipeSpeedStats).sum()) / TRACKED_TICKS;
    }

    public class SolarFurnaceRecipeLogic extends MultiblockRecipeLogic {

        private int recipePower;
        private int heatBuffer = 0;
        private boolean isHalted;

        public SolarFurnaceRecipeLogic(RecipeMapMultiblockController tileEntity) {
            super(tileEntity);
        }

        @Override
        public boolean checkRecipe(@NotNull Recipe recipe) {
            return super.checkRecipe(recipe) && RecipeCheckUtils.checkDimension(recipe, this.metaTileEntity) &&
                    recipe.hasProperty(SolarFurnaceMinPowerProperty.getInstance()) && recipe.getProperty(SolarFurnaceMinPowerProperty.getInstance(), 2147483647) <= currentPower;
        }

        @Override
        protected void setupRecipe(Recipe recipe) {
            super.setupRecipe(recipe);
            this.recipePower = recipe.getProperty(SolarFurnaceMinPowerProperty.getInstance(), 0);
            this.heatBuffer = 0;
        }

        /// Do not overclock
        @Override
        protected int @NotNull [] calculateOverclock(@NotNull Recipe recipe) {
            return new int[] { recipe.getEUt(), recipe.getDuration() };
        }

        @Override
        protected boolean hasEnoughPower(int @NotNull [] resultOverclock) {
            return true;
        }

        @Override
        protected void updateRecipeProgress() {
            if (this.canRecipeProgress) {

                int totalHeat = currentPower + heatBuffer;

                int remainingHeat = 0;
                int maxProgress;
                if (currentPower >= getRecipePower()) {
                    remainingHeat = totalHeat % getRecipePower();
                    maxProgress = totalHeat / getRecipePower();
                    hasEnoughPower = true;
                } else {
                    maxProgress = (currentPower - getRecipePower()) / 2000; //regress if not enough power
                    hasEnoughPower = false;
                }

                SusyLog.logger.debug("ASDFGH CURRENT POWER: {}", currentPower);
                SusyLog.logger.debug("ASDFGH TOTAL HEAT: {}", totalHeat);
                SusyLog.logger.debug("ASDFGH REMAINING HEAT: {}", remainingHeat);
                SusyLog.logger.debug("ASDFGH MAX PROGRESS: {}", maxProgress);
                SusyLog.logger.debug("ASDFGH RECIPE POWER: {}", getRecipePower());
                updateSpeedStats(maxProgress);

                boolean halted = maxProgress == 0;
                if (this.isHalted != halted) {
                    this.isHalted = halted;
                    writeCustomData(SuSyDataCodes.UPDATE_WORK_HALTED, buf -> buf.writeBoolean(halted));
                }


                this.progressTime += maxProgress;
                this.heatBuffer = remainingHeat;
                if (this.progressTime > this.maxProgressTime) {
                    this.completeRecipe();
                }
            }
            if (getOffsetTimer() % 100 == 0) {
                numValidHeliostats = getNumOfValidHeliostats();
            }
            currentPower = WATTS_PER_HELIOSTAT * numValidHeliostats;
        }

        @Deprecated
        protected int getRecipePower() {
            return recipePower != 0 ? recipePower : 12000; //copied from evap pool idk what this does exactly
        }

        @Override
        protected void completeRecipe() {
            super.completeRecipe();
            this.recipePower = 0;
            this.heatBuffer = 0;
        }

        @Override
        public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
            super.receiveCustomData(dataId, buf);
            if (dataId == SuSyDataCodes.UPDATE_WORK_HALTED) {
                this.isHalted = buf.readBoolean();
            }
        }

        @Override
        public void writeInitialSyncData(@NotNull PacketBuffer buf) {
            super.writeInitialSyncData(buf);
            buf.writeBoolean(this.isHalted);
        }

        @Override
        public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
            super.receiveInitialSyncData(buf);
            this.isHalted = buf.readBoolean();
        }

        @NotNull @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound compound = super.serializeNBT();
            if (this.progressTime > 0) {
                compound.setInteger("RecipePower", recipePower);
            }
            compound.setBoolean("IsHalted", this.isHalted);
            return compound;
        }

        @Override
        public void deserializeNBT(@NotNull NBTTagCompound compound) {
            super.deserializeNBT(compound);
            if (this.progressTime > 0) {
                recipePower = compound.getInteger("RecipePower");
            }
            this.isHalted = compound.getBoolean("IsHalted");
        }
    }
}
