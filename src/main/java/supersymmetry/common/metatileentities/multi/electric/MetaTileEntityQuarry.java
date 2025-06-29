package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.GTValues;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ImageCycleButtonWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockDisplayText;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.Recipe;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.StoneVariantBlock;
import it.unimi.dsi.fastutil.ints.IntLists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.capability.impl.QuarryLogic;
import supersymmetry.api.gui.SusyGuiTextures;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.api.recipes.properties.DimensionProperty;

import javax.annotation.Nonnull;
import java.util.List;

import static gregtech.api.capability.GregtechDataCodes.assignId;

/** The quarry multiblock is a regular {@link RecipeMapMultiblockController}, with an additional mode similar to {@link gregtech.common.metatileentities.multi.electric.MetaTileEntityLargeMiner} that allows from breaking block in game.
 * For this it uses {@link QuarryLogic}
 * @author h3tR / RMI
 */

public class MetaTileEntityQuarry extends RecipeMapMultiblockController {

    private static final int BASE_TICKS_PER_EXCAVATION = 10;

    private static final int QUARRY_EXCAVATION_ACTIVE = assignId();

    private boolean isInitialized = false;

    private boolean excavationMode = false;
    private boolean excavationActive = false;
    private int excavationProgress = 0;
    private final QuarryLogic quarryLogic;

