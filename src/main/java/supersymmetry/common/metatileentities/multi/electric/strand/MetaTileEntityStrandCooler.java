package supersymmetry.common.metatileentities.multi.electric.strand;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.items.materialitem.MetaPrefixItem;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.capability.Strand;
import supersymmetry.api.capability.StrandConversion;

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
            FluidStack drained = fluidInventory.drain(Materials.Water.getFluid(amount), false);
            if (drained == null || drained.amount != amount) {
                return false;
            }
            current = OreDictUnifier.get(conversion.prefix, input.getStrand().material, conversion.amount);
            if (!outputInventory.insertItem(0, current, true).isEmpty()) {
                return false;
            }
            fluidInventory.drain(Materials.Water.getFluid(amount), true);
            input.take();
            maxProgress = 1;
            progress = 0;
            return true;
        }
        return false;
    }



    @Override
    protected void output() {
        outputInventory.insertItem(0, current, false);
    }

    @Override
    protected Strand resultingStrand() {
        return null;
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return null;
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityStrandCooler(metaTileEntityId);
    }
}
