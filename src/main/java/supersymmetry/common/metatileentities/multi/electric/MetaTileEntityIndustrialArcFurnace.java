package supersymmetry.common.metatileentities.multi.electric;

import gregicality.multiblocks.api.render.GCYMTextures;
import gregicality.multiblocks.common.block.GCYMMetaBlocks;
import gregicality.multiblocks.common.block.blocks.BlockLargeMultiblockCasing;
import gregicality.multiblocks.common.block.blocks.BlockUniqueCasing;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.ParallelLogicType;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.blocks.BlockBoilerCasing.BoilerCasingType;
import gregtech.common.blocks.BlockFireboxCasing;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.capability.impl.NoEnergyMultiblockRecipeLogic;
import supersymmetry.api.metatileentity.multiblock.SuSyPredicates;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.BlockElectrodeAssembly;
import supersymmetry.common.blocks.SuSyBlocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityIndustrialArcFurnace extends MetaTileEntityAdvancedArcFurnace {
    public MetaTileEntityIndustrialArcFurnace(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        this.recipeMapWorkable = new IndustrialArcFurnaceLogic(this);
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityIndustrialArcFurnace(this.metaTileEntityId);
    }

    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle(" AAAAA ", " AAAAA ", " AAAAA ", " AAAAA ", " EEEEE ", " EEEEE ", "  FFF  ", "  FFF  ", "  FFF  ", "  FFF  ")
                .aisle("AAAAAAA", "AAAAAAA", "AABBBAA", "A#####A", "E#CCC#E", "E#CCC#E", " ACCCA ", "  CCC  ", "  CCC  ", "  CCC  ")
                .aisle("AAAAAAA", "AABBBAA", "AB###BA", "A#####A", "E#####E", "E#####E", "AAAAAAA", "       ", "       ", "  AAA  ")
                .aisle("AAAAAAA", "AABBBAA", "AB###BA", "A#####A", "E#CCC#E", "E#CCC#E", "AACCCAA", "  CCC  ", "  CCC  ", "  CCC  ")
                .aisle("AAAAAAA", "AABBBAA", "AB###BA", "A#####A", "E#####E", "E#####E", "AAAAAAA", "       ", "       ", "  AAA  ")
                .aisle("AAAAAAA", "AAAAAAA", "AABBBAA", "A#####A", "E#CCC#E", "E#CCC#E", " ACCCA ", "  CCC  ", "  CCC  ", "  CCC  ")
                .aisle(" AAAAA ", " AAAAA ", " AAAAA ", " AASAA ", " EEEEE ", " EEEEE ", "       ", "       ", "       ", "       ")
                .where('S', selfPredicate())
                .where('A', states(GCYMMetaBlocks.LARGE_MULTIBLOCK_CASING.getState(BlockLargeMultiblockCasing.CasingType.STRESS_PROOF_CASING)).setMinGlobalLimited(56)
                        .or(autoAbilities()))
                .where('B', SuSyPredicates.sinteringBricks())
                .where('C', states(SuSyBlocks.ELECTRODE_ASSEMBLY.getState(BlockElectrodeAssembly.ElectrodeAssemblyType.CARBON)))
                .where('E', states(GCYMMetaBlocks.UNIQUE_CASING.getState(BlockUniqueCasing.UniqueCasingType.HEAT_VENT)))
                .where('F', frames(Materials.Steel))
                .where(' ', any())
                .where('#', air())
                .build();
    }

    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return GCYMTextures.STRESS_PROOF_CASING;
    }

    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.parallel_limit", 256));

    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return SusyTextures.ARC_FURNACE_OVERLAY;
    }

    private class IndustrialArcFurnaceLogic extends MultiblockRecipeLogic {
        public IndustrialArcFurnaceLogic(RecipeMapMultiblockController tileEntity) {
            super(tileEntity);
        }

        @Override
        public int getParallelLimit() {
            return 256;
        }

        @Override
        protected long getMaxParallelVoltage() {
            return 2147432767L;
        }

        @Override
        public boolean consumesEnergy() {
            return false;
        }

        @Override
        public @NotNull ParallelLogicType getParallelLogicType() {
            return ParallelLogicType.APPEND_ITEMS;
        }
    }
}
