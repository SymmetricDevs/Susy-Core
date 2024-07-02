package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiFluidHatch;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.metatileentity.multiblock.ICryogenicProvider;
import supersymmetry.api.metatileentity.multiblock.ICryogenicReceiver;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.common.blocks.BlockSuSyMultiblockCasing;
import supersymmetry.common.blocks.SuSyBlocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static gregtech.api.util.RelativeDirection.*;

public class MetaTileEntitySingleColumnCryogenicDistillationPlant extends RecipeMapMultiblockController implements ICryogenicProvider {

    private @Nullable ICryogenicReceiver receiver;

    public MetaTileEntitySingleColumnCryogenicDistillationPlant(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.SINGLE_COLUMN_CRYOGENIC_DISTILLATION);
        this.recipeMapWorkable = new MultiblockRecipeLogic(this, false);
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntitySingleColumnCryogenicDistillationPlant(this.metaTileEntityId);
    }

    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(RIGHT, FRONT, UP)
                .aisle("CCC", "CCC", "CCC")
                .aisle("CSC", "CFC", "CCC")
                .aisle("XXX", "XFX", "XXX").setRepeatable(1,16)
                .aisle("CCC", "CCC", "CCC")
                .aisle("CEC", "E E", "CEC")
                .aisle("DDD", "DED", "DDD")
                .where('S', this.selfPredicate())
                .where('C', states(this.getCasingState())
                        .or(abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(3))
                        .or(abilities(MultiblockAbility.IMPORT_ITEMS).setMaxGlobalLimited(1))
                        .or(autoAbilities(false, true, false, false, false, false, false).setExactLimit(1)))
                .where('F', states(SuSyBlocks.MULTIBLOCK_CASING.getState(BlockSuSyMultiblockCasing.CasingType.STRUCTURAL_PACKING)))
                .where('X', states(getCasingState())
                        .or(metaTileEntities(MultiblockAbility.REGISTRY.get(MultiblockAbility.EXPORT_FLUIDS).stream()
                                .filter(mte->!(mte instanceof MetaTileEntityMultiFluidHatch))
                                .toArray(MetaTileEntity[]::new))
                                .setMaxLayerLimited(1))
                        .or(metaTileEntities(MultiblockAbility.REGISTRY.get(MultiblockAbility.IMPORT_FLUIDS).stream()
                                .filter(mte->!(mte instanceof MetaTileEntityMultiFluidHatch))
                                .toArray(MetaTileEntity[]::new))
                                .setMaxLayerLimited(1)))
                .where('D', states(this.getCasingState()))
                .where('E', states(this.getCasingState())
                        .or(abilities(MultiblockAbility.PASSTHROUGH_HATCH)))
                .where('#', air())
                .where(' ', cryogenicRecieverPredicate())
                .build();
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        if (this.receiver != null) {
            this.receiver.setCryogenicProvider(null);
            this.receiver = null;
        }
    }

    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.FROST_PROOF_CASING;
    }

    protected IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(MetalCasingType.ALUMINIUM_FROSTPROOF);
    }

    @Nonnull
    protected ICubeRenderer getFrontOverlay() {
        return Textures.BLAST_FURNACE_OVERLAY;
    }

    @Override
    public void setReceiver(@NotNull ICryogenicReceiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }
}
