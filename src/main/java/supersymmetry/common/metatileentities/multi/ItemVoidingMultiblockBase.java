package supersymmetry.common.metatileentities.multi;

import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandlerModifiable;

public abstract class ItemVoidingMultiblockBase extends MultiblockWithDisplayBase {
    public final int voidingFrequency = 10;
    public boolean active = false;
    public int baseVoidingRate = 10;
    public ItemVoidingMultiblockBase(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }
    public int voidItems(int items) {
        int toVoidCount = items;
        for(IItemHandlerModifiable storage: getAbilities(MultiblockAbility.IMPORT_ITEMS)) {
            for(int i = 0; i < (storage.getSlots() - 1) && items > 0; i++) {
                ItemStack stack = storage.getStackInSlot(i);
                if(!stack.isEmpty()) {
                    int removeFromThisStack = Integer.min(stack.getCount(), items);
                    storage.extractItem(i, removeFromThisStack, false);
                    items -= removeFromThisStack;
                }
            }
        }
        return toVoidCount - items;
    }
    public void doVoiding() {
        active = voidItems(baseVoidingRate) > 0;
    }

    @Override
    protected void updateFormedValid() {
        if(getWorld().isRemote) return;
        if(getOffsetTimer() % voidingFrequency == 0) {
            doVoiding();
        }
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return false;
    }

}
