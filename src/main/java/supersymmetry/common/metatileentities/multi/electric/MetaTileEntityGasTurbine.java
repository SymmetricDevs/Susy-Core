package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.RelativeDirection;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMultiblockCasing;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.BlockTurbineRotor;
import supersymmetry.common.blocks.SuSyBlocks;

import java.util.function.Supplier;

import static supersymmetry.api.blocks.VariantHorizontalRotatableBlock.FACING;

public class MetaTileEntityGasTurbine extends MetaTileEntitySUSYLargeTurbine {

    public MetaTileEntityGasTurbine(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, int tier, int maxSpeed, int accel, int decel, IBlockState casingState, IBlockState rotorState, ICubeRenderer casingRenderer, ICubeRenderer frontOverlay) {
        super(metaTileEntityId, recipeMap, tier, maxSpeed, accel, decel, casingState, rotorState, casingRenderer, frontOverlay);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityGasTurbine(metaTileEntityId, recipeMap, tier, maxSpeed, accel, decel, casingState, rotorState, casingRenderer, frontOverlay);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        // Different characters use common constraints. Copied from GCyM
        TraceabilityPredicate casingPredicate = states(this.casingState).setMinGlobalLimited(51)
                .or(abilities(MultiblockAbility.IMPORT_ITEMS).setPreviewCount(1));
        TraceabilityPredicate maintenance = abilities(MultiblockAbility.MAINTENANCE_HATCH).setMaxGlobalLimited(1);

        return FactoryBlockPattern.start()
                .aisle("GAAAAAAAO", "GAAAAAAAO", "G   A   O")
                .aisle("GAAAAAAAO", "IDDDDCCCF", "GAAAAAAAO")
                .aisle("GAAAAAAAO", "GSAAAAAAO", "G   A   O")
                .where('S', selfPredicate())
                .where('A', casingPredicate
                        .or(autoAbilities(false, false, false, false, false, false, false))
                        .or(maintenance))
                .where('O', casingPredicate
                        .or(autoAbilities(false, false, false, false, false, true, false))
                        .or(maintenance))
                .where('C', coilOrientation())
                .where('D', rotorOrientation())
                .where('F', abilities(MultiblockAbility.OUTPUT_ENERGY))
                .where('G', casingPredicate
                        .or(autoAbilities(false, false, false, false, true, false, false))
                        .or(maintenance))
                .where('I', states(MetaBlocks.MULTIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.EXTREME_ENGINE_INTAKE_CASING)))
                .where(' ', any())
                .build();
    }
}