    public MetaTileEntityQuarry(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.QUARRY_RECIPES);
        this.quarryLogic = new QuarryLogic(this);
    }
    @Override

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityQuarry(this.metaTileEntityId);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {

        return FactoryBlockPattern.start()
                .aisle("CCCCCFFFFFCCCCC", " CC    F    CC ", "       F       ", "       F       ", "       F       ")
                .aisle("CFS         SFC", "CFS         SFC", " FS         SF ", " FS         SF ", " FSFFFFFFFFFSF ")
                .aisle("CS           SC", "CS           SC", " S           S ", " S           S ", " S           S ")
                .aisle("C             C", "               ", "               ", "               ", " F           F ")
                .aisle("C             C", "               ", "               ", "               ", " F           F ")
                .aisle("F             F", "               ", "               ", "               ", " F           F ")
                .aisle("AG           GA", "SS           SS", "               ", "               ", " F           F ")
                .aisle("FG           GG", " G           G ", " F           F ", " F           F ", " F           F ")
                .aisle("AG           GA", "SS           SS", "               ", "               ", " F           F ")
                .aisle("F             F", "               ", "               ", "               ", " F           F ")
                .aisle("C             C", "               ", "               ", "               ", " F           F ")
                .aisle("C             C", "               ", "               ", "               ", " F           F ")
                .aisle("CS           SC", "CS           SC", " S           S ", " S           S ", " S           S ")
                .aisle("CFS         SFC", "CFS         SFC", " FS         SF ", " FS         SF ", " FSFFFFFFFFFSF ")
                .aisle("CCCCCFAAAFCCCCC", " CC   AMA   CC ", "               ", "               ", "               ")
                .where('M', selfPredicate())
                .where('A', states(getCasingState())
                        .or(autoAbilities(true, true, true, true, false, false, false)))
                .where('S', states(getCasingState()))
                .where('G', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID)))
                .where('C', states(MetaBlocks.STONE_BLOCKS.get(StoneVariantBlock.StoneVariant.SMOOTH).getState(StoneVariantBlock.StoneType.CONCRETE_LIGHT)))
                .where('F', frames(Materials.Steel))
                .where('G', states(MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_GEARBOX)))
                .build();
    }


    protected static IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.CHUNK_MINER_OVERLAY; //TODO: custom texture?
    }

    @Override
    public boolean isMultiblockPartWeatherResistant(@Nonnull IMultiblockPart part) {
        return true;
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        if(!this.isInitialized)
            quarryLogic.init();
        this.isInitialized = true;
    }


    @Override
    public boolean checkRecipe(@NotNull Recipe recipe, boolean consumeIfSuccess) {
        for (int dimension : recipe.getProperty(DimensionProperty.getInstance(), IntLists.EMPTY_LIST))
            if (dimension == this.getWorld().provider.getDimension())
                return super.checkRecipe(recipe, consumeIfSuccess);
        return false;
    }

    @Override
    protected @NotNull Widget getFlexButton(int x, int y, int width, int height) {
        return new ImageCycleButtonWidget(x, y, width, height, SusyGuiTextures.BUTTON_QUARRY_MODES, 2, () -> this.excavationMode ? 1 : 0,
                this::setExcavationMode)
                .setTooltipHoverString(mode -> mode == 1 ? "susy.multiblock.quarry.excavation_mode" : "susy.multiblock.quarry.recipe_mode");
    }

    private void setExcavationMode(int mode) {
        this.excavationMode = mode == 1;
        if(this.excavationMode)
            this.quarryLogic.init(); //reset quarrylogic
    }

    @Override
    protected void updateFormedValid() {
        if(getWorld().isRemote || !this.recipeMapWorkable.isWorkingEnabled()) return;

        if (this.excavationMode && !this.quarryLogic.finished && this.drainEnergy(true) && this.getNumMaintenanceProblems() <= 5){
            this.drainEnergy(false);
            excavationProgress++;
            if(!excavationActive) toggleExcavationActive();

            if (excavationProgress % getTicksPerExcavation() == 0)
                this.quarryLogic.doQuarryOperation();
        } else{
            if(excavationActive) toggleExcavationActive();

            this.recipeMapWorkable.updateWorkable();
        }
    }

    private void toggleExcavationActive(){
        excavationActive = !excavationActive;
        writeCustomData(QUARRY_EXCAVATION_ACTIVE, buf -> buf.writeBoolean(excavationActive));
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        if(excavationMode)
            MultiblockDisplayText.builder(textList, isStructureFormed())
                    .setWorkingStatus(recipeMapWorkable.isWorkingEnabled(), excavationActive)
                    .addEnergyUsageLine(recipeMapWorkable.getEnergyContainer())
                    .addWorkingStatusLine();
        else
            MultiblockDisplayText.builder(textList, isStructureFormed())
                .setWorkingStatus(recipeMapWorkable.isWorkingEnabled(), recipeMapWorkable.isActive())
                .addEnergyUsageLine(recipeMapWorkable.getEnergyContainer())
                .addEnergyTierLine(GTUtility.getTierByVoltage(recipeMapWorkable.getMaxVoltage()))
                .addParallelsLine(recipeMapWorkable.getParallelLimit())
                .addWorkingStatusLine()
                .addProgressLine(recipeMapWorkable.getProgressPercent());
    }

    public int getTicksPerExcavation(){
        return (int) (BASE_TICKS_PER_EXCAVATION / Math.pow(2,getEnergyTier() - 1));
    }

    //cap to EV tier
    public int getEnergyTier() {
        return Math.min(GTUtility.getFloorTierByVoltage(energyContainer.getInputVoltage()), GTValues.EV);
    }

    public boolean drainEnergy(boolean simulate) {
        long energyToDrain = GTValues.VA[this.getEnergyTier()];
        long resultEnergy = this.getEnergyContainer().getEnergyStored() - energyToDrain;
        if (resultEnergy >= 0L && resultEnergy <= this.getEnergyContainer().getEnergyCapacity()) {
            if (!simulate)
                this.getEnergyContainer().changeEnergy(-energyToDrain);
            return true;
        }
        return false;
    }


    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.quarryLogic.readFromNBT(data.getCompoundTag("quarryLogic"));
        this.excavationMode = data.getBoolean("excavationMode");
        this.excavationProgress = data.getInteger("excavationProgress");
        this.excavationActive = data.getBoolean("excavationActive");
        this.isInitialized = true;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setTag("quarryLogic", this.quarryLogic.writeToNBT());
        data.setBoolean("excavationMode", this.excavationMode);
        data.setInteger("excavationProgress", this.excavationProgress);
        data.setBoolean("excavationActive", this.excavationActive);
        return super.writeToNBT(data);
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        if (dataId == QUARRY_EXCAVATION_ACTIVE)
            excavationActive = buf.readBoolean();
        super.receiveCustomData(dataId, buf);
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        buf.writeBoolean(excavationActive);
        buf.writeBoolean(excavationMode);
        super.writeInitialSyncData(buf);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        excavationActive = buf.readBoolean();
        excavationMode = buf.readBoolean();
        super.receiveInitialSyncData(buf);
    }

    @Override
    public boolean isActive() {
        return (super.isActive() && !excavationMode) || (excavationActive && this.isStructureFormed() && this.recipeMapWorkable.isWorkingEnabled());
    }
}
