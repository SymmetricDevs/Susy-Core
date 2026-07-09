package supersymmetry.common.metatileentities.multi.electric;

import static supersymmetry.api.metatileentity.multiblock.SuSyPredicates.hiddenGearTooth;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.RelativeDirection;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.MetaBlocks;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.BlockGrinderCasing;
import supersymmetry.common.blocks.SuSyBlocks;
import gregtech.common.blocks.BlockTurbineCasing;

public class MetaTileEntityAttritionScrubber extends RecipeMapMultiblockController {

    private static final int PARALLEL_LIMIT = 32;

    public MetaTileEntityAttritionScrubber(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.ATTRITION_SCRUBBER);
        this.recipeMapWorkable = new AttritionScrubberLogic(this);
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityAttritionScrubber(this.metaTileEntityId);
    }

    private static IBlockState getAbrasionResistantCasingState() {
        return SuSyBlocks.GRINDER_CASING.getState(BlockGrinderCasing.Type.ABRASION_RESISTANT_CASING);
    }

    private static IBlockState getGearboxState() {
        return MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_GEARBOX);
    }

    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle(" CCC CCC ", " CCCCCCC ", " CCCCCCC ", " CCCCCCC ", " CCC CCC ", " FGF FGF ")
                .aisle("CCCCCCCCC", "W#B###B#S", "C###C###C", "I#B#C#B#O", "C###C###C", " FGF FGF ")
                .aisle("CCCCCCCCC", "WBBB#BBBS", "C#F#C#F#C", "IBFBCBFBO", "C#F#C#F#C", " FGF FGF ")
                .aisle("CCCCCCCCC", "W#B###B#S", "C###C###C", "I#B#C#B#O", "C###C###C", " F F F F ")
                .aisle(" CCC CCC ", " CXCCCCC ", " CCCCCCC ", " CCCCCCC ", " CCC CCC ", " F F F F ")
                .where('C', states(getAbrasionResistantCasingState()).or(autoAbilities(
                        true, true, false,
                        false, false, false, false)))
                .where('I', abilities(MultiblockAbility.IMPORT_ITEMS).or(states(getAbrasionResistantCasingState())))
                .where('O', abilities(MultiblockAbility.EXPORT_ITEMS).or(states(getAbrasionResistantCasingState())))
                .where('W', abilities(MultiblockAbility.IMPORT_FLUIDS).or(states(getAbrasionResistantCasingState())))
                .where('S', abilities(MultiblockAbility.EXPORT_FLUIDS).or(states(getAbrasionResistantCasingState())))
                .where('G', states(getGearboxState()))
                .where('B', states(MetaBlocks.METAL_CASING.getState(MetalCasingType.ALUMINIUM_FROSTPROOF)))
                .where('F', frames(Materials.Iron))
                .where('X', selfPredicate())
                .where('#', air())
                .where(' ', any())
                .where('M', hiddenGearTooth(
                        RelativeDirection.UP.getRelativeFacing(getFrontFacing(), getUpwardsFacing(), false)
                                .getAxis()))
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart multiblockPart) {
        return SusyTextures.ABRASION_RESISTANT_CASING;
    }

    protected static IBlockState getCasingState() {
        return SuSyBlocks.GRINDER_CASING.getState(BlockGrinderCasing.Type.ABRASION_RESISTANT_CASING);
    }

    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("susy.machine.parallel_pure", 32));
    }

    private static class AttritionScrubberlLogic extends MultiblockRecipeLogic {

        public AttritionScrubberlLogic(RecipeMapMultiblockController tileEntity) {
            super(tileEntity);
            this.setParallelLimit(PARALLEL_LIMIT);
        }
    }
}
