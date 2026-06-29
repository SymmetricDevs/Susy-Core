package supersymmetry.common.metatileentities.multiblockpart;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityEnergyHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntitySubstationEnergyHatch;
import supersymmetry.common.metatileentities.SuSyMetaTileEntities;

public class SusyMetaTileEntitySubstationEnergyHatch extends MetaTileEntitySubstationEnergyHatch
                                                     implements IMultiblockAbilityPart<IEnergyContainer> {

    public SusyMetaTileEntitySubstationEnergyHatch(ResourceLocation metaTileEntityId, int tier, int amperage,
                                                   boolean isExportHatch) {
        super(metaTileEntityId, tier, amperage, isExportHatch);
    }

    @Override
    public void getSubItems(CreativeTabs creativeTab, NonNullList<ItemStack> subItems) {
        for (MetaTileEntityEnergyHatch hatch : SuSyMetaTileEntities.NEW_SUBSTATION_ENERGY_OUTPUT_HATCH_64A) {
            if (hatch != null) subItems.add(hatch.getStackForm());
        }
        for (MetaTileEntityEnergyHatch hatch : SuSyMetaTileEntities.NEW_SUBSTATION_ENERGY_OUTPUT_HATCH_256A) {
            if (hatch != null) subItems.add(hatch.getStackForm());
        }
        for (MetaTileEntityEnergyHatch hatch : SuSyMetaTileEntities.NEW_SUBSTATION_ENERGY_INPUT_HATCH_64A) {
            if (hatch != null) subItems.add(hatch.getStackForm());
        }
        for (MetaTileEntityEnergyHatch hatch : SuSyMetaTileEntities.NEW_SUBSTATION_ENERGY_INPUT_HATCH_256A) {
            if (hatch != null) subItems.add(hatch.getStackForm());
        }
    }
}
