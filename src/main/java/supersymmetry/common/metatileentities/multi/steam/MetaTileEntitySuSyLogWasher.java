package supersymmetry.common.metatileentities.multi.steam;

import static net.minecraft.block.BlockDirectional.FACING;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.capability.impl.SteamMultiWorkable;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.ParallelLogicType;
import gregtech.api.metatileentity.multiblock.RecipeMapSteamMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.BlockPaddleShaft;
import supersymmetry.common.blocks.SuSyBlocks;

public class MetaTileEntitySuSyLogWasher extends RecipeMapSteamMultiblockController {

    private static final int PARALLEL_LIMIT = 8;
    private FluidTankList fluidImportInventory;

    public MetaTileEntitySuSyLogWasher(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMap.getByName("ore_washer"), CONVERSION_RATE);
        this.recipeMapWorkable = new LogWasherRecipeLogic(this, CONVERSION_RATE);
        this.recipeMapWorkable.setParallelLimit(PARALLEL_LIMIT);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntitySuSyLogWasher(this.metaTileEntityId);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        initializeAbilities2();
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        resetTileAbilities();
    }

    private void initializeAbilities2() {
        this.fluidImportInventory = new FluidTankList(true, getAbilities(MultiblockAbility.IMPORT_FLUIDS));
        this.inputInventory = new ItemHandlerList(getAbilities(MultiblockAbility.IMPORT_ITEMS));
        List<IItemHandlerModifiable> exportHandlers = new ArrayList<>();
        exportHandlers.addAll(getAbilities(MultiblockAbility.EXPORT_ITEMS));
        exportHandlers.addAll(getAbilities(MultiblockAbility.STEAM_EXPORT_ITEMS));
        this.outputInventory = new ItemHandlerList(exportHandlers);
        this.steamFluidTank = new FluidTankList(true, getAbilities(MultiblockAbility.STEAM));
    }

    private void resetTileAbilities() {
        this.fluidImportInventory = new FluidTankList(true);
        this.inputInventory = new GTItemStackHandler(this, 0);
        this.outputInventory = new GTItemStackHandler(this, 0);
        this.steamFluidTank = new FluidTankList(true);
    }

    public IMultipleTankHandler getFluidImportInventory() {
        return fluidImportInventory;
    }

    private static class LogWasherRecipeLogic extends SteamMultiWorkable {

        public LogWasherRecipeLogic(RecipeMapSteamMultiblockController tileEntity, double conversionRate) {
            super(tileEntity, conversionRate);
        }

        @NotNull
        @Override
        public ParallelLogicType getParallelLogicType() {
            return ParallelLogicType.MULTIPLY;
        }

        @Override
        public void applyParallelBonus(@NotNull RecipeBuilder<?> builder) {
            int currentRecipeEUt = builder.getEUt();
            int currentRecipeDuration = builder.getDuration();
            builder.EUt((int) Math.min(32.0, Math.ceil(currentRecipeEUt * 1.33)))
                    .duration((int) (currentRecipeDuration * 1.5));
        }

        @Override
        protected IMultipleTankHandler getInputTank() {
            MetaTileEntitySuSyLogWasher controller = (MetaTileEntitySuSyLogWasher) metaTileEntity;
            return controller.getFluidImportInventory();
        }
    }

    private static IBlockState[] getShaftStates() {
        IBlockState shaftState = SuSyBlocks.PADDLE_SHAFT.getState(BlockPaddleShaft.ShaftType.IRON);
        return new IBlockState[] {
                shaftState.withProperty(FACING, EnumFacing.NORTH),
                shaftState.withProperty(FACING, EnumFacing.SOUTH),
                shaftState.withProperty(FACING, EnumFacing.WEST),
                shaftState.withProperty(FACING, EnumFacing.EAST)
        };
    }

    @Override
    @NotNull
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("PIIP", "    ", "    ")
                .aisle("FPPF", "PSSP", "    ")
                .aisle(" BB ", "PSSP", "    ")
                .aisle(" BB ", "PSSP", "    ")
                .aisle(" BB ", "PSSP", "    ")
                .aisle("F  F", "FBBF", "PSSP")
                .aisle("    ", " BB ", "PSSP")
                .aisle("    ", " BB ", "PSSP")
                .aisle("F  F", "FXXF", "CGGX")
                .where('C', this.selfPredicate())
                .where('P', states(getPipeCasingState()))
                .where('F', frames(Materials.Bronze))
                .where('S', states(getShaftStates()))
                .where('X', abilities(MultiblockAbility.STEAM_EXPORT_ITEMS).setPreviewCount(1)
                        .or(abilities(MultiblockAbility.STEAM).setPreviewCount(1))
                        .or(abilities(MultiblockAbility.IMPORT_FLUIDS).setPreviewCount(1))
                        .or(abilities(MultiblockAbility.EXPORT_ITEMS).setPreviewCount(0)))
                .where('I', abilities(MultiblockAbility.IMPORT_ITEMS).setPreviewCount(1)
                        .or(states(getCasingState())))
                .where('G', states(getGearBoxState()))
                .where('B', states(getCasingState()))
                .where('#', air())
                .where(' ', any())
                .build();
    }

    protected static IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.BRONZE_BRICKS);
    }

    protected static IBlockState getPipeCasingState() {
        return MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.BRONZE_PIPE);
    }

    private static IBlockState getGearBoxState() {
        return MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.BRONZE_GEARBOX);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.BRONZE_PLATED_BRICKS;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.multiblock.steam_.duration_modifier"));
        tooltip.add(I18n.format("gregtech.universal.tooltip.parallel", PARALLEL_LIMIT));
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return false;
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return SusyTextures.LOG_WASHER_OVERLAY;
    }
}
