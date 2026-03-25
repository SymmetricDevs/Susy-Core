package supersymmetry.common.metatileentities.multiblockpart;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityEnergyHatch;
import supersymmetry.common.metatileentities.SuSyMetaTileEntities;

public class SusyMetaTileEntityEnergyHatch extends MetaTileEntityEnergyHatch
                                           implements IMultiblockAbilityPart<IEnergyContainer> {

    public SusyMetaTileEntityEnergyHatch(ResourceLocation metaTileEntityId, int tier, int amperage,
                                         boolean isExportHatch) {
        super(metaTileEntityId, tier, amperage, isExportHatch);
    }

    @Override
    public void getSubItems(CreativeTabs creativeTab, NonNullList<ItemStack> subItems) {
        for (MetaTileEntityEnergyHatch hatch : SuSyMetaTileEntities.NEW_ENERGY_OUTPUT_HATCH_4A) {
            if (hatch != null) subItems.add(hatch.getStackForm());
        }
        for (MetaTileEntityEnergyHatch hatch : SuSyMetaTileEntities.NEW_ENERGY_OUTPUT_HATCH_16A) {
            if (hatch != null) subItems.add(hatch.getStackForm());
        }
    }
}
