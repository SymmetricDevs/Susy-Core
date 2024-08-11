package supersymmetry.common.metatileentities.multi;

import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandlerModifiable;
import supersymmetry.Supersymmetry;

public abstract class ItemVoidingMultiblockBase extends MultiblockWithDisplayBase {
    public final int voidingFrequency = 10;
    public int bonusMultiplier = 1;
    public boolean active = true;

    public ItemVoidingMultiblockBase(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    public int baseVoidingRate() {
        return 32;
    }
    private int actualVoidingRate() {
        return baseVoidingRate() * this.bonusMultiplier;
    }
    protected void doVoiding() {
        int toSubtract = actualVoidingRate();
        for(IItemHandlerModifiable storage: getAbilities(MultiblockAbility.IMPORT_ITEMS)) {
            for(int i = 0; i < storage.getSlots() && toSubtract > 0; i++) {
                ItemStack stack = storage.getStackInSlot(i);
                if(!stack.isEmpty()) {
                    ItemStack replaceWith = stack.copy();
                    int removeFromThisStack = Integer.min(stack.getCount(), toSubtract);
                    replaceWith.setCount(stack.getCount() - removeFromThisStack);
                    storage.setStackInSlot(i, replaceWith);
                    toSubtract -= removeFromThisStack;
                }
            }
        }
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
