package supersymmetry.common.metatileentities.multi.electric;

import static supersymmetry.api.blocks.VariantHorizontalRotatableBlock.FACING;
import static supersymmetry.api.metatileentity.multiblock.SuSyPredicates.heliostats;

import org.jspecify.annotations.NonNull;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
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
import supersymmetry.api.capability.impl.NoEnergyMultiblockRecipeLogic;
import supersymmetry.api.metatileentity.multiblock.SuSyPredicates;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.BlockSolarFurnaceMirror;
import supersymmetry.common.blocks.SuSyBlocks;

public class MetaTileEntitySolarFurnace extends RecipeMapMultiblockController {

    public static final int MAX_HELIOSTAT_DISTANCE = 21;
    private int timer = Math.round(getOffsetTimer());

    public MetaTileEntitySolarFurnace(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.SOLAR_FURNACE_RECIPES);
        this.recipeMapWorkable = new NoEnergyMultiblockRecipeLogic(this);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntitySolarFurnace(metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
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
                .aisle("#####I   I#####", "######I I######", "#######I#######", "#######C#######", "###############",
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
                .where('S', selfPredicate()).where('F', frames(Materials.Aluminium))
                .where('I', frames(Materials.Aluminium).or(autoAbilities(false, true, true, true, true, true, false)))
                .where('C', states(MetaBlocks.METAL_CASING.getState(MetalCasingType.TUNGSTENSTEEL_ROBUST)))
                .where('M', epoxyMirrorOrientation().or(steelMirrorOrientation()))
                .where('H', heliostats(RelativeDirection.LEFT)).where('#', air()).where(' ', any()).build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.FROST_PROOF_CASING;
    }

    @NonNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return SusyTextures.HEAT_EXCHANGER_OVERLAY;
    }

    protected IBlockState epoxyMirrorState() {
        return SuSyBlocks.SOLAR_FURNACE_MIRROR.getState(BlockSolarFurnaceMirror.SolarFurnaceMirrorType.EPOXY);
    }

    protected TraceabilityPredicate epoxyMirrorOrientation() {
        return SuSyPredicates.horizontalOrientation(this, epoxyMirrorState(), RelativeDirection.FRONT, FACING);
    }

    protected IBlockState steelMirrorState() {
        return SuSyBlocks.SOLAR_FURNACE_MIRROR.getState(BlockSolarFurnaceMirror.SolarFurnaceMirrorType.STEEL);
    }

