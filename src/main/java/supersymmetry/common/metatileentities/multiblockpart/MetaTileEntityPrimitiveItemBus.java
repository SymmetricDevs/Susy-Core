package supersymmetry.common.metatileentities.multiblockpart;

import java.util.List;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandlerModifiable;

import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityItemBus;
import supersymmetry.api.metatileentity.multiblock.SuSyMultiblockAbilities;
import supersymmetry.client.renderer.textures.SusyTextures;

public class MetaTileEntityPrimitiveItemBus extends MetaTileEntityItemBus {

    public MetaTileEntityPrimitiveItemBus(ResourceLocation metaTileEntityId, boolean isExportHatch) {
        super(metaTileEntityId, 1, isExportHatch);
        initializeInventory();
    }

    @Override
    public ICubeRenderer getBaseTexture() {
        MultiblockControllerBase controller = getController();
        if (controller != null) {
            return this.hatchTexture = controller.getBaseTexture(this);
        } else if (this.hatchTexture != null) {
            if (hatchTexture != Textures.getInactiveTexture(hatchTexture)) {
                return this.hatchTexture = Textures.getInactiveTexture(hatchTexture);
            }
            return this.hatchTexture;
        } else {
            return SusyTextures.MASONRY_BRICK;
        }
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityPrimitiveItemBus(this.metaTileEntityId, this.isExportHatch);
    }

    @Override
    public MultiblockAbility<IItemHandlerModifiable> getAbility() {
        return isExportHatch ? SuSyMultiblockAbilities.PRIMITIVE_EXPORT_ITEMS :
                SuSyMultiblockAbilities.PRIMITIVE_IMPORT_ITEMS;
    }

    @Override
    public void registerAbilities(List<IItemHandlerModifiable> abilityList) {
        abilityList.add(isExportHatch ? this.exportItems : this.importItems);
    }

    @Override
    public int getDefaultPaintingColor() {
        return 0xFFFFFF;
    }

    @Override
    public boolean hasGhostCircuitInventory() {
        return false;
    }

    @Override
    public String getHarvestTool() {
        return "pickaxe";
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return !isExportHatch ? new GTItemStackHandler(this, 0) :
                new NotifiableItemStackHandler(this, 4, getController(), true) {

                    @Override
                    public int getSlotLimit(int slot) {
                        return 16;
                    }
                };
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return isExportHatch ? new GTItemStackHandler(this, 0) :
                new NotifiableItemStackHandler(this, 4, getController(), false) {

                    @Override
                    public int getSlotLimit(int slot) {
                        return 16;
                    }
                };
    }
}
