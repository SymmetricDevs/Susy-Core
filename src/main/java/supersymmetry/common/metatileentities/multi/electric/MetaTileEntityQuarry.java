package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.GTValues;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ImageCycleButtonWidget;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
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
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.capability.impl.QuarryLogic;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.api.recipes.properties.DimensionProperty;

import javax.annotation.Nonnull;

/**
 * @author h3tR / RMI
 */

public class MetaTileEntityQuarry extends RecipeMapMultiblockController {

    private static final int BASE_TICKS_PER_EXCAVATION = 10;

    private boolean isInitialized = false;

    private boolean excavationMode = false;
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
        //TODO: custom icons
        return new ImageCycleButtonWidget(x, y, width, height, GuiTextures.BUTTON_MINER_MODES, 2, () -> this.excavationMode ? 1 : 0,
                this::setExcavationMode);
    }

    private void setExcavationMode(int mode) {
        this.excavationMode = mode == 1;
        if(this.excavationMode)
            this.quarryLogic.init(); //reset quarrylogic
    }


    @Override
    protected boolean shouldUpdate(MTETrait trait) {
        //Ignore recipe if set inactive
        //this might be kinda hacky, but it works
        if(trait == this.recipeMapWorkable)
            return !this.excavationMode;
        return super.shouldUpdate(trait);
    }

    @Override
    public void update() {
        super.update();
        if (!this.getWorld().isRemote && this.excavationMode && !this.quarryLogic.finished && this.drainEnergy(true) && this.recipeMapWorkable.isWorkingEnabled()){
            this.drainEnergy(false);
            excavationProgress++;
            if (excavationProgress % getTicksPerExcavation() == 0)
                this.quarryLogic.doQuarryOperation();
        }
    }


    public int getTicksPerExcavation(){
        //TODO energy scaling
        return BASE_TICKS_PER_EXCAVATION;
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
        this.isInitialized = true;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setTag("quarryLogic", this.quarryLogic.writeToNBT());
        data.setBoolean("excavationMode", this.excavationMode);
        data.setInteger("excavationProgress", this.excavationProgress);
        return super.writeToNBT(data);
    }
}