    protected TraceabilityPredicate steelMirrorOrientation() {
        return SuSyPredicates.horizontalOrientation(this, steelMirrorState(), RelativeDirection.FRONT, FACING);
    }

    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }

    @Override
    public void update() {
        super.update();
        timer = timer % 200;
        timer++;
        if (timer >= 200) {

        }
    }

    public BlockPos findHeliostat(BlockPos currentBlock, EnumFacing checkDir) {
        for (int i = 0; i < MAX_HELIOSTAT_DISTANCE; i++) {
            if (getWorld().getBlockState(currentBlock).getBlock() == SuSyBlocks.HELIOSTAT) {
                return currentBlock;
            }
            currentBlock.offset(checkDir);
        }
        return null;
    }

    public boolean checkHeliostatValidity(BlockPos checkPos) {
        return false;
    }
    /*
     * public class SolarFurnaceRecipeLogic extends MultiblockRecipeLogic {
     * 
     * private int recipeJt; private int heatBuffer = 0; private boolean isHeating =
     * false; private boolean isHalted;
     * 
     * public SolarFurnaceRecipeLogic(RecipeMapMultiblockController tileEntity) {
     * super(tileEntity); }
     * 
     * @Override public boolean checkRecipe(@NotNull Recipe recipe) { return
     * super.checkRecipe(recipe) &&
     * recipe.hasProperty(EvaporationEnergyProperty.getInstance()); }
     * 
     * @Override protected void setupRecipe(Recipe recipe) {
     * super.setupRecipe(recipe); this.recipeJt =
     * recipe.getProperty(EvaporationEnergyProperty.getInstance(), 0); // TODO: is
     * this correct? this.heatBuffer = 0; }
     * 
     * /// Do not overclock
     * 
     * @Override protected int @NotNull [] calculateOverclock(@NotNull Recipe
     * recipe) { return new int[] { recipe.getEUt(), recipe.getDuration() }; }
     * 
     * @Override protected boolean hasEnoughPower(int @NotNull [] resultOverclock) {
     * return true; }
     * 
     * @Override protected void updateRecipeProgress() { if (this.canRecipeProgress)
     * { int baseHeat = getHeatFromSunlight() + heatBuffer; int coilHeat = 0; int
     * maxEnergy2Draw = (int) Math.min(Math.min(getEnergyStored(),
     * getMaxEnergyInput()), getMaxHeatFromCoils() / SuSyUtility.JOULES_PER_EU); if
     * (drawEnergy(maxEnergy2Draw, true)) { drawEnergy(maxEnergy2Draw, false);
     * coilHeat = maxEnergy2Draw * SuSyUtility.JOULES_PER_EU; }
     * 
     * int totalHeat = (baseHeat + coilHeat); int remainingHeat = totalHeat %
     * getRecipeJt(); int maxProgress = totalHeat / getRecipeJt();
     * 
     * updateSpeedStats(maxProgress);
     * 
     * boolean halted = maxProgress == 0; if (this.isHalted != halted) {
     * this.isHalted = halted; writeCustomData(SuSyDataCodes.UPDATE_WORK_HALTED, buf
     * -> buf.writeBoolean(halted)); } this.isHeating = coilHeat > 0;
     * 
     * this.progressTime += maxProgress; this.heatBuffer = remainingHeat; if
     * (this.progressTime > this.maxProgressTime) { this.completeRecipe(); } } }
     * 
     * /// Workaround for backwards compat /// Random fallback number IDK
     * 
     * @Deprecated protected int getRecipeJt() { return recipeJt != 0 ? recipeJt :
     * 500; }
     * 
     * /// This could potentially be cached in the mte, but ig it doesn't matter
     * that much protected int getHeatFromSunlight() { return exposedBlocks *
     * JT_PER_BLOCK; }
     * 
     * /// This could potentially be cached in the mte, but ig it doesn't matter
     * that much protected long getMaxEnergyInput() { IEnergyContainer
     * energyContainer = getEnergyContainer(); /// This seems to be correct as far
     * as I've tested return energyContainer.getInputVoltage() *
     * energyContainer.getInputAmperage(); }
     * 
     * @Override protected void completeRecipe() { super.completeRecipe();
     * this.recipeJt = 0; this.heatBuffer = 0; }
     * 
     * @Override public void receiveCustomData(int dataId, @NotNull PacketBuffer
     * buf) { super.receiveCustomData(dataId, buf); if (dataId ==
     * SuSyDataCodes.UPDATE_WORK_HALTED) { this.isHalted = buf.readBoolean(); } }
     * 
     * @Override public void writeInitialSyncData(@NotNull PacketBuffer buf) {
     * super.writeInitialSyncData(buf); buf.writeBoolean(this.isHalted); }
     * 
     * @Override public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
     * super.receiveInitialSyncData(buf); this.isHalted = buf.readBoolean(); }
     * 
     * @NotNull
     * 
     * @Override public NBTTagCompound serializeNBT() { NBTTagCompound compound =
     * super.serializeNBT(); if (this.progressTime > 0) {
     * compound.setInteger("RecipeJt", recipeJt); } compound.setBoolean("IsHalted",
     * this.isHalted); return compound; }
     * 
     * @Override public void deserializeNBT(@NotNull NBTTagCompound compound) {
     * super.deserializeNBT(compound); if (this.progressTime > 0) { recipeJt =
     * compound.getInteger("RecipeJt"); } this.isHalted =
     * compound.getBoolean("IsHalted"); } }
     */
}
