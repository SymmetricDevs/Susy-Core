package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.GTValues;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ImageCycleButtonWidget;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.StoneVariantBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.capability.impl.QuarryLogic;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.textures.SusyTextures;

import javax.annotation.Nonnull;

public class MetaTileEntityQuarry extends RecipeMapMultiblockController {

    private boolean usingRecipeMode;

    private QuarryLogic quarryLogic;
    private int ticksPerOperation = 5;
    private int tickProgress = 0;


    public MetaTileEntityQuarry(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.QUARRY_RECIPES);
        quarryLogic = new QuarryLogic(this);
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
                        .or(autoAbilities(true, true, false, true, false, false, false))
                        )
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
        return SusyTextures.DUMPER_OVERLAY; //TODO: custom texture
    }

    @Override
    public boolean isMultiblockPartWeatherResistant(@Nonnull IMultiblockPart part) {
        return true;
    }


    @Override
    public TraceabilityPredicate autoAbilities(boolean checkEnergyIn, boolean checkMaintenance, boolean checkItemIn, boolean checkItemOut, boolean checkFluidIn, boolean checkFluidOut, boolean checkMuffler) {
        TraceabilityPredicate predicate = super.autoAbilities(checkMaintenance, checkMuffler);
        if (checkEnergyIn)
            predicate = predicate.or(abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(2).setPreviewCount(1));

        if (checkItemOut && this.recipeMap.getMaxOutputs() > 0)
            predicate = predicate.or(abilities(MultiblockAbility.EXPORT_ITEMS).setMaxGlobalLimited(2).setPreviewCount(1));

        if(checkMaintenance)
            predicate = predicate.or(abilities(MultiblockAbility.MAINTENANCE_HATCH).setMaxGlobalLimited(1));

        return predicate;
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        quarryLogic.init();
    }

    @Override
    protected @NotNull Widget getFlexButton(int x, int y, int width, int height) {
        return new ImageCycleButtonWidget(x, y, width, height, GuiTextures.BUTTON_MINER_MODES, 2, this::getCurrentMode,
                this::setCurrentMode)
                .setTooltipHoverString(mode -> mode == 1 ? "gregtech.machine.miner.multi.modes": "gregtech.machine.miner.done"); //TODO
    }


    private int getCurrentMode() {
        return usingRecipeMode ? 1 : 0;
    }

    private void setCurrentMode(int usingRecipeMode) {
        this.usingRecipeMode = usingRecipeMode == 1;
    }


    @Override
    protected boolean shouldUpdate(MTETrait trait) {
        if(trait == this.recipeMapWorkable)
            return usingRecipeMode;
        return super.shouldUpdate(trait);
    }

    @Override
    public void update() {
        if(!usingRecipeMode && drainEnergy(true)){
            if(tickProgress == ticksPerOperation)
                quarryLogic.doQuarryOperation();

            tickProgress = (tickProgress + 1) % (ticksPerOperation + 1);
            drainEnergy(false);
        }
        super.update();
    }

    public boolean drainEnergy(boolean simulate) {
        long energyToDrain = GTValues.VA[getEnergyTier()];
        long resultEnergy = energyContainer.getEnergyStored() - energyToDrain;
        if (resultEnergy >= 0L && resultEnergy <= energyContainer.getEnergyCapacity()) {
            if (!simulate)
                energyContainer.changeEnergy(-energyToDrain);
            return true;
        }
        return false;
    }

    public int getEnergyTier() {
        return GTUtility.getFloorTierByVoltage(energyContainer.getInputVoltage());
    }
}
