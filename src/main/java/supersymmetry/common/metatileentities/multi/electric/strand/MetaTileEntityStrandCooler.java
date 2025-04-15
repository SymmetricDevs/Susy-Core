package supersymmetry.common.metatileentities.multi.electric.strand;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.items.materialitem.MetaPrefixItem;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;
import gregtech.api.util.RelativeDirection;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.capability.Strand;
import supersymmetry.api.capability.StrandConversion;
import supersymmetry.api.metatileentity.multiblock.SuSyMultiblockAbilities;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.BlockMetallurgyRoll;
import supersymmetry.common.blocks.BlockSuSyMultiblockCasing;
import supersymmetry.common.blocks.SuSyBlocks;

import java.util.function.Supplier;

public class MetaTileEntityStrandCooler extends MetaTileEntityStrandShaper {
    private ItemStack current;
    public MetaTileEntityStrandCooler(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public long getVoltage() {
        return 8;
    }

    @Override
    protected boolean consumeInputsAndSetupRecipe() {
        if (input.getStrand() != null) {
            StrandConversion conversion = StrandConversion.getConversion(input.getStrand());
            if (conversion == null) {
                return false;
            }
            int amount = (int) (conversion.amount * conversion.prefix.getMaterialAmount(input.getStrand().material) / GTValues.M);
            FluidStack drained = inputFluidInventory.drain(Materials.Water.getFluid(amount), false);
            if (drained == null || drained.amount != amount) {
                return false;
            }
            current = OreDictUnifier.get(conversion.prefix, input.getStrand().material, conversion.amount);
            if (!GTTransferUtils.insertItem(outputInventory, current, true).isEmpty()) {
                return false;
            }
            inputFluidInventory.drain(Materials.Water.getFluid(amount), true);
            input.take();
            maxProgress = 1;
            progress = 0;
            return true;
        }
        return false;
    }



    @Override
    protected void output() {
        GTTransferUtils.insertItem(outputInventory, current, false);
    }

    @Override
    protected Strand resultingStrand() {
        return null;
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("CCCCC", "CCOCC", "     ", "     ")
                .aisle("CCCCC", "CRRRC", "CAAAC", "CCCCC")
                .aisle("CCCCC", "CRRRC", "PAAAP", "PPPPP")
                .aisle("CCCCC", "CRRRC", "CAAAC", "CCCCC")
                .aisle("CCCCC", "CRRRC", "PAAAP", "PPPPP")
                .aisle("CCCCC", "CRRRC", "CAAAC", "CCCCC")
                .aisle("CCCCC", "CRRRC", "PAAAP", "PPPPP")
                .aisle("CCCCC", "CRRRC", "CAAAC", "CCCCC")
                .aisle("CCCCC", "CRRRC", "PAAAP", "PPPPP")
                .aisle("CCCCC", "CRRRC", "CAAAC", "CCCCC")
                .aisle("CCCCC", "CCICC", "CAAAC", "CCSCC")
                .where('S', selfPredicate())
                .where('P', states(getPipeCasingState()))
                .where('I', abilities(SuSyMultiblockAbilities.STRAND_IMPORT))
                .where('O', abilities(MultiblockAbility.EXPORT_ITEMS))
                .where('C', states(getCasingState())
                        .or(autoAbilities(true, false))
                        .or(abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(2))
                        .or(abilities(MultiblockAbility.IMPORT_FLUIDS).setMinGlobalLimited(1).setMaxGlobalLimited(2)))
                .where('A', air())
                .where('R', rollOrientation(RelativeDirection.RIGHT))
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return SusyTextures.MONEL_500_CASING;
    }

    private IBlockState getPipeCasingState() {
        return SuSyBlocks.MULTIBLOCK_CASING.getState(BlockSuSyMultiblockCasing.CasingType.MONEL_500_PIPE);
    }

    private IBlockState getCasingState() {
        return SuSyBlocks.MULTIBLOCK_CASING.getState(BlockSuSyMultiblockCasing.CasingType.MONEL_500_CASING);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityStrandCooler(metaTileEntityId);
    }
}
